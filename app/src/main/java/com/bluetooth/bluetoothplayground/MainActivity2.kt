package com.bluetooth.bluetoothplayground

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity2 : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val bluetoothLeScanner: BluetoothLeScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }
    private val pairedDevices = mutableListOf<String>()
    private val discoveredDevices = mutableListOf<String>()
    private val scanResults = mutableListOf<ScanResult>()
    private lateinit var txtDevices: TextView
    private lateinit var txtPairedDevices: TextView
    private lateinit var txtAvailableDevices: TextView

//    private val scanCallback = object : ScanCallback() {
//        override fun onScanResult(callbackType: Int, result: ScanResult) {
//            super.onScanResult(callbackType, result)
//            scanResults.add(result)
//            updateDeviceList()
//        }
//
//        override fun onBatchScanResults(results: List<ScanResult>) {
//            super.onBatchScanResults(results)
//            scanResults.addAll(results)
//            updateDeviceList()
//        }
//
//        override fun onScanFailed(errorCode: Int) {
//            super.onScanFailed(errorCode)
//            Toast.makeText(this@MainActivity2, "Scan failed with error: $errorCode", Toast.LENGTH_SHORT).show()
//        }
//    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            scanResults.add(result)
            // Handle each scan result here
        }
    }


    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 2

        private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    private lateinit var enableBluetoothLauncher: ActivityResultLauncher<Intent>

    private fun allPermissionsGranted() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    } else {
        REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        if (!allPermissionsGranted()) {
            requestPermissions()
        }

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        // Check if device supports Bluetooth

//        enableBluetoothLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                // Bluetooth is now enabled
//                startScanning()
//            }
//        }

        enableBluetoothLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled
                updateDeviceLists()
                startScanning()
            }
        }

        val btnTurnOn: Button = findViewById(R.id.btnTurnOn)
        val btnTurnOff: Button = findViewById(R.id.btnTurnOff)
        val btnScan: Button = findViewById(R.id.btnScan)
        val btnStopScan: Button = findViewById(R.id.btnStopScan)
        txtDevices = findViewById(R.id.txtDevices)
        txtPairedDevices = findViewById(R.id.txtPairedDevices)
        txtAvailableDevices = findViewById(R.id.txtAvailableDevices)



        btnTurnOn.setOnClickListener {
            turnBluetoothOn()
        }

        btnTurnOff.setOnClickListener {
            turnBluetoothOff()
        }

        btnScan.setOnClickListener {
            startScanning()
        }

        btnStopScan.setOnClickListener {
            stopScanning()
        }

        // Register broadcast receiver for device discovery
        registerReceiver(deviceDiscoveryReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        registerReceiver(deviceDiscoveryReceiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))


        // Initialize device lists
        updateDeviceLists()
    }

    private fun turnBluetoothOn() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
        }else{
            updateDeviceLists()
            startScanning()
        }
    }

    private fun turnBluetoothOff() {
        if (bluetoothAdapter.isEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    bluetoothAdapter.disable()  // Although deprecated, this is currently the only method.
                } else {
                    Toast.makeText(this, "Bluetooth Connect Permission not granted", Toast.LENGTH_SHORT).show()
                    requestPermissions()
                }
            } else {
                bluetoothAdapter.disable()  // Although deprecated, this is currently the only method.
            }
        }
    }

    private fun disableBluetooth() {
        try {
            val method = BluetoothAdapter::class.java.getMethod("disable")
            method.invoke(bluetoothAdapter)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


//    private fun startScanning() {
//        if (!bluetoothAdapter.isEnabled) {
//            Toast.makeText(this, "Please turn on Bluetooth first", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Bluetooth Scan Permission not granted", Toast.LENGTH_SHORT).show()
//                return
//            }
//        } else {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Location Permission not granted", Toast.LENGTH_SHORT).show()
//                return
//            }
//        }
//
//        scanResults.clear()
//        bluetoothLeScanner.startScan(scanCallback)
//        updateDeviceLists()
//    }
//
//    private fun stopScanning() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Bluetooth Scan Permission not granted", Toast.LENGTH_SHORT).show()
//                return
//            }
//        }
//
//        bluetoothLeScanner.stopScan(scanCallback)
//    }

    private fun startScanning() {
        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(this, "Please turn on Bluetooth first", Toast.LENGTH_SHORT).show()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Bluetooth Scan Permission not granted", Toast.LENGTH_SHORT).show()
                return
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location Permission not granted", Toast.LENGTH_SHORT).show()
                return
            }
        }

        bluetoothAdapter.startDiscovery()
        updateDeviceLists()
    }

    private fun stopScanning() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        bluetoothAdapter.cancelDiscovery()
    }

    private fun updateDeviceLists() {
        // Check for Bluetooth permissions before updating device lists
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Bluetooth Connect Permission not granted", Toast.LENGTH_SHORT).show()
                requestPermissions()
                return
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location Permission not granted", Toast.LENGTH_SHORT).show()
                requestPermissions()
                return
            }
        }

        // Ensure Bluetooth is enabled
        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(this, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show()
            return
        }

        // Clear previous scan results
        scanResults.clear()

        // Start device discovery
        bluetoothAdapter.startDiscovery()

        // Update paired devices
        val pairedDevices = bluetoothAdapter.bondedDevices
        val pairedDeviceNames = pairedDevices.joinToString("\n") { device ->
            "${device.name ?: "Unknown"} - ${device.address}"
        }
        txtPairedDevices.text = "Paired Devices:\n$pairedDeviceNames"

        // Update available (discovered) devices
        val availableDeviceNames = scanResults.joinToString("\n") { result ->
            val device = result.device
            "${device.name ?: "Unknown"} - ${device.address}"
        }
        txtAvailableDevices.text = "Available Devices:\n$availableDeviceNames"
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(deviceDiscoveryReceiver)
    }

    private val deviceDiscoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // A Bluetooth device was found
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        val deviceName = device.name ?: "Unknown"
                        val deviceAddress = device.address
                        val deviceInfo = "$deviceName - $deviceAddress"
                        if (!discoveredDevices.contains(deviceInfo)) {
                            discoveredDevices.add(deviceInfo)
                            updateDiscoveredDevicesTextView()
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    // Discovery has finished
                    Toast.makeText(context, "Bluetooth device discovery finished", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun updateDiscoveredDevicesTextView() {
        txtAvailableDevices.text = "Discovered Devices:\n${discoveredDevices.joinToString("\n")}"
    }

}
