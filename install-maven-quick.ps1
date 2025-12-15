# Quick Maven Installation Script (No Admin Required)
# Run this in PowerShell to set up Maven temporarily

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Quick Maven Setup for NIDAR Backend" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check if Maven is already installed
$mavenInstalled = Get-Command mvn -ErrorAction SilentlyContinue
if ($mavenInstalled) {
    Write-Host "✅ Maven is already installed!" -ForegroundColor Green
    mvn --version
    exit 0
}

# Step 2: Set up Maven directory
$mavenDir = "$env:USERPROFILE\.m2\apache-maven-3.9.6"
$mavenBin = "$mavenDir\bin"

if (Test-Path $mavenDir) {
    Write-Host "✅ Maven already downloaded at $mavenDir" -ForegroundColor Green
} else {
    Write-Host "[1/3] Downloading Maven 3.9.6..." -ForegroundColor Yellow
    
    $downloadUrl = "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
    $zipPath = "$env:TEMP\maven.zip"
    
    try {
        Invoke-WebRequest -Uri $downloadUrl -OutFile $zipPath -UseBasicParsing
        Write-Host "✅ Download complete!" -ForegroundColor Green
    } catch {
        Write-Host "❌ Download failed. Trying alternate mirror..." -ForegroundColor Red
        $downloadUrl = "https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
        Invoke-WebRequest -Uri $downloadUrl -OutFile $zipPath -UseBasicParsing
    }
    
    Write-Host "[2/3] Extracting Maven..." -ForegroundColor Yellow
    Expand-Archive -Path $zipPath -DestinationPath "$env:USERPROFILE\.m2" -Force
    Remove-Item $zipPath
    Write-Host "✅ Maven extracted!" -ForegroundColor Green
}

# Step 3: Add to PATH for current session
Write-Host "[3/3] Adding Maven to PATH..." -ForegroundColor Yellow
$env:Path = "$mavenBin;$env:Path"
$env:MAVEN_HOME = $mavenDir

Write-Host "✅ Maven setup complete!" -ForegroundColor Green
Write-Host ""
Write-Host "Testing Maven installation:" -ForegroundColor White
mvn --version

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Maven is Ready!" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Note: Maven is added to PATH for this PowerShell session only." -ForegroundColor Yellow
Write-Host "If you close this window, you'll need to run this script again." -ForegroundColor Yellow
Write-Host ""
Write-Host "To run NIDAR backend:" -ForegroundColor White
Write-Host "  cd 'd:\Nidar Project\backend'" -ForegroundColor Green
Write-Host "  mvn spring-boot:run" -ForegroundColor Green
Write-Host ""
