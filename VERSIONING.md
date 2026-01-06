# 📦 Sistema de Versionamiento de PolyWorkApp

Este proyecto utiliza **Semantic Versioning (SemVer)** para el control de versiones.

## 📋 Formato de Versión

```
MAJOR.MINOR.PATCH[-SUFFIX]
```

- **MAJOR**: Cambios incompatibles (breaking changes)
- **MINOR**: Nuevas funcionalidades (backward compatible)
- **PATCH**: Correcciones de bugs
- **SUFFIX**: Sufijo para pre-lanzamientos (opcional)
  - `alpha`: Versión en desarrollo temprano
  - `beta`: Versión en pruebas
  - `rc1`, `rc2`: Release candidates

**Ejemplos:**
- `1.0.0` - Versión de producción
- `1.2.3-beta` - Versión beta
- `2.0.0-rc1` - Release candidate

## 🛠️ Archivos de Configuración

### `version.properties`
Archivo principal que contiene la versión actual:
```properties
VERSION_MAJOR=1
VERSION_MINOR=0
VERSION_PATCH=0
VERSION_CODE=1
VERSION_SUFFIX=
```

### `AppVersion.kt`
Objeto Kotlin que expone la información de versión en el código:
```kotlin
AppVersion.VERSION_NAME      // "1.0.0"
AppVersion.VERSION_CODE      // 1
AppVersion.getFullVersion()  // "1.0.0"
AppVersion.getVersionInfo()  // "PolyWorkApp v1.0.0 (Build 1)"
```

## 🚀 Incrementar Versión

### Usando PowerShell Scripts

#### Incrementar versión patch (1.0.0 → 1.0.1)
```powershell
.\increment-version.ps1 patch
.\sync-version.ps1
```

#### Incrementar versión minor (1.0.0 → 1.1.0)
```powershell
.\increment-version.ps1 minor
.\sync-version.ps1
```

#### Incrementar versión major (1.0.0 → 2.0.0)
```powershell
.\increment-version.ps1 major
.\sync-version.ps1
```

#### Incrementar solo build number
```powershell
.\increment-version.ps1 build
.\sync-version.ps1
```

#### Crear versión beta
```powershell
.\increment-version.ps1 minor -Suffix "beta"
.\sync-version.ps1
```

### Manualmente

1. Edita `version.properties`
2. Ejecuta `.\sync-version.ps1` para sincronizar `AppVersion.kt`
3. O actualiza `AppVersion.kt` manualmente

## 📱 Usar la Versión en el Código

```kotlin
import com.konoec.polyworkapp.AppVersion

// Mostrar versión en UI
Text("Versión: ${AppVersion.VERSION_NAME}")

// Información completa
Text(AppVersion.getVersionInfo())

// Verificar si es pre-release
if (AppVersion.isPreRelease()) {
    // Mostrar advertencia de versión en desarrollo
}

// Enviar versión en requests API
val headers = mapOf(
    "App-Version" to AppVersion.VERSION_NAME,
    "Build-Number" to AppVersion.VERSION_CODE.toString()
)
```

## 📝 Workflow Recomendado

### Para desarrollo
```bash
# Nueva feature
.\increment-version.ps1 minor -Suffix "beta"
.\sync-version.ps1

# Commit
git add version.properties composeApp/src/commonMain/kotlin/com/konoec/polyworkapp/AppVersion.kt
git commit -m "chore: bump version to $(cat version.properties | grep VERSION_MAJOR)"
```

### Para bug fixes
```bash
.\increment-version.ps1 patch
.\sync-version.ps1
git add version.properties composeApp/src/commonMain/kotlin/com/konoec/polyworkapp/AppVersion.kt
git commit -m "fix: bump patch version"
```

### Para release
```bash
# Quitar sufijo y hacer release
.\increment-version.ps1 patch -Suffix ""
.\sync-version.ps1
git add version.properties composeApp/src/commonMain/kotlin/com/konoec/polyworkapp/AppVersion.kt
git commit -m "chore: release version 1.0.1"
git tag -a v1.0.1 -m "Release v1.0.1"
git push origin main --tags
```

## 🔄 Sincronización Automática

El archivo `build.gradle.kts` lee automáticamente `version.properties` y configura:
- `versionCode` para Android
- `versionName` para Android

No es necesario modificar `build.gradle.kts` manualmente.

## 📌 Notas Importantes

1. **Siempre ejecuta `sync-version.ps1`** después de modificar `version.properties`
2. **Commit ambos archivos** (`version.properties` y `AppVersion.kt`) juntos
3. **Usa tags de Git** para releases importantes (`git tag -a v1.0.0`)
4. El `VERSION_CODE` debe **siempre incrementarse** para publicar en stores

## 🎯 Ejemplo Completo

```powershell
# Desarrollo de nueva feature
.\increment-version.ps1 minor -Suffix "alpha"
.\sync-version.ps1
# Resultado: 1.1.0-alpha (Build 2)

# Testing
.\increment-version.ps1 build -Suffix "beta"
.\sync-version.ps1
# Resultado: 1.1.0-beta (Build 3)

# Release candidate
.\increment-version.ps1 build -Suffix "rc1"
.\sync-version.ps1
# Resultado: 1.1.0-rc1 (Build 4)

# Release final
.\increment-version.ps1 build -Suffix ""
.\sync-version.ps1
# Resultado: 1.1.0 (Build 5)
```

---

**Última actualización:** 2026-01-06

