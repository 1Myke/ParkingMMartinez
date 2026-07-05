# Click&Park - Mikel Martinez Diez
Bienvenido a **Click&Park**, una aplicación de gestión de reservas de parking moderna, intuitiva y eficiente, diseñada para optimizar la experiencia de los trabajadores de LKS a la hora de reservar plaza para el Parking de la empresa.
---
> [!warning]
> Para poder utilizar las notificaciones push sigue estos pasos:
> 1. Solicita las API Keys enviando un correo a: **1myke.contactme@gmail.com** (A poder ser desde un correo corporativo)
> 2. Una vez que recibas las claves, dirígete al archivo gradle.properties en la raíz del proyecto.
> 3. Añade las API Keys eliminando los placeholders que hay por defecto

---
## 🏗️ Arquitectura y Estructura del Proyecto
**Click&Park** está desarrollado íntegramente en **Kotlin** y sigue la arquitectura **MVVM (Model-View-ViewModel)** recomendada por Google (y por los profesores del Aula Empresa), garantizando una separación clara de responsabilidades, alta escalabilidad y un código altamente testeable.
### Estructura Real del Proyecto
```
ParkingMMartinez/
├── app/
│   ├── src/
│   │   ├── androidTest/         # Tests Funcionales / E2E (Ej. BookingFlowTest)
│   │   ├── test/                # Tests Unitarios completos (ViewModels, Repositorios, Tests de Estado)
│   │   └── main/java/com/lksnext/ParkingMMartinez/
│   │       ├── data/            # Capa de Datos
│   │       │   ├── receiver/    # Receptores de Sistema / Broadcast Receivers
│   │       │   ├── repository/  # Repositorios (Lógica de negocio e interfacing con Firebase/Local)
│   │       │   └── service/     # Servicios en segundo plano (FCM, OneSignal)
│   │       ├── model/           # Modelos de Dominio y Datos (Data classes)
│   │       └── ui/              # Capa de Presentación (Haciendo uso de Jetpack Compose)
│   │           ├── components/  # Componentes UI reutilizables
│   │           ├── constants/   # Constantes para los TestTag de los componentes
│   │           ├── navigation/  # Navegación del sistema (Navigation Compose)
│   │           ├── screens/     # Pantallas completas de la App
│   │           ├── theme/       # Estilos, colores y tipografía de Material Theme
│   │           └── viewmodel/   # ViewModels que conectan la UI con la capa de datos
├── build.gradle.kts             # Configuración Gradle raíz
├── gradle.properties            # Variables globales y ¡API Keys! (Añadir aqui las API Keys)
├── settings.gradle.kts          # Inclusiones del proyecto
└── README.md                    # Documentación del proyecto
```
---
## 🛠️ Tecnologías y Herramientas Utilizadas
El proyecto utiliza las herramientas más modernas del ecosistema Android:
- 🟢 **Kotlin:** Lenguaje principal del desarrollo.
- 🧩 **Jetpack Componentes:**
    - **Jetpack Compose:** Framework declarativo moderno utilizado 100% para las interfaces de usuario.
    - **ViewModel & StateFlow:** Gestión de los estados de la UI.
    - **Navigation Compose:** Para el enrutamiento visual.
- 🔥 **Firebase:** Completo backend as a service, incluyendo *Firestore* (Base de Datos), *Storage*, *Crashlytics*, *Analytics* y *Auth*.
- 🔔 **OneSignal:** Integración para Push Notifications avanzadas.
- 🖼️ **Coil:** Librería de carga de imágenes optimizada y adaptada a Compose (Para cambiar la foto de perfil, haciendo click en la foto de perfil 😉).
- 🤖 **TensorFlow Lite (litert):** Motor principal de nuestro sistema predictivo local.
- 🧪 **Testing:** JUnit, Mockito, Kotlinx Coroutines Test y Espresso.
---
## 🔔 Notificaciones del sistema y Notificaciones Push
La aplicación incluye un sistema completo de **5 tipos de notificaciones** para mantener al usuario informado en todo momento:
- **3 Notificaciones Locales:** Funciones básicas y recordatorios programados directamente en el dispositivo. Funcionan de forma autónoma sin ninguna configuración adicional.
- **2 Alertas Push (En Segundo Plano):** Te avisan cuando la ocupación del parking llega al 50% de las plazas libres y cuando se libera una plaza de interés. **Para que las 2 alertas en segundo plano funcionen, se requieren API Keys.**

