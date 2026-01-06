# PolyWorkApp

Aplicación móvil multiplataforma de gestión laboral construida con Kotlin Multiplatform y Compose Multiplatform.

## 📱 Plataformas

- **Android** (Target SDK 34)
- **iOS** (En desarrollo)

## 🏗️ Arquitectura

- **Kotlin Multiplatform** - Código compartido entre plataformas
- **Compose Multiplatform** - UI declarativa multiplataforma
- **Clean Architecture** - Separación de capas (Domain, Data, Presentation)
- **MVVM** - Patrón de arquitectura para la capa de presentación
- **Ktor Client** - Networking
- **Kotlinx Serialization** - Serialización JSON
- **DataStore** - Persistencia local
- **Coroutines & Flow** - Programación asíncrona

## 📦 Versionamiento

Este proyecto utiliza **Semantic Versioning (SemVer)**: `MAJOR.MINOR.PATCH`

**Versión actual:** `1.0.0` (Build 1)

Para más información sobre cómo gestionar versiones, consulta [VERSIONING.md](./VERSIONING.md)

### Comandos rápidos de versión

```powershell
# Incrementar patch (1.0.0 → 1.0.1)
.\increment-version.ps1 patch
.\sync-version.ps1

# Incrementar minor (1.0.0 → 1.1.0)
.\increment-version.ps1 minor
.\sync-version.ps1

# Crear versión beta
.\increment-version.ps1 minor -Suffix "beta"
.\sync-version.ps1
```

## 🚀 Estructura del Proyecto

* [/composeApp](./composeApp/src) - Código compartido entre plataformas
  - [commonMain](./composeApp/src/commonMain/kotlin) - Código común para todas las plataformas
  - [androidMain](./composeApp/src/androidMain/kotlin) - Código específico de Android
  - [iosMain](./composeApp/src/iosMain/kotlin) - Código específico de iOS

* [/iosApp](./iosApp/iosApp) - Aplicación iOS nativa

## 🛠️ Build and Run Android

```shell
# Windows
.\gradlew.bat :composeApp:assembleDebug

# macOS/Linux
./gradlew :composeApp:assembleDebug
```

## 📱 Build and Run iOS

Abre [/iosApp](./iosApp) en Xcode y ejecuta desde ahí.

## 📚 Recursos

- [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Semantic Versioning](https://semver.org/)

---

**Desarrollado con ❤️ usando Kotlin Multiplatform**

