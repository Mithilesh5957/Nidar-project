package com.nidar.gcs.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Service to listen for MAVLink messages from QGroundControl via UDP.
 * Forwards received commands to the flight controller via serial port.
 */
@Slf4j
@Service
public class MAVLinkUDPListener {

    @Value("${mavlink.udp.listener.enabled:false}")
    private boolean listenerEnabled;

    @Value("${mavlink.udp.listener.port:14552}")
    private int listenerPort;

    @Autowired
    private MAVLinkSerialService serialService;

    private DatagramSocket udpSocket;
    private ExecutorService executorService;
    private volatile boolean running = false;

    @PostConstruct
    public void init() {
        if (!listenerEnabled) {
            log.info("UDP listener disabled. Set mavlink.udp.listener.enabled=true to enable.");
            return;
        }

        executorService = Executors.newSingleThreadExecutor();
        startListener();
    }

    public void startListener() {
        if (!listenerEnabled) {
            return;
        }

        try {
            udpSocket = new DatagramSocket(listenerPort);
            log.info("UDP listener started on port {}, waiting for MAVLink from QGC...", listenerPort);
            running = true;

            executorService.submit(this::listenForMessages);

        } catch (Exception e) {
            log.error("Failed to start UDP listener on port {}: {}", listenerPort, e.getMessage(), e);
        }
    }

    /**
     * Listen for incoming MAVLink messages from QGC
     */
    private void listenForMessages() {
        byte[] buffer = new byte[512];

        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);

                byte[] data = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), 0, data, 0, packet.getLength());

                // Parse message type for logging
                if (data.length >= 6 && data[0] == (byte) 0xFE) {
                    int msgId = data[5] & 0xFF;
                    int sysId = data[3] & 0xFF;

                    // Log important commands
                    if (msgId == 76) { // COMMAND_LONG
                        log.debug("Received COMMAND_LONG from QGC (system {})", sysId);
                    } else if (msgId == 23) { // PARAM_SET
                        log.debug("Received PARAM_SET from QGC");
                    } else if (msgId == 44) { // MISSION_COUNT
                        log.debug("Received MISSION_COUNT from QGC");
                    } else {
                        log.trace("Received MAVLink message ID {} from QGC ({} bytes)", msgId, data.length);
                    }

                    // Forward to flight controller via serial
                    serialService.sendToFlightController(data);
                }

            } catch (Exception e) {
                if (running) {
                    log.error("Error receiving UDP packet: {}", e.getMessage());
                }
            }
        }
    }

    public boolean isListening() {
        return listenerEnabled && udpSocket != null && !udpSocket.isClosed();
    }

    @PreDestroy
    public void stop() {
        running = false;

        if (executorService != null) {
            executorService.shutdownNow();
            try {
                executorService.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.warn("Interrupted while waiting for executor shutdown");
            }
        }

        if (udpSocket != null && !udpSocket.isClosed()) {
            udpSocket.close();
            log.info("UDP listener stopped");
        }
    }
}
