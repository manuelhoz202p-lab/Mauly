package com.maulinetz

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.provider.Settings
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicReference

class HysteriaVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var hysteriaProcess: Process? = null
    private val serverAddr = AtomicReference<String>("")

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START" -> {
                val addr = intent.getStringExtra("SERVER_ADDR") ?: ""
                serverAddr.set(addr)
                startVpn()
            }
            "STOP" -> {
                stopVpn()
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startVpn() {
        try {
            // 1. Preparar el binario de Hysteria
            val hysteriaBin = prepareHysteriaBinary()
            
            // 2. Generar configuración del cliente
            val configFile = generateClientConfig()

            // 3. Establecer la interfaz VPN
            setupVpnInterface()

            // 4. Ejecutar Hysteria
            // Nota: En una implementación real, se usaría tun2socks para redirigir el tráfico TUN al proxy SOCKS5 de Hysteria
            // Para este ejemplo, mostramos la lógica de ejecución del binario
            val builder = ProcessBuilder(
                hysteriaBin.absolutePath,
                "client",
                "-c", configFile.absolutePath
            )
            builder.directory(filesDir)
            builder.redirectErrorStream(true)
            hysteriaProcess = builder.start()

            Log.i("HysteriaVpn", "Hysteria process started")

        } catch (e: Exception) {
            Log.e("HysteriaVpn", "Error starting VPN", e)
            stopVpn()
        }
    }

    private fun setupVpnInterface() {
        val builder = Builder()
        builder.setSession("Maulinet Z")
        builder.addAddress("10.0.0.2", 24)
        builder.addDnsServer("8.8.8.8")
        builder.addRoute("0.0.0.0", 0)
        
        // Evitar que el propio tráfico de Hysteria pase por la VPN (bucle infinito)
        // builder.addDisallowedApplication(packageName) 
        
        vpnInterface = builder.establish()
    }

    private fun prepareHysteriaBinary(): File {
        val binFile = File(filesDir, "hysteria")
        if (!binFile.exists()) {
            // En un entorno real, copiaríamos desde assets
            // assets.open("hysteria-android-arm64").use { input ->
            //     FileOutputStream(binFile).use { output -> input.copyTo(output) }
            // }
            binFile.createNewFile() // Placeholder
        }
        binFile.setExecutable(true)
        return binFile
    }

    private fun generateClientConfig(): File {
        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val configContent = """
            server: ${serverAddr.get()}
            auth: $androidId
            transport: udp
            tls:
              sni: google.com
              insecure: true
            socks5:
              listen: 127.0.0.1:1080
            bandwidth:
              up: 10 mbps
              down: 50 mbps
        """.trimIndent()

        val configFile = File(filesDir, "client.yaml")
        configFile.writeText(configContent)
        return configFile
    }

    private fun stopVpn() {
        hysteriaProcess?.destroy()
        hysteriaProcess = null
        vpnInterface?.close()
        vpnInterface = null
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }
}
