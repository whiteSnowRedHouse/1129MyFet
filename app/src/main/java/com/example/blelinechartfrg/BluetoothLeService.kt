//package com.example.blelinechartfrg
//
//import android.bluetooth.*
//import android.util.Log
//import java.util.*
//
///**
// * @author  白雪红房子
// * @date  2020/10/10 20:10
// * @version 1.0
// */
//private val TAG = "蓝牙连接"
//private const val STATE_DISCONNECTED = 0
//private const val STATE_CONNECTING = 1
//private const val STATE_CONNECTED = 2
//const val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
//const val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
//const val ACTION_GATT_SERVICES_DISCOVERED =
//    "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
//const val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
//const val EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"
//val serviceUUID = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB")
//
//// A service that interacts with the BLE device via the Android BLE API.
//class BluetoothLeService(private var bluetoothGatt: BluetoothGatt?) : Service(){
//
//    private var connectionState = STATE_DISCONNECTED
//
//    // Various callback methods defined by the BLE API.
//    private val gattCallback = object : BluetoothGattCallback() {
//        override fun onConnectionStateChange(
//            gatt: BluetoothGatt,
//            status: Int,
//            newState: Int
//        ) {
//            val intentAction: String
//            when (newState) {
//                BluetoothProfile.STATE_CONNECTED -> {
//                    intentAction = ACTION_GATT_CONNECTED
//                    connectionState = STATE_CONNECTED
////                    broadcastUpdate(intentAction)
//                    Log.i(TAG, "Connected to GATT server.")
//                    Log.i(TAG, "Attempting to start service discovery: " +
//                            bluetoothGatt?.discoverServices())
//                }
//                BluetoothProfile.STATE_DISCONNECTED -> {
//                    intentAction = ACTION_GATT_DISCONNECTED
//                    connectionState = STATE_DISCONNECTED
//                    Log.i(TAG, "Disconnected from GATT server.")
////                    broadcastUpdate(intentAction)
//                }
//            }
//        }
//
//        // New services discovered
//        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
//            when (status) {
////                BluetoothGatt.GATT_SUCCESS -> broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
//                else -> Log.w(TAG, "onServicesDiscovered received: $status")
//            }
//        }
//
//        // Result of a characteristic read operation
//        override fun onCharacteristicRead(
//            gatt: BluetoothGatt,
//            characteristic: BluetoothGattCharacteristic,
//            status: Int
//        ) {
//            when (status) {
//                BluetoothGatt.GATT_SUCCESS -> {
////                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
//                }
//            }
//        }
//    }
//}