package com.maulinetz

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    private val vpnPrepareLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            startVpnService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaulinetZTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        onConnectClick = { serverAddr ->
                            prepareVpn(serverAddr)
                        },
                        onDisconnectClick = {
                            stopVpnService()
                        }
                    )
                }
            }
        }
    }

    private fun prepareVpn(serverAddr: String) {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            vpnPrepareLauncher.launch(intent)
        } else {
            startVpnService(serverAddr)
        }
    }

    private fun startVpnService(serverAddr: String = "") {
        val intent = Intent(this, HysteriaVpnService::class.java).apply {
            action = "START"
            putExtra("SERVER_ADDR", serverAddr)
        }
        startService(intent)
    }

    private fun stopVpnService() {
        val intent = Intent(this, HysteriaVpnService::class.java).apply {
            action = "STOP"
        }
        startService(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onConnectClick: (String) -> Unit, onDisconnectClick: () -> Unit) {
    val context = LocalContext.current
    val androidId = remember { 
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
    }
    
    var serverAddr by remember { mutableStateOf("") }
    var connectionState by remember { mutableStateOf("Desconectado") } // Desconectado, Conectando..., Conectado

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Maulinet Z",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        OutlinedTextField(
            value = serverAddr,
            onValueChange = { serverAddr = it },
            label = { Text("Dirección del Servidor (IP:Puerto)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = connectionState == "Desconectado"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = android.content.ClipData.newPlainText("Auth Code", androidId)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Copy Code",
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = "Código de Autenticación", fontSize = 12.sp)
                    Text(text = androidId, fontWeight = FontWeight.Medium, fontSize = 18.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                if (connectionState == "Desconectado") {
                    if (serverAddr.isNotBlank()) {
                        connectionState = "Conectando..."
                        onConnectClick(serverAddr)
                        // Simulación de cambio de estado para la UI (en producción vendría del servicio)
                        // connectionState = "Conectado" 
                    } else {
                        Toast.makeText(context, "Ingresa una dirección", Toast.LENGTH_SHORT).show()
                    }
                } else if (connectionState == "Conectado") {
                    connectionState = "Desconectado"
                    onDisconnectClick()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = connectionState != "Conectando...",
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(text = connectionState, fontSize = 18.sp)
        }
    }
}

@Composable
fun MaulinetZTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF000000),
            onPrimary = Color.White,
            surfaceVariant = Color(0xFFF0F0F0)
        ),
        content = content
    )
}
