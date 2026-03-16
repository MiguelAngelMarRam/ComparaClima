# ComparaClima 🚀
**Proyecto Final - Desarrollo de Aplicaciones Multiplataforma (DAM)**

> Herramienta inteligente de toma de decisiones basada en datos climatológicos reales y perfiles de usuario personalizados.

---

## 📝 Presentación del Proyecto
**ComparaClima** es una aplicación móvil nativa para Android diseñada para resolver la subjetividad a la hora de elegir un destino. A diferencia de las aplicaciones meteorológicas convencionales, ComparaClima evalúa las condiciones de dos ciudades basándose en los **"Presets"** (gustos personales) del usuario, otorgando una puntuación objetiva del 0 al 100.

Este proyecto integra el consumo de APIs externas, persistencia de datos local, seguridad en el acceso y una interfaz diseñada para la facilidad de uso (UX).

## ✨ Características Principales
- **Algoritmo de Puntuación:** Motor lógico que cruza datos de temperatura, viento y lluvia con las preferencias del usuario.
- **Conexión con AEMET:** Consumo de datos oficiales en tiempo real mediante **AEMET OpenData**.
- **Gestión Multiusuario:** Registro y Login con validación robusta y almacenamiento cifrado de sesiones (`SharedPreferences`).
- **Personalización Total:** Cada usuario gestiona sus propios "Presets" y su historial de consultas de forma privada (SQLite).
- **Guía Interactiva:** Sistema de ayuda contextual que facilita el primer contacto con la app.

## 🛠️ Stack Tecnológico
- **Lenguaje:** Java ☕
- **Entorno:** Android Studio Dolphin / Flamingo
- **Red:** Retrofit 2 (JSON parsing)
- **Base de Datos:** SQLite (Arquitectura de 3 tablas: Usuarios, Presets, Historial)
- **Pruebas:** JUnit 4 (10 Tests unitarios y pruebas de integración)



## 🚀 Instalación y Uso
1. **Clonar el repositorio:**
   ```bash
   git clone [https://github.com/TU_USUARIO/ComparaClima.git](https://github.com/TU_USUARIO/ComparaClima.git)