> [!important]
> El resto de la aplicación funciona de forma **perfecta y completa** sin estas llaves, pero si deseas probar todas las notificaciones push, por favor sigue los pasos del warning al inicio de este archivo
---
## 🧠 Chatbot Predictivo (Red Neuronal / IA)
La aplicación integra una **Red Neuronal (usando TensorFlow Lite)** diseñada para simular un *FAQ (Preguntas Frecuentes)* interactivo a través de un chatbot predictivo, todo operando localmente en el dispositivo para garantizar privacidad y respuesta rápida.
### 1. Procesamiento de la consulta
Cuando el usuario envía una duda textual, el sistema la pre-procesa (tokenización, limpieza y *lematización* o normalización) para transformarla en el formato numérico de entrada (Tensores) que espera el modelo.
### 2. Clasificación de la Intención (Intent Classification)
El modelo cargado de forma local (mediante litert) analiza el array probabilístico e infiere la "intención" del usuario con mayor peso relativo. Es capaz de identificar que *"¿A qué hora cierra?"* y *"Horarios del parking"* corresponden al mismo *intent*.
### 3. Selección de la respuesta
Una vez que la red neuronal devuelve un *tag* o intención clasificada, la lógica del sistema selecciona la respuesta óptima de una base de conocimiento o JSON pre-establecido y la despliega al usuario a través del componente UI del chat.
> *Nota de Autor:* Los detalles específicos del modelo final entrenado, pesos estadísticos concretos de la arquitectura y el despliegue del .tflite se podrian seguir refinándose en la carpeta de *assets* entrenando con mas instancias el modelo, actualmente ha sido entrenado con 6545 frases.
---
## 🧪 Testing (Calidad de Código)
La calidad está asegurada gracias a un foco muy estricto en pruebas tanto granulares como sistemáticas usando herramientas reales robustas (**JUnit 4**, **Mockito**, **Coroutines-Test**, **Espresso - Compose UI Tests**).
- ✅ **Test Unitarios (100% en ViewModels y Flujos):** Cobertura exhaustiva en todos los módulos de ui/viewmodel, data/repository, asegurando que BookingViewModel, LoginViewModel, ProfileViewModel, etc., se comporten como debe ante todo rango de respuestas y estados asíncronos.
- 🚀 **Test Funcionales / E2E (Integración):** Contamos con automatizaciones completas desde AndroidTest/ de flujos completos E2E (Ejemplo: BookingFlowTest), cubriendo interacciones físicas reales desde el inicio hasta la reserva exitosa de una plaza.
---
## 🚀 Instalación y Despliegue
Sigue estos rápidos pasos para compilar y ejecutar el proyecto:
1. **Clona el repositorio:**
   `
   git clone https://github.com/1Myke/ParkingMMartinez.git
   `
2. **Abre el Proyecto:** Inicia **Android Studio** y utiliza "Open an existing project" para seleccionar la carpeta raíz del proyecto clonado.
3. **Agrega las API Keys:** Abre el archivo gradle.properties y añade las API Keys (onesignal.app.id y onesignal.rest.api.key) como se indica en la sección inicial.
4. **Sincroniza y Ejecuta:** Espera a que termine la sincronización de *Gradle* automáticamente, selecciona un emulador físico o virtual y presiona el botón ▶️ *(Run 'app')*.
> ¡No pierdas el tiempo, entra en la aplicacion, haz un par de clicks y la reserva para el Parking de la empresa estara hecha! 🚗🅿️
