# Script bien dich va chay du an Understanding JDBC Core
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectDir

Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host "   DU AN: HIEU RO BAN CHAT COT LOI CUA JDBC (JDBC UNDER THE HOOD)" -ForegroundColor Cyan
Write-Host "=====================================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

Write-Host "[1/2] Dang bien dich ma nguon Java..." -ForegroundColor Yellow
$sources = (Get-ChildItem -Path "src" -Recurse -Filter "*.java").FullName
javac -encoding UTF-8 -cp "lib/*" -d bin $sources

if ($LASTEXITCODE -ne 0) {
    Write-Host "[LOI] Qua trinh bien dich that bai!" -ForegroundColor Red
    Exit $LASTEXITCODE
}

Write-Host "[2/2] Bien dich thanh cong! Dang khoi chay chuong trinh..." -ForegroundColor Green
Write-Host ""

if ($args.Count -gt 0) {
    & java -cp "bin;lib/*" Main $args[0]
} else {
    & java -cp "bin;lib/*" Main
}
