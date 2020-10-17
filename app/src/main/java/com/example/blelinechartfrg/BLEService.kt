package com.example.blelinechartfrg

import android.app.Service
import android.bluetooth.*
import android.bluetooth.BluetoothProfile.STATE_CONNECTED
import android.bluetooth.BluetoothProfile.STATE_DISCONNECTED
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import android.util.Log


private const val STATE_DISCONNECTED = 0
private const val STATE_CONNECTING = 1
private const val STATE_CONNECTED = 2
const val ACTION_GATT_CONNECTED = "com.example.blelinechartfrg.ACTION_GATT_CONNECTED"
const val ACTION_GATT_DISCONNECTED = "com.example.blelinechartfrg.ACTION_GATT_DISCONNECTED"
const val ACTION_GATT_SERVICES_DISCOVERED =
    "com.example.blelinechartfrg.ACTION_GATT_SERVICES_DISCOVERED"
const val ACTION_DATA_AVAILABLE = "com.example.blelinechartfrg.ACTION_DATA_AVAILABLE"
const val EXTRA_DATA = "com.example.blelinechartfrg.EXTRA_DATA"
const val UUID_service = "0000ffe0-0000-1000-8000-00805F9B34FB"
const val read_UUID_service = "00002902-0000-1000-8000-00805F9B34FB"
const val write_UUID_service = "0000ffe1-0000-1000-8000-00805F9B34FB"

class BLEService : Service() {
    val TAG = "service服务"
    var bluetoothGatt: BluetoothGatt? = null
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
                            if (characteristics[j].uuid.toString().contains("ae10")) {
                                //  gatt.readCharacteristic(characteristics.get(j)); //读数据，跳到onCharacteristicRead
                                val value = byteArrayOf(1, 2, 3, 4, 5, 6)
                                characteristics[j].value = value
                                gatt.writeCharacteristic(characteristics[j]) //调到onCharacteristicWrite
                            }
                        }
                    }


                }
                else -> Log.w(
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
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "进入onStartCommand1")
        val deviceReceiver = DeviceReceiver()
        val filter = IntentFilter()
        filter.addAction("deviceConnect")
        registerReceiver(deviceReceiver, filter)
        Log.d(TAG, "进入onStartCommand2")
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onCreate() {
        super.onCreate()
//        bluetoothGatt?.getService(UUID_service)?.getCharacteristic(read_UUID_service)
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
        val heartRate = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 1)
        Log.d(TAG, String.format("Received heart rate: %d", heartRate))
        intent.putExtra(EXTRA_DATA, (heartRate).toString())
        sendBroadcast(intent)
    }
}

class DeviceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        Log.d("扫描结果选择", "进入接收广播")
//        val device: String? = intent?.getStringExtra("DEVICECONNECT")
        if (action == "deviceConnect") {
            Log.d("扫描结果选择", action)
        }
    }

}


