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
    const val VERSION_NAME = "1.0.0"

    /**
     * Código de versión interno (se incrementa con cada build)
     */
    const val VERSION_CODE = 1

    /**
     * Versión mayor (breaking changes)
     */
    const val MAJOR = 1

    /**
     * Versión menor (nuevas features)
     */
    const val MINOR = 0

    /**
     * Versión de parche (bug fixes)
     */
    const val PATCH = 0

    /**
     * Sufijo de versión (e.g., "alpha", "beta", "rc1")
     * Vacío para versiones de producción
     */
    const val SUFFIX = ""

    /**
     * Nombre de la aplicación
     */
    const val APP_NAME = "PolyWorkApp"

    /**
     * Obtiene la versión completa con formato personalizado
     */
    fun getFullVersion(): String {
        return if (SUFFIX.isNotEmpty()) {
            "$MAJOR.$MINOR.$PATCH-$SUFFIX"
        } else {
            "$MAJOR.$MINOR.$PATCH"
        }
    }

    /**
     * Obtiene información detallada de la versión
     */
    fun getVersionInfo(): String {
        return "$APP_NAME v${getFullVersion()} (Build $VERSION_CODE)"
    }

    /**
     * Verifica si es una versión de pre-lanzamiento
     */
    fun isPreRelease(): Boolean {
        return SUFFIX.isNotEmpty()
    }
}

