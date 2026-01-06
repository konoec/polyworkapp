#!/usr/bin/env pwsh
# Script para sincronizar version.properties con AppVersion.kt

$versionFile = Join-Path $PSScriptRoot "version.properties"
$appVersionFile = Join-Path $PSScriptRoot "composeApp\src\commonMain\kotlin\com\konoec\polyworkapp\AppVersion.kt"

if (-not (Test-Path $versionFile)) {
    Write-Error "No se encuentra el archivo version.properties"
    exit 1
}

# Leer valores del archivo de propiedades
$content = Get-Content $versionFile -Raw
$lines = $content -split "`n"

$versionMajor = 0
$versionMinor = 0
$versionPatch = 0
$versionCode = 0
$versionSuffix = ""

foreach ($line in $lines) {
    if ($line -match "VERSION_MAJOR=(\d+)") { $versionMajor = [int]$matches[1] }
    if ($line -match "VERSION_MINOR=(\d+)") { $versionMinor = [int]$matches[1] }
    if ($line -match "VERSION_PATCH=(\d+)") { $versionPatch = [int]$matches[1] }
    if ($line -match "VERSION_CODE=(\d+)") { $versionCode = [int]$matches[1] }
    if ($line -match "VERSION_SUFFIX=(.*)") { $versionSuffix = $matches[1].Trim() }
}

$versionName = "$versionMajor.$versionMinor.$versionPatch"
if ($versionSuffix) {
    $versionName += "-$versionSuffix"
}

# Generar nuevo contenido de AppVersion.kt
$newAppVersionContent = @"
package com.konoec.polyworkapp

/**
 * Información de versión de la aplicación PolyWorkApp
 *
 * Esta clase proporciona acceso a la información de versión de la app
 * desde cualquier parte del código.
 */
object AppVersion {
    /**
     * Versión completa en formato semántico (e.g., "1.0.0", "1.2.3-beta")
     */
    const val VERSION_NAME = "$versionName"

    /**
     * Código de versión interno (se incrementa con cada build)
     */
    const val VERSION_CODE = $versionCode

    /**
     * Versión mayor (breaking changes)
     */
    const val MAJOR = $versionMajor

    /**
     * Versión menor (nuevas features)
     */
    const val MINOR = $versionMinor

    /**
     * Versión de parche (bug fixes)
     */
    const val PATCH = $versionPatch

    /**
     * Sufijo de versión (e.g., "alpha", "beta", "rc1")
     * Vacío para versiones de producción
     */
    const val SUFFIX = "$versionSuffix"

    /**
     * Nombre de la aplicación
     */
    const val APP_NAME = "PolyWorkApp"

    /**
     * Obtiene la versión completa con formato personalizado
     */
    fun getFullVersion(): String {
        return if (SUFFIX.isNotEmpty()) {
            "`$MAJOR.`$MINOR.`$PATCH-`$SUFFIX"
        } else {
            "`$MAJOR.`$MINOR.`$PATCH"
        }
    }

    /**
     * Obtiene información detallada de la versión
     */
    fun getVersionInfo(): String {
        return "`$APP_NAME v`${getFullVersion()} (Build `$VERSION_CODE)"
    }

    /**
     * Verifica si es una versión de pre-lanzamiento
     */
    fun isPreRelease(): Boolean {
        return SUFFIX.isNotEmpty()
    }
}
"@

Set-Content -Path $appVersionFile -Value $newAppVersionContent -NoNewline

Write-Host "✅ AppVersion.kt sincronizado exitosamente!" -ForegroundColor Green
Write-Host ""
Write-Host "Versión: $versionName (Build $versionCode)" -ForegroundColor Cyan

