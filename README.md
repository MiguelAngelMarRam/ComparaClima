# ComparaClima 🚀
**Proyecto Final - Desarrollo de Aplicaciones Multiplataforma (DAM)**

> Herramienta inteligente de toma de decisiones basada en datos climatológicos reales y perfiles de usuario personalizados con persistencia en la nube.

---

## 📝 Presentación del Proyecto
**ComparaClima** es una aplicación móvil nativa para Android diseñada para eliminar la subjetividad al elegir un destino. A diferencia de las apps meteorológicas convencionales, ComparaClima evalúa las condiciones de dos ciudades basándose en los **"Presets"** (gustos personales) del usuario, otorgando una puntuación objetiva del 0 al 100 mediante un algoritmo de decisión propio.

Este proyecto destaca por su arquitectura **Cloud-First**, el consumo de APIs oficiales y un sistema de gestión de errores diseñado para entornos de red variables y alta latencia.

## ✨ Características Principales
- **Algoritmo de Puntuación:** Motor lógico que cruza datos de temperatura, viento, lluvia y estado del cielo con las preferencias del usuario.
- **Persistencia 100% Cloud:** Implementación de **Firebase Realtime Database** para sincronizar Presets e Historial en tiempo real entre dispositivos.
- **Seguridad y Acceso:** Sistema de autenticación mediante **Firebase Auth** con validaciones de seguridad en el lado del cliente.
- **Robustez de Red (Anti-Timeout):** Cliente de red optimizado con **OkHttp** que incluye políticas de reintentos automáticos y *timeouts* extendidos para garantizar el funcionamiento en emuladores y redes inestables.
- **Conexión con AEMET:** Consumo de datos oficiales en tiempo real mediante **AEMET OpenData** (Retrofit 2).
- **Guía Interactiva:** Sistema de ayuda contextual controlado por `SharedPreferences` para facilitar el primer contacto con la app.

## 🛠️ Stack Tecnológico
- **Lenguaje:** Java ☕ (Android Nativo)
- **Backend & DB:** Firebase (Authentication & Realtime Database)
- **Red:** Retrofit 2 + OkHttpClient (Interceptor de reintentos y JSON parsing)
- **Pruebas:** JUnit 4 (**12 Tests unitarios y de integración** con cobertura del 100% en lógica de negocio crítica)
- **Arquitectura:** Estructura modular por paquetes (`activities`, `models`, `network`, `utils`).

## 🚀 Instalación y Uso
1. **Clonar el repositorio:**
   ```bash
   git clone [https://github.com/MiguelAngelMarRam/ComparaClima.git](https://github.com/MiguelAngelMarRam/ComparaClima.git)
