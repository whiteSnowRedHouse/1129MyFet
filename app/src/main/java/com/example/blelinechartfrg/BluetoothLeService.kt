package com.example.blelinechartfrg

import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.util.*

class BluetoothLeService : Service() {
    private val mEnabledSensors: List<Sensor> = ArrayList()

    //蓝牙相关类
    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothDeviceAddress: String? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var connectionState = STATE_DISCONNECTED
    private var mOnDataAvailableListener: OnDataAvailableListener? = null

    // Implements callback methods for GATT events that the app cares about. For
    // example,connection change and services discovered.
    interface OnDataAvailableListener {
        fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?, status: Int
        )

        fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        )

        fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        )
    }

    fun setOnDataAvailableListener(l: OnDataAvailableListener?) {
        mOnDataAvailableListener = l
    }

    /* 连接远程设备的回调函数 */
    private val mGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        //断开或连接 状态发生变化时调用
        override fun onConnectionStateChange(
            gatt: BluetoothGatt, status: Int,
            newState: Int
        ) {
            val intentAction: String
            if (newState == BluetoothProfile.STATE_CONNECTED) //连接成功
            {
                intentAction = ACTION_GATT_CONNECTED
                connectionState = STATE_CONNECTED
                /* 通过广播更新连接状态 */
                broadcastUpdate(intentAction)
                Log.i(TAG, "Connected to GATT server.")
                // Attempts to discover services after successful connection.
                Log.i(
                    TAG,
                    "Attempting to start service discovery:" + bluetoothGatt!!.discoverServices()
                )
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) //连接失败
            {
                intentAction = ACTION_GATT_DISCONNECTED
                connectionState = STATE_DISCONNECTED
                Log.i(TAG, "Disconnected from GATT server.")
                broadcastUpdate(intentAction)
            }
        }

        // 重写onServicesDiscovered，发现蓝牙服务
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) //发现到服务
            {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
                Log.i(TAG, "--onServicesDiscovered called--")
            } else {
                Log.w(TAG, "onServicesDiscovered received: $status")
                println("onServicesDiscovered received: $status")
            }
        }

        //特征值的读写
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic, status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "--onCharacteristicRead called--")
                //从特征值读取数据
                val sucString = characteristic.value
                val string = String(sucString)
                //将数据通过广播到Ble_Activity
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
            }
        }

        // 特征值的改变
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            println("++++++++++++++++")
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
        }

        // 特征值的写
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic, status: Int
        ) {
            Log.w(TAG, "--onCharacteristicWrite--: $status")
            // 以下语句实现 发送完数据或也显示到界面上
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        //读描述值
        override fun onDescriptorRead(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor, status: Int
        ) {
            // TODO Auto-generated method stub
            // super.onDescriptorRead(gatt, descriptor, status);
            Log.w(TAG, "----onDescriptorRead status: $status")
            val desc = descriptor.value
            if (desc != null) {
                Log.w(TAG, "----onDescriptorRead value: " + String(desc))
            }
        }

        // 写描述值
        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor, status: Int
        ) {
            // TODO Auto-generated method stub
            // super.onDescriptorWrite(gatt, descriptor, status);
            Log.w(TAG, "--onDescriptorWrite--: $status")
        }

        //读写蓝牙信号值
        override fun onReadRemoteRssi(
            gatt: BluetoothGatt,
            rssi: Int,
            status: Int
        ) {
            // TODO Auto-generated method stub
            // super.onReadRemoteRssi(gatt, rssi, status);
            Log.w(TAG, "--onReadRemoteRssi--: $status")
            broadcastUpdate(ACTION_DATA_AVAILABLE, rssi)
        }

        override fun onReliableWriteCompleted(
            gatt: BluetoothGatt,
            status: Int
        ) {
            // TODO Auto-generated method stub
            // super.onReliableWriteCompleted(gatt, status);
            Log.w(TAG, "--onReliableWriteCompleted--: $status")
        }
    }

    //广播意图
    private fun broadcastUpdate(action: String, rssi: Int) {
        val intent = Intent(action)
        intent.putExtra(
            EXTRA_DATA,
            rssi.toString()
        )
        sendBroadcast(intent)
    }

    //广播意图
    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    /* 广播远程发送过来的数据 */
    fun broadcastUpdate(action: String?, characteristic: BluetoothGattCharacteristic) {
        val intent = Intent(action)
        //从特征值获取数据
        val data = characteristic.value
        if (data != null && data.size > 0) {
            val stringBuilder = StringBuilder(data.size)
            for (byteChar in data) {
                stringBuilder.append(String.format("%02X ", byteChar))
                Log.i(TAG, "***broadcastUpdate: byteChar = $byteChar")
            }
            intent.putExtra(EXTRA_DATA, String(data))
            println("broadcastUpdate for  read data:" + String(data))
        }
        sendBroadcast(intent)
    }

    inner class LocalBinder : Binder() {
        open fun getService():BluetoothDevice {
            return getService()
        }
    }
    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onUnbind(intent: Intent): Boolean {
        close()
        return super.onUnbind(intent)
    }

    private val mBinder: IBinder = LocalBinder()

    /* service 中蓝牙初始化 */
    fun initialize(): Boolean {
        // For API level 18 and above, get a reference to BluetoothAdapter
        // through BluetoothManager.
        if (bluetoothManager == null) {   //获取系统的蓝牙管理器
            bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            if (bluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.")
                return false
            }
        }
        bluetoothAdapter = bluetoothManager!!.adapter
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.")
            return false
        }
        return true
    }

    // 连接远程蓝牙
    fun connect(address: String?): Boolean {
        if (bluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.")
            return false
        }
        // Previously connected device. Try to reconnect.
        if (bluetoothDeviceAddress != null && address == bluetoothDeviceAddress && bluetoothGatt != null
        ) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.")
            return if (bluetoothGatt!!.connect()) //连接蓝牙，其实就是调用BluetoothGatt的连接方法
            {
                connectionState = STATE_CONNECTING
                true
            } else {
                false
            }
        }
        /* 获取远端的蓝牙设备 */
        val device = bluetoothAdapter!!.getRemoteDevice(address)
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.")
            return false
        }
        // We want to directly connect to the device, so we are setting the autoConnect  parameter to false.
        /* 调用device中的connectGatt连接到远程设备 */
        bluetoothGatt = device.connectGatt(this, false, mGattCallback)
        Log.d(TAG, "Trying to create a new connection.")
        bluetoothDeviceAddress = address
        connectionState = STATE_CONNECTING
        println("device.getBondState==" + device.bondState)
        return true
    }

    //取消连接
    fun disconnect() {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        bluetoothGatt!!.disconnect()
    }

    /**
     * After using a given BLE device, the app must call this method to ensure
     * resources are released properly.
     */
    fun close() {
        if (bluetoothGatt == null) {
            return
        }
        bluetoothGatt!!.close()
        bluetoothGatt = null
    }

    /**
     * Request a read on a given `BluetoothGattCharacteristic`. The read result is reported asynchronously through the
     * BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)`
     * callback.
     * The characteristic to read from.
     */
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic?) {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        bluetoothGatt!!.readCharacteristic(characteristic)
    }

    // 写入特征值
    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic?) {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        bluetoothGatt!!.writeCharacteristic(characteristic)
    }

    // 读取RSSi
    fun readRssi() {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        bluetoothGatt!!.readRemoteRssi()
    }

    // Enables or disables notification on a give characteristic. If true, enable notification. False otherwise.
    fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic, enabled: Boolean
    ) {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        bluetoothGatt!!.setCharacteristicNotification(characteristic, enabled)
        val clientConfig = characteristic
            .getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
        if (enabled) {
            clientConfig.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        } else {
            clientConfig.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
        }
        bluetoothGatt!!.writeDescriptor(clientConfig)
    }

    //得到特征值下的描述值
    fun getCharacteristicDescriptor(descriptor: BluetoothGattDescriptor?) {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        bluetoothGatt!!.readDescriptor(descriptor)
    }

    //得到蓝牙的所有服务
    val supportedGattServices: List<BluetoothGattService>?
        get() = if (bluetoothGatt == null) null else bluetoothGatt!!.services

    companion object {
        private const val TAG = "BluetoothLeService" // luetoothLeService.class.getSimpleName();
        private const val STATE_DISCONNECTED = 0
        private const val STATE_CONNECTING = 1
        private const val STATE_CONNECTED = 2
        const val ACTION_GATT_CONNECTED = "com.example.blelinechartfrg.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED =
            "com.example.blelinechartfrg.ACTION_GATT_DISCONNECTED"
        const val ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.blelinechartfrg.ACTION_GATT_SERVICES_DISCOVERED"
        const val ACTION_DATA_AVAILABLE = "com.example.blelinechartfrg.ACTION_DATA_AVAILABLE"
        const val EXTRA_DATA = "com.example.blelinechartfrg.EXTRA_DATA"
    }
}