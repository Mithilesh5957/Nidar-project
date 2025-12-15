package com.nidar.gcs.service;

import com.fazecast.jSerialComm.SerialPort;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.*;

/**
 * Service for bidirectional MAVLink communication via serial port.
 * Reads MAVLink messages from flight controller (COM port) and forwards to QGC
 * via UDP.
 * Receives commands from QGC and forwards to flight controller via serial.
 */
@Slf4j
@Service
public class MAVLinkSerialService {

    @Value("${mavlink.serial.enabled:false}")
    private boolean serialEnabled;

    @Value("${mavlink.serial.port:COM6}")
    private String serialPortName;

    @Value("${mavlink.serial.baudrate:115200}")
    private int baudRate;

    @Value("${mavlink.serial.timeout:5000}")
    private int timeout;

    @Value("${mavproxy.host:localhost}")
    private String qgcHost;

    @Value("${mavproxy.port:14552}")
    private int qgcPort;

    private SerialPort serialPort;
    private DatagramSocket udpSocket;
    private ScheduledExecutorService executorService;
    private volatile boolean running = false;
    private BlockingQueue<byte[]> outgoingQueue = new LinkedBlockingQueue<>();

    @PostConstruct
    public void init() {
        if (!serialEnabled) {
            log.info("Serial communication disabled. Set mavlink.serial.enabled=true to enable.");
            return;
        }

        executorService = Executors.newScheduledThreadPool(2);
        connect();
    }

    public void connect() {
        if (!serialEnabled) {
            return;
        }

        try {
            // Initialize UDP socket for forwarding to QGC
            udpSocket = new DatagramSocket();
            log.info("UDP socket created for forwarding to QGC at {}:{}", qgcHost, qgcPort);

            // Find and open serial port
            SerialPort[] ports = SerialPort.getCommPorts();
            log.info("Available serial ports:");
            for (SerialPort port : ports) {
                log.info("  - {} ({})", port.getSystemPortName(), port.getDescriptivePortName());
            }

            serialPort = SerialPort.getCommPort(serialPortName);
            serialPort.setBaudRate(baudRate);
            serialPort.setNumDataBits(8);
            serialPort.setNumStopBits(1);
            serialPort.setParity(SerialPort.NO_PARITY);
            serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, timeout, 0);

            if (serialPort.openPort()) {
                log.info("Successfully connected to {} at {} baud", serialPortName, baudRate);
                running = true;

                // Start reader thread (serial → UDP)
                executorService.submit(this::readFromSerial);

                // Start writer thread (queue → serial)
                executorService.submit(this::writeToSerial);
            } else {
                log.error("Failed to open serial port {}", serialPortName);
            }

        } catch (Exception e) {
            log.error("Error connecting to serial port: {}", e.getMessage(), e);
            scheduleReconnect();
        }
    }

    /**
     * Read MAVLink messages from serial port and forward to QGC via UDP
     */
    private void readFromSerial() {
        log.info("Started serial reader thread");
        byte[] buffer = new byte[263]; // MAVLink v1 max packet size
        int bufferIndex = 0;
        boolean inPacket = false;
        int expectedLength = 0;

        while (running) {
            try {
                InputStream inputStream = serialPort.getInputStream();
                int available = inputStream.available();

                if (available > 0) {
                    int b = inputStream.read();
                    if (b == -1)
                        continue;

                    // MAVLink v1 packet structure:
                    // 0: STX (0xFE)
                    // 1: payload length
                    // 2: sequence
                    // 3: system ID
                    // 4: component ID
                    // 5: message ID
                    // 6-n: payload
                    // n+1, n+2: CRC

                    if (!inPacket && b == 0xFE) {
                        // Start of MAVLink v1 packet
                        buffer[0] = (byte) b;
                        bufferIndex = 1;
                        inPacket = true;
                    } else if (inPacket) {
                        buffer[bufferIndex++] = (byte) b;

                        if (bufferIndex == 2) {
                            // Got payload length
                            expectedLength = 8 + (buffer[1] & 0xFF); // header(6) + payload + CRC(2)
                        }

                        if (bufferIndex >= 8 && bufferIndex == expectedLength) {
                            // Complete packet received
                            byte[] packet = new byte[bufferIndex];
                            System.arraycopy(buffer, 0, packet, 0, bufferIndex);

                            // Forward to QGC via UDP
                            forwardToQGC(packet);

                            // Log message type
                            int msgId = buffer[5] & 0xFF;
                            if (msgId == 0) {
                                log.debug("Received HEARTBEAT from system {}", buffer[3] & 0xFF);
                            } else {
                                log.trace("Forwarding MAVLink message ID {} ({} bytes)", msgId, bufferIndex);
                            }

                            // Reset for next packet
                            bufferIndex = 0;
                            inPacket = false;
                        }

                        if (bufferIndex >= buffer.length) {
                            // Buffer overflow, reset
                            log.warn("Buffer overflow, resetting parser");
                            bufferIndex = 0;
                            inPacket = false;
                        }
                    }
                } else {
                    Thread.sleep(10);
                }

            } catch (Exception e) {
                if (running) {
                    log.error("Error reading from serial port: {}", e.getMessage());
                    scheduleReconnect();
                    break;
                }
            }
        }
    }

    /**
     * Write MAVLink messages from queue to serial port
     */
    private void writeToSerial() {
        log.info("Started serial writer thread");
        while (running) {
            try {
                byte[] data = outgoingQueue.poll(1, TimeUnit.SECONDS);
                if (data != null && serialPort != null && serialPort.isOpen()) {
                    serialPort.writeBytes(data, data.length);
                    log.debug("Sent {} bytes to flight controller", data.length);
                }
            } catch (Exception e) {
                if (running) {
                    log.error("Error writing to serial port: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Forward MAVLink packet to QGC via UDP
     */
    private void forwardToQGC(byte[] packet) {
        try {
            InetAddress address = InetAddress.getByName(qgcHost);
            DatagramPacket udpPacket = new DatagramPacket(packet, packet.length, address, qgcPort);
            udpSocket.send(udpPacket);
        } catch (Exception e) {
            log.error("Error forwarding to QGC: {}", e.getMessage());
        }
    }

    /**
     * Queue a MAVLink message to be sent to the flight controller
     */
    public void sendToFlightController(byte[] mavlinkPacket) {
        if (serialEnabled && running) {
            try {
                outgoingQueue.offer(mavlinkPacket, 1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("Failed to queue message for flight controller", e);
            }
        }
    }

    /**
     * Schedule automatic reconnection attempt
     */
    private void scheduleReconnect() {
        running = false;
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
        }

        log.info("Scheduling reconnection attempt in 5 seconds...");
        executorService.schedule(() -> {
            log.info("Attempting to reconnect...");
            connect();
        }, 5, TimeUnit.SECONDS);
    }

    public boolean isConnected() {
        return serialEnabled && serialPort != null && serialPort.isOpen();
    }

    @PreDestroy
    public void disconnect() {
        running = false;

        if (executorService != null) {
            executorService.shutdownNow();
            try {
                executorService.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.warn("Interrupted while waiting for executor shutdown");
            }
        }

        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            log.info("Serial port closed");
        }

        if (udpSocket != null && !udpSocket.isClosed()) {
            udpSocket.close();
            log.info("UDP socket closed");
        }
    }
}
