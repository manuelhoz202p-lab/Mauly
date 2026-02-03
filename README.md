# Maulinet Z - Cliente Hysteria 2 para Android

**Maulinet Z** es una aplicación Android minimalista diseñada para funcionar como cliente de Hysteria 2, utilizando un sistema de autenticación personalizado basado en usuarios de Linux. La aplicación está construida con Jetpack Compose y es compatible con Android 10 (API 29) en adelante.

## Características Principales

*   **Interfaz de Usuario Minimalista**: Sencilla y fácil de usar, con los elementos esenciales para la conexión.
*   **Autenticación por Dispositivo**: Utiliza el `ANDROID_ID` del dispositivo como código de autenticación único.
*   **Integración con Hysteria 2**: Incluye la lógica para ejecutar el binario de Hysteria 2 y configurar un `VpnService`.
*   **Compilación Automática**: Configurado con GitHub Actions para generar el APK automáticamente.

## Estructura del Proyecto

```
MaulinetZ/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── assets/                 # Contendrá el binario de Hysteria 2 (placeholder actual)
│   │   │   ├── java/com/maulinetz/
│   │   │   │   ├── MainActivity.kt     # Interfaz de usuario y lógica principal
│   │   │   │   └── HysteriaVpnService.kt # Servicio VPN y gestión de Hysteria
│   │   │   └── res/
│   │   │       ├── values/
│   │   │       │   ├── strings.xml
│   │   │       │   └── themes.xml
│   │   │       └── xml/
│   │   │           ├── backup_rules.xml
│   │   │           └── data_extraction_rules.xml
│   │   └── build.gradle
├── .github/
│   └── workflows/
│       └── android.yml             # Flujo de GitHub Actions para compilación
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── server_config/
│   ├── auth_script.sh              # Script de autenticación para el servidor
│   └── server.yaml                 # Configuración de ejemplo para el servidor Hysteria 2
├── build.gradle
├── gradlew
├── gradlew.bat
└── settings.gradle
└── README.md
```

## Interfaz de Usuario (Diseño Conceptual)

```
+---------------------------------------+
|                                       |
|             Maulinet Z                |
|                                       |
|  +---------------------------------+  |
|  | Dirección del Servidor (IP:Puerto)|  |
|  | (Campo de texto)                |  |
|  +---------------------------------+  |
|                                       |
|  +---------------------------------+  |
|  |  [Icono Huella]                 |  |
|  |  Código de Autenticación        |  |
|  |  [ANDROID_ID HEXADECIMAL]       |  |
|  +---------------------------------+  |
|                                       |
|  +---------------------------------+  |
|  |                                 |  |
|  |          Desconectado           |  |
|  |                                 |  |
|  +---------------------------------+  |
|                                       |
+---------------------------------------+
```

## Configuración del Servidor Hysteria 2

Para que la aplicación funcione, necesitas un servidor Hysteria 2 configurado con autenticación por comando. Aquí se detalla cómo configurarlo:

1.  **Instalar Hysteria 2**: Sigue las instrucciones oficiales para instalar Hysteria 2 en tu servidor Linux.

2.  **Certificados TLS**: Genera o adquiere certificados TLS válidos para tu dominio. Por ejemplo, con `certbot` o `acme.sh`.

    ```bash
    # Ejemplo de generación de certificados autofirmados (SOLO PARA PRUEBAS)
    openssl genrsa -out server.key 2048
    openssl req -new -x509 -key server.key -out server.crt -days 365 -subj "/CN=your_domain.com"
    ```

3.  **Script de Autenticación (`auth_script.sh`)**:

    Copia el archivo `server_config/auth_script.sh` a una ubicación segura en tu servidor (ej. `/etc/hysteria/auth_script.sh`).

    ```bash
    #!/bin/bash

    AUTH_CODE=$2

    if id "$AUTH_CODE" >/dev/null 2>&1; then
        echo "$AUTH_CODE"
        exit 0
    else
        exit 1
    fi
    ```

    Asegúrate de que el script sea ejecutable:

    ```bash
    sudo chmod +x /etc/hysteria/auth_script.sh
    ```

