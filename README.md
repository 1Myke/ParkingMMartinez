# App Parking LKS - Mikel Martinez

Proyecto de gestión de plazas de aparcamiento para el aula empresa de LKSNext.

## Estado actual del Proyecto
Actualmente en fase de desarrollo de UI y lógica de navegación. Próxima implementación: Tests unitarios y CI/CD con SonarCloud.

## Herramientas en uso
* **Lenguaje:** [Kotlin](https://kotlinlang.org/)
* **UI:** [Jetpack Compose](https://developer.android.com/compose)
* **Navegación:** Compose Navigation
* **Arquitectura:** MVVM (Model-View-ViewModel)

## Estructura de carpetas
Basada en capas para facilitar la escalabilidad:
* `ui`: Componentes visuales y pantallas.
* `model`: Clases de datos y lógica de negocio.
* `data`: (Próximamente) Repositorios y fuentes de datos.

## Cómo empezar
1. Clonar el repositorio.
2. Abrir con Android Studio Ladybug | 2024.2.1 o superior.
3. Sincronizar Gradle y ejecutar en un emulador con API 34+.
