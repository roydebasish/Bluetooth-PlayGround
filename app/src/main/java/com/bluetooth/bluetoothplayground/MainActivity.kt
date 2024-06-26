package com.bluetooth.bluetoothplayground

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var bluetoothManager : BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter

    private val REQUEST_BLUETOOTH_PERMISSIONS = 1
    private val REQUEST_LOCATION_PERMISSIONS = 2
    private val REQUEST_BACKGROUND_LOCATION_PERMISSION = 3
    private val REQUEST_ENABLE_BT = 4

    private lateinit var enableBluetoothLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    // Define a ScanCallback to handle Bluetooth scan results
    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            // Handle scan result here
        }
        // ... other callback methods if needed ...
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Declare the three buttons from the layout file
        val btnOn = findViewById<Button>(R.id.btnOn)
        val btnOff = findViewById<Button>(R.id.btnOFF)
        val btnDisc = findViewById<Button>(R.id.btnDiscoverable)

        // Initialize the Bluetooth Adapter
        // Inside your Activity or other Context-aware class
        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        // Register for the Activity Result for enabling Bluetooth
        enableBluetoothLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                // Bluetooth is now enabled, proceed with Bluetooth operations
            } else {
                // Handle the case where the user didn't enable Bluetooth
            }
        }

        // Register for the Activity Result for requesting permissions
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.all { it.value }) {
                // All permissions granted, proceed with Bluetooth operations
                performBluetoothAction(BluetoothAction.ENABLE) // Or the action you want
            } else {
                // Handle the case where some or all permissions were denied
            }
        }


        // Check and request necessary permissions
        checkAndRequestPermissions()

        // Action when Turn ON Button is clicked
        btnOn.setOnClickListener {

            performBluetoothAction(BluetoothAction.ENABLE)

            // If Bluetooth support or API is absent or private in the device
//            enableBluetoothLauncher = registerForActivityResult(
//                ActivityResultContracts.StartActivityForResult()
//            ) { result ->
//                if (result.resultCode == RESULT_OK) {
//                    // Bluetooth is now enabled, proceed with Bluetooth operations
//                } else {
//                    performBluetoothAction(BluetoothAction.ENABLE)
//                    // Handle the case where the user didn't enable Bluetooth
//                }
//            }
        }

        // Action when Turn OFF Button is clicked
        btnOff.setOnClickListener {
            // Disable the Bluetooth Adapter and make a Toast
            performBluetoothAction(BluetoothAction.DISABLE)
            Toast.makeText(applicationContext, "Bluetooth Turned OFF", Toast.LENGTH_SHORT).show()
        }

        // Action when Discoverable Button is clicked
        btnDisc.setOnClickListener {
            // Make the Bluetooth in a Discovering State and make a Toast
//            if (bluetoothAdapter!!.isDiscovering) {
//                performBluetoothAction(BluetoothAction.SCAN)
//                Toast.makeText(applicationContext, "Making Device Discoverable", Toast.LENGTH_SHORT).show()
//            }
        }

    }

    // Function to perform Bluetooth actions (enable, disable, scan)
    private fun performBluetoothAction(action: BluetoothAction) {
        if (hasAllBluetoothPermissions()) {
            // All permissions granted, proceed with Bluetooth action
            when (action) {
                BluetoothAction.ENABLE -> enableBluetooth()
                BluetoothAction.DISABLE -> disableBluetooth()
                BluetoothAction.SCAN -> startScanning()
            }
        } else {
            // Permissions not granted, request them again
            checkAndRequestPermissions()
        }
    }

    private fun enableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
        } else {
            // Bluetooth is already enabled
        }
    }

    private fun disableBluetooth() {
        if (hasAllBluetoothPermissions()) {
            // All permissions granted, proceed with Bluetooth action
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
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
            bluetoothAdapter.takeIf { it.isEnabled }?.disable()
        } else {
            // Permissions not granted, request them again
            checkAndRequestPermissions()
        }
    }


    private fun startScanning() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
        val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner

        if (bluetoothAdapter?.isEnabled == true && bluetoothLeScanner != null) {
            // ... (rest of the startScanning() implementation)
        } else {
            // Handle the case where Bluetooth is not enabled or BluetoothLeScanner is not available
        }
    }


    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Legacy Bluetooth permissions (for devices below Android 12)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADMIN)
            }
        }

        // Bluetooth permissions (Android 12 and above)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        // Location permissions (if needed for Bluetooth scanning)
        if (shouldRequestLocationPermission()) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }

        // Background location permission (Android 10 and above, if needed)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && shouldRequestBackgroundLocationPermission()) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }

        // Request permissions if necessary
        if (permissionsToRequest.isNotEmpty()) {
            val requestCode = if (permissionsToRequest.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                REQUEST_BACKGROUND_LOCATION_PERMISSION
            } else if (permissionsToRequest.any { it.startsWith("android.permission.ACCESS_") }) {
                REQUEST_LOCATION_PERMISSIONS
            } else {
                REQUEST_BLUETOOTH_PERMISSIONS
            }

            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), requestCode)
        } else {
            // All permissions granted, proceed with Bluetooth operations
            enableBluetooth()
        }
    }

    // Function to check if all required Bluetooth permissions are granted
    private fun hasAllBluetoothPermissions(): Boolean {

        val permissionsToCheck: MutableList<String> = mutableListOf()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mutableListOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        }

        // Add legacy permissions for devices below Android 12
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            permissionsToCheck.add(Manifest.permission.BLUETOOTH)
            permissionsToCheck.add(Manifest.permission.BLUETOOTH_ADMIN)
        }

        // Check if your app uses Bluetooth scan results to derive physical location
        if (shouldRequestLocationPermission()) {
            permissionsToCheck.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToCheck.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        // Check if your app needs to scan for Bluetooth devices in the background
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && shouldRequestBackgroundLocationPermission()) {
            permissionsToCheck.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        return permissionsToCheck.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Helper functions to determine if location permissions are needed
    private fun shouldRequestLocationPermission(): Boolean {
        // Check if your app uses Bluetooth scan results to derive physical location
        // Return true if it does, false otherwise
        return true // Replace with your actual logic
    }

    private fun shouldRequestBackgroundLocationPermission(): Boolean {
        // Check if your app needs to scan for Bluetooth devices in the background
        // Return true if it does, false otherwise
        return true // Replace with your actual logic
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_BLUETOOTH_PERMISSIONS -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // Bluetooth permissions granted, proceed
                    checkAndRequestPermissions() // Check for location permissions if needed
                } else {
                    // Handle the case where Bluetooth permissions are denied
                }
            }
            REQUEST_LOCATION_PERMISSIONS -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // Location permissions granted, proceed
                    checkAndRequestPermissions() // Check for background location if needed
                } else {
                    // Handle the case where location permissions are denied
                }
            }
            REQUEST_BACKGROUND_LOCATION_PERMISSION -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // Background location permission granted, proceed
                    enableBluetooth()
                } else {
                    // Handle the case where background location permission is denied
                }
            }
        }
    }

//    private fun checkSelfPermission() : Boolean{
//        var permissionGranted = false
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            val storagePermissionNotGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES ) == PackageManager.PERMISSION_DENIED
//            if (storagePermissionNotGranted) {
//                val permission = arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
//                requestPermissions(permission, READ_PERMISSION_CODE)
//            } else {
//                permissionGranted = true
//            }
//        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
//            val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
//            requestPermissions(permission, READ_PERMISSION_CODE)
//        } else {
//            permissionGranted = true
//        }
//        return permissionGranted
//    }

}


// Enum to represent different Bluetooth actions
enum class BluetoothAction {
    ENABLE, DISABLE, SCAN
}