package com.example.blelinechartfrg

import android.app.Service
import android.bluetooth.*
import android.bluetooth.BluetoothProfile.STATE_CONNECTED
import android.bluetooth.BluetoothProfile.STATE_DISCONNECTED
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.util.*


private const val STATE_DISCONNECTED = 0
private const val STATE_CONNECTING = 1
private const val STATE_CONNECTED = 2
const val ACTION_GATT_CONNECTED = "com.example.blelinechartfrg.ACTION_GATT_CONNECTED"
const val ACTION_GATT_DISCONNECTED = "com.example.blelinechartfrg.ACTION_GATT_DISCONNECTED"
const val ACTION_GATT_SERVICES_DISCOVERED =
    "com.example.blelinechartfrg.ACTION_GATT_SERVICES_DISCOVERED"
const val ACTION_DATA_AVAILABLE = "com.example.blelinechartfrg.ACTION_DATA_AVAILABLE"
const val ACTION_HC42_DISCOVERED = "com.example.blelinechartfrg.ACTION_HC42_DISCOVERED"
const val EXTRA_DATA = "com.example.blelinechartfrg.EXTRA_DATA"
const val UUID_service = "0000ffe0-0000-1000-8000-00805F9B34FB"
const val read_UUID_service = "00002902-0000-1000-8000-00805F9B34FB"
const val write_UUID_service = "0000ffe1-0000-1000-8000-00805F9B34FB"

class BLEService : Service() {
    val TAG = "service服务"
    var bluetoothGatt: BluetoothGatt? = null
    lateinit var mcharacteristic: BluetoothGattCharacteristic
    private var connectionState = STATE_DISCONNECTED
    val coonectBinder = ConnectBinder()

    //通过继承实现connectBinder类
    inner class ConnectBinder : Binder() {
        fun getService(): BLEService {
            return this@BLEService
        }

        fun connect(device: BluetoothDevice) {
            bluetoothGatt = device.connectGatt(applicationContext, false, gattCallback)
        }

    }

    override fun onBind(intent: Intent): IBinder {
        return coonectBinder
    }

    fun readCharacteristic() {
        bluetoothGatt?.readCharacteristic(mcharacteristic)

    }

    val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            val intentAction: String
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    intentAction = ACTION_GATT_CONNECTED
                    connectionState = STATE_CONNECTED
                    broadcastUpdate(intentAction)
                    Log.d(TAG, "Connected to GATT server.")
                    Log.d(
                        TAG, "Attempting to start service discovery: " +
                                bluetoothGatt?.discoverServices()
                    )
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    intentAction = ACTION_GATT_DISCONNECTED
                    connectionState = STATE_DISCONNECTED
                    Log.d(TAG, "Disconnected from GATT server.")
                    broadcastUpdate(intentAction)
                }
            }
        }

        // New services discovered
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
                    val services = gatt.services
                    Log.d(TAG, "onServicesDiscovered有几个服务:" + services.size)
                    for (i in services.indices) {
                        val characteristics =
                            services[i].characteristics
                        Log.d(
                            TAG,
                            "onServicesDiscovered服务" + i + "的特征数量：" + characteristics.size
                        )
                        for (j in characteristics.indices) {
                            if (characteristics[j].uuid.toString().contains("e1")) {
                                Log.d(
                                    TAG,
                                    "onServicesDiscovered服务" + j + "的特征UUID：" + characteristics.get(
                                        j
                                    ).uuid.toString()
                                )
                                mcharacteristic = characteristics[j]
                                readCharacteristic()
//                                val intent=Intent(ACTION_HC42_DISCOVERED)
//                                intent.putExtra("EXTRA_DATA",characteristics[j].toString())
//                                sendBroadcast(intent)

                            }
                        }
                    }
                }
                else -> Log.d(
                    TAG + "+onServicesDiscovered",
                    "onServicesDiscovered received: $status"
                )
            }
        }

        // Result of a characteristic read operation
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.d(TAG, "进入读回调")
                    Log.d(TAG + "characteristic", characteristic.toString())
                    setNotification(characteristic, true)
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
                }
            }
        }

        fun setNotification(
            characteristic: BluetoothGattCharacteristic,
            enabled: Boolean
        ) {

            bluetoothGatt?.setCharacteristicNotification(characteristic, enabled)
            val clientConfig = characteristic
                .getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
            Log.d(TAG, "setNotification:::::::" + clientConfig.toString())
            if (enabled) {
                clientConfig.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            } else {
                clientConfig.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            }
            bluetoothGatt?.writeDescriptor(clientConfig)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            println("//++++++++++++++++")
            if (characteristic != null) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
            }
        }

    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
        val str: String = intent?.extras?.getString("EXTRA_DATA").toString()
        if (str == "readData") {
            readCharacteristic()
        }

    }


    override fun onCreate() {
        super.onCreate()
    }

    //广播各种蓝牙相关状态,发送广播
    fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        intent.`package` = "com.example.blelinechartfrg"
        intent.putExtra("control", 1)
        applicationContext.sendBroadcast(intent)
        Log.d(TAG, "状态已发送")
    }

    private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic) {
        val intent = Intent(action)
        //从特征值获取数据
        //从特征值获取数据
        val data = characteristic.value
        if (data != null && data.size > 0) {
            val stringBuilder = StringBuilder(data.size)
            for (byteChar in data) {
                stringBuilder.append(String.format("%02X ", byteChar))
                Log.d(
                    TAG,
                    "***broadcastUpdate: byteChar = $byteChar"
                )
            }
            intent.putExtra(EXTRA_DATA, String(data))
            println(
                "broadcastUpdate for  read data:"
                        + String(data)
            )
        }
        sendBroadcast(intent)
    }
}


