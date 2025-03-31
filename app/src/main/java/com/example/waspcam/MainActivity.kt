package com.example.waspcam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.waspcam.ui.theme.WaspCamTheme
import android.content.Context
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

fun getConnectedUsbDevices(context: Context): List<UsbDevice> {
    val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    return usbManager.deviceList.values.toList()
//    return usbManager.deviceList.values.filter { device ->
//        // USB Video Class (UVC) devices have a class code of 0x0E (14)
//        device.deviceClass == UsbConstants.USB_CLASS_VIDEO
//    }
}

@Composable
fun usbDeviceListScreen(context: Context): Int {
    val usbDevices = remember { mutableStateOf(getConnectedUsbDevices(context)) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Connected USB Video Devices", style = MaterialTheme.typography.headlineSmall)

        if (usbDevices.value.isEmpty()) {
            Text("No USB video devices found", style = MaterialTheme.typography.bodyMedium)
            return 0
        } else {
            LazyColumn {
                items(usbDevices.value) { device ->
                    UsbDeviceItem(device)
                }
            }
        }
    }
    return 1
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


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
//            WaspCamTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
//            }
            usbDeviceListScreen(this)
        }
    }
}