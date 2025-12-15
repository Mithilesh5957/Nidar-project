# Installing Maven on Windows (Quick Guide)

Maven is not currently installed on your system. Here are quick installation methods:

## Option 1: Chocolatey (Recommended - Fastest)

### Install Chocolatey first:
Open **PowerShell as Administrator** and run:
```powershell
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
```

### Then install Maven:
```powershell
choco install maven -y
```

### Restart PowerShell and verify:
```powershell
mvn --version
```

---

## Option 2: Manual Installation

1. **Download Maven**:
   - Go to: https://maven.apache.org/download.cgi
   - Download: `apache-maven-3.9.6-bin.zip` (latest stable)

2. **Extract**:
   - Extract to: `C:\Program Files\Apache\maven`

3. **Set Environment Variables**:
   ```powershell
   # Add Maven to PATH
   [Environment]::SetEnvironmentVariable("MAVEN_HOME", "C:\Program Files\Apache\maven", "Machine")
   [Environment]::SetEnvironmentVariable("Path", $env:Path + ";C:\Program Files\Apache\maven\bin", "Machine")
   ```

4. **Restart PowerShell** and test:
   ```powershell
   mvn --version
   ```

---

## Option 3: Use Docker (No Maven Install Required)

If you don't want to install Maven, you can use Docker's Maven image to build and run:

```powershell
# From d:\Nidar Project\backend
docker run -it --rm `
  -v ${PWD}:/app `
  -w /app `
  -p 8080:8080 `
  -p 14552:14552/udp `
  --network host `
  maven:3.9.6-eclipse-temurin-17 `
  mvn spring-boot:run
```

**Note**: This won't give you serial port access (COM6), so it's only useful for testing if the backend compiles.

---

## After Installing Maven

Once Maven is installed, you can run the backend natively:

```powershell
cd "d:\Nidar Project\backend"
mvn spring-boot:run
```

This will enable direct COM6 access for real drone control!
