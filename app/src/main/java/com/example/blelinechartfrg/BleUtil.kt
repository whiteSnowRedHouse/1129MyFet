package com.example.blelinechartfrg

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.util.Log
import android.widget.ArrayAdapter


/**
 * @author  白雪红房子
 * @date  2020/10/9 23:30
 * @version 1.0
 */

class BleUtil(context: Context) {
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    private var mScanning = false
    private val handler = Handler()

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000
    val bluetoothList: ArrayList<BluetoothDevice> = ArrayList()
    val adapter = ArrayAdapter(context, R.layout.listview, R.id.scan_device, bluetoothList)
    val TAG = "蓝牙连接"
    val readData = ArrayList<String>()
    fun getBluetoothAdapter(): BluetoothAdapter? {
        return bluetoothAdapter
    }

    fun closeBluetooth() {
        bluetoothAdapter.disable()
    }

    val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            if (bluetoothList.contains(result.device) == false) {
                adapter.add(result.device)
                adapter.notifyDataSetChanged()
            }
        }
    }

    fun scanLeDevice() {
        if (!mScanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                mScanning = false
                Log.d("蓝牙扫描", "停止扫描")
                bluetoothLeScanner.stopScan(leScanCallback)
            }, SCAN_PERIOD)
            mScanning = true
            Log.d("蓝牙扫描", "扫描开始")
            bluetoothLeScanner.startScan(leScanCallback)
        } else {
            mScanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
        }
    }

    fun stopScanDevice() {
        bluetoothLeScanner.stopScan(leScanCallback)
    }
}


