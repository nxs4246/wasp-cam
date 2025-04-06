package com.example.waspcam

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.waspcam.ui.theme.WaspCamTheme

private const val ACTION_USB_PERMISSION = "com.example.waspcam.USB_PERMISSION"
private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    private lateinit var usbManager: UsbManager
    private lateinit var permissionIntent: PendingIntent
    private val usbDevices = mutableStateOf<List<UsbDevice>>(emptyList())

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_USB_PERMISSION == intent.action) {
                synchronized(this) {
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.apply {
                            Log.d(TAG, "Permission granted for device: $deviceName")
                            // could set up device communication here
                        }
                    } else {
                        Log.d(TAG, "Permission denied for device: $device")
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED == intent.action) {
                // refresh the list when a new device is attached automatically
                refreshUsbDeviceList()
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED == intent.action) {
                // refresh the list when a device is detached automatically
                refreshUsbDeviceList()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        permissionIntent = PendingIntent.getBroadcast(this, 0,
            Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE)

        val filter = IntentFilter(ACTION_USB_PERMISSION)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 and higher
            registerReceiver(usbReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            // Older versions of Android
            registerReceiver(usbReceiver, filter)
        }

        // initial fetch
        refreshUsbDeviceList()

        setContent {
            WaspCamTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    UsbDeviceListScreen(
                        context = this@MainActivity,
                        usbDevices = usbDevices.value,
                        onRefresh = { refreshUsbDeviceList() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun refreshUsbDeviceList() {
        val connectedDevices: List<UsbDevice> = usbManager.deviceList.values.toList()
        usbDevices.value = connectedDevices
        // request permission for each connected device
        connectedDevices.forEach { device ->
            usbManager.requestPermission(device, permissionIntent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbReceiver)
    }
}

@Composable
fun UsbDeviceListScreen(
    context: Context,
    usbDevices: List<UsbDevice>,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text("Connected USB Devices", style = MaterialTheme.typography.headlineSmall)

        // this button is not used now because device refresh is set automatically
        Button(onClick = onRefresh, modifier = Modifier.padding(vertical = 8.dp)) {
            Text("Refresh USB Devices")
        }

        if (usbDevices.isEmpty()) {
            Text("No USB devices found", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn {
                items(usbDevices) { device ->
                    UsbDeviceItem(device)
                }
            }
        }
    }
}

@Composable
fun UsbDeviceItem(device: UsbDevice) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Device Name: ${device.deviceName}", fontWeight = FontWeight.Bold)
            Text("Vendor ID: ${device.vendorId}")
            Text("Product ID: ${device.productId}")
        }
    }
}