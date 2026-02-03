#!/bin/bash

# Hysteria 2 Command Authentication Script
# Argumentos recibidos de Hysteria:
# $1: addr (IP del cliente)
# $2: auth (Código hexadecimal / ANDROID_ID)
# $3: tx (Bytes transmitidos)

AUTH_CODE=$2

# Verificar si el código de autenticación (que usamos como nombre de usuario) existe en el sistema
if id "$AUTH_CODE" >/dev/null 2>&1; then
    # El usuario existe, permitir conexión
    # Imprimimos el nombre del usuario para los logs de Hysteria
    echo "$AUTH_CODE"
    exit 0
else
    # El usuario no existe, rechazar conexión
    exit 1
fi
