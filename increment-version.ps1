#!/usr/bin/env pwsh
# Script para incrementar la versión de PolyWorkApp
# Uso: .\increment-version.ps1 [major|minor|patch|build]

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("major", "minor", "patch", "build")]
    [string]$Type,

    [string]$Suffix = ""
)

$versionFile = Join-Path $PSScriptRoot "version.properties"

if (-not (Test-Path $versionFile)) {
    Write-Error "No se encuentra el archivo version.properties"
    exit 1
}

# Leer el archivo actual
$content = Get-Content $versionFile -Raw
$lines = $content -split "`n"

$versionMajor = 0
$versionMinor = 0
$versionPatch = 0
$versionCode = 0
$versionSuffix = ""

# Parsear valores actuales
foreach ($line in $lines) {
    if ($line -match "VERSION_MAJOR=(\d+)") { $versionMajor = [int]$matches[1] }
    if ($line -match "VERSION_MINOR=(\d+)") { $versionMinor = [int]$matches[1] }
    if ($line -match "VERSION_PATCH=(\d+)") { $versionPatch = [int]$matches[1] }
    if ($line -match "VERSION_CODE=(\d+)") { $versionCode = [int]$matches[1] }
    if ($line -match "VERSION_SUFFIX=(.*)") { $versionSuffix = $matches[1] }
}

# Incrementar según el tipo
switch ($Type) {
    "major" {
        $versionMajor++
        $versionMinor = 0
        $versionPatch = 0
        $versionCode++
    }
    "minor" {
        $versionMinor++
        $versionPatch = 0
        $versionCode++
    }
    "patch" {
        $versionPatch++
        $versionCode++
    }
    "build" {
        $versionCode++
    }
}

# Aplicar sufijo si se proporciona
if ($Suffix) {
    $versionSuffix = $Suffix
}

# Construir nueva versión
$newVersion = "$versionMajor.$versionMinor.$versionPatch"
if ($versionSuffix) {
    $newVersion += "-$versionSuffix"
}

# Escribir nuevo archivo
$newContent = @"
# Version configuration for PolyWorkApp
# Update these values to release a new version

# Major version: Breaking changes (e.g., 1.x.x -> 2.0.0)
VERSION_MAJOR=$versionMajor

# Minor version: New features, backward compatible (e.g., 1.0.x -> 1.1.0)
VERSION_MINOR=$versionMinor

# Patch version: Bug fixes (e.g., 1.0.0 -> 1.0.1)
VERSION_PATCH=$versionPatch

# Build number (auto-incremented for each build)
VERSION_CODE=$versionCode

# Version suffix for pre-release versions (e.g., -alpha, -beta, -rc1)
# Leave empty for production releases
VERSION_SUFFIX=$versionSuffix
"@

Set-Content -Path $versionFile -Value $newContent -NoNewline

Write-Host "✅ Versión actualizada exitosamente!" -ForegroundColor Green
Write-Host ""
Write-Host "Nueva versión: $newVersion (Build $versionCode)" -ForegroundColor Cyan
Write-Host ""
Write-Host "📝 No olvides actualizar AppVersion.kt manualmente o ejecutar:" -ForegroundColor Yellow
Write-Host "   .\sync-version.ps1" -ForegroundColor Yellow