4.  **Archivo de Configuración (`server.yaml`)**:

    Copia el archivo `server_config/server.yaml` a una ubicación en tu servidor (ej. `/etc/hysteria/server.yaml`). Modifica las rutas de los certificados y el puerto de escucha según sea necesario.

    ```yaml
    listen: :443

tls:
  cert: /etc/hysteria/server.crt
  key: /etc/hysteria/server.key

auth:
  type: command
  command:
    name: /etc/hysteria/auth_script.sh

udp:
  mtu: 1350
    ```

5.  **Iniciar Hysteria 2**: Inicia el servidor Hysteria 2 con tu archivo de configuración.

    ```bash
    /path/to/hysteria server -c /etc/hysteria/server.yaml
    ```

## Uso de la Aplicación

1.  **Obtener el APK**: Sube este proyecto a un repositorio de GitHub. El flujo de GitHub Actions (`.github/workflows/android.yml`) compilará automáticamente el APK. Puedes descargarlo desde la pestaña "Actions" de tu repositorio.

2.  **Instalar la Aplicación**: Instala el APK en tu dispositivo Android.

3.  **Configurar Servidor**: Abre la aplicación. Ingresa la dirección IP y el puerto de tu servidor Hysteria 2 en el campo de texto.

4.  **Copiar Código de Autenticación**: Toca el icono de huella digital en la tarjeta para copiar el `ANDROID_ID` de tu dispositivo al portapapeles.

5.  **Crear Usuario en Linux**: En tu servidor Linux, crea un usuario con el `ANDROID_ID` copiado como nombre de usuario. Por ejemplo:

    ```bash
    sudo useradd -M -N -s /sbin/nologin <ANDROID_ID_HEXADECIMAL>
    # Ejemplo: sudo useradd -M -N -s /sbin/nologin 0123456789abcdef
    ```
    Esto crea un usuario sin directorio home, sin grupo principal y sin shell de inicio de sesión, ideal para propósitos de autenticación.

6.  **Conectar**: Vuelve a la aplicación y presiona el botón "Desconectado". Cambiará a "Conectando..." y luego a "Conectado" si la autenticación es exitosa y la conexión se establece.

7.  **Desconectar**: Presiona el botón "Conectado" para detener la VPN.

## Compilación con GitHub Actions

El archivo `.github/workflows/android.yml` configura un flujo de trabajo que se ejecuta en cada `push` o `pull_request` a las ramas `main` o `master`, o manualmente a través de `workflow_dispatch`.

**Pasos para compilar desde un dispositivo móvil:**

1.  **Crea un Repositorio en GitHub**: Crea un nuevo repositorio vacío en GitHub.
2.  **Sube los Archivos**: Utiliza la interfaz web de GitHub (o una aplicación Git móvil) para subir todos los archivos de este proyecto a tu nuevo repositorio. Asegúrate de que el archivo `.github/workflows/android.yml` esté en su lugar correcto.
3.  **Verifica las Acciones**: Una vez que los archivos estén subidos, ve a la pestaña "Actions" de tu repositorio. Deberías ver un flujo de trabajo llamado "Android CI" ejecutándose.
4.  **Descarga el APK**: Cuando el flujo de trabajo haya terminado exitosamente, haz clic en la ejecución completada. En la sección "Artifacts", encontrarás un archivo ZIP llamado `MaulinetZ-Debug-APK`. Descárgalo, descomprímelo y obtendrás el `app-debug.apk`.

## Consideraciones de Seguridad

*   **Certificados TLS**: Para producción, es **crucial** usar certificados TLS válidos emitidos por una autoridad de certificación reconocida. `insecure: true` en la configuración del cliente es solo para pruebas.
*   **ANDROID_ID**: Ten en cuenta que el `ANDROID_ID` puede cambiar si el dispositivo se restablece de fábrica. Los usuarios deberán volver a registrar su nuevo ID.
*   **Permisos del Script**: Asegúrate de que `auth_script.sh` tenga los permisos adecuados para ser ejecutado por el usuario bajo el cual se ejecuta Hysteria 2, pero no más de lo necesario.
*   **Binario de Hysteria**: El binario de Hysteria 2 debe ser el correcto para la arquitectura de tu dispositivo Android (ej. `hysteria-android-arm64`). Colócalo en la carpeta `app/src/main/assets/` y asegúrate de que tu `HysteriaVpnService.kt` lo copie y le dé permisos de ejecución correctamente (la implementación actual usa un placeholder).

---

**Autor:** Manus AI
**Fecha:** 03 de Febrero de 2026
