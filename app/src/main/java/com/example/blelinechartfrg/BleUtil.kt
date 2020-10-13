package com.example.blelinechartfrg

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.ArrayAdapter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


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

    private val STATE_DISCONNECTED = 0
    private val STATE_CONNECTED = 2
    private var connectionState = STATE_DISCONNECTED
    val TAG = "蓝牙连接"
    var bluetoothGatt: BluetoothGatt? = null
    val serviceUUID: UUID = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB")
    val characteristicUUID: UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805F9B34FB")
    val notify_UUID_chara: UUID = UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB")
    val notify_UUID_service: UUID = UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB")
    var bluetoothGattService: BluetoothGattService? = null
    var bluetoothGattCharacteristic: BluetoothGattCharacteristic? = null
    val readData=ArrayList<String>()

    fun getBluetoothAdapter(): BluetoothAdapter? {
        return bluetoothAdapter
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

    fun connnectGATTServer(context: Context, device: BluetoothDevice?): BluetoothGatt? {
        bluetoothGatt = device?.connectGatt(context, false, gattCallback)
        return bluetoothGatt
    }

    //BLE client(app)读取数据
    open fun readData() {
        val characteristic: BluetoothGattCharacteristic =
            bluetoothGatt!!.getService(serviceUUID).getCharacteristic(characteristicUUID)
        bluetoothGatt!!.readCharacteristic(characteristic)
//        return readData
    }
    //写入数据，参考资料https://www.jianshu.com/p/d991f0fdec63
    //  这里写入数据需要说一下，首先拿到写入的BluetoothGattService和BluetoothGattCharacteristic对象，
    //  把要写入的内容转成16进制的字节（蓝牙BLE规定的数据格式），然后要判断一下字节大小，
    //  如果大于20个字节就要分批次写入了，因为GATT协议规定蓝牙BLE每次传输的有效字节不能超过20个，
    //  最后通过BluetoothGattCharacteristic.setValue(data); mBluetoothGatt.writeCharacteristic(BluetoothGattCharacteristic);
    //  就可以完成写入了。写入成功了会回调onCharacteristicWrite方法

    fun writeData(data: ByteArray) {

        val service: BluetoothGattService? = bluetoothGatt?.getService(serviceUUID)
        val characteristicWrite = service?.getCharacteristic(characteristicUUID)
        bluetoothGatt?.setCharacteristicNotification(characteristicWrite, true)
//        val data: ByteArray = byteArrayOf(11)
        if (data.size > 20) { //数据大于个字节 分批次写入
            Log.e(TAG, "writeData: length=" + data.size)
            var num = 0
            num = if (data.size % 20 != 0) {
                data.size / 20 + 1
            } else {
                data.size / 20
            }
            for (i in 0 until num) {
                var tempArr: ByteArray
                if (i == num - 1) {
                    tempArr = ByteArray(data.size - i * 20)
                    System.arraycopy(data, i * 20, tempArr, 0, data.size - i * 20)
                } else {
                    tempArr = ByteArray(20)
                    System.arraycopy(data, i * 20, tempArr, 0, 20)
                }
                if (characteristicWrite != null) {
                    characteristicWrite.value = tempArr
                }
                bluetoothGatt?.writeCharacteristic(characteristicWrite)
            }
        } else {
            if (characteristicWrite != null) {
                characteristicWrite.value = data
            }
            bluetoothGatt?.writeCharacteristic(characteristicWrite)
        }
    }

    var isConnecting: Boolean = false
    private val gattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        /**
         * 断开或连接 状态发生变化时调用
         */
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            super.onConnectionStateChange(gatt, status, newState)
            Log.d(TAG, "onConnectionStateChange()")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //连接成功
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    Log.d(TAG, "连接成功")
                    //发现服务
                    gatt.discoverServices()
                }
            } else {
                //连接失败
                Log.d(TAG, "失败==$status")
                bluetoothGatt?.close()
                isConnecting = false
            }
        }
        /**
         * 发现设备（真正建立连接）
         */
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            //直到这里才是真正建立了可通信的连接
            isConnecting = false
            Log.d(TAG, "onServicesDiscovered()---建立连接")
            //获取初始化服务和特征值
//            initServiceAndChara()
            //订阅通知
//            bluetoothGatt?.setCharacteristicNotification(
//                bluetoothGatt!!.getService(serviceUUID).getCharacteristic(characteristicUUID), true
//            )
            Log.d(TAG, "getservice:" + bluetoothGatt!!.getService(serviceUUID).toString())
//            Log.d(TAG,
//                "getCharacteristic:" + bluetoothGatt!!.getService(serviceUUID)
//                    .getCharacteristic(characteristicUUID).toString()
//            )
//            bluetoothGatt!!.setCharacteristicNotification(bluetoothGatt!!.getService(notify_UUID_service).getCharacteristic(notify_UUID_chara),true);
//            UiThreadStatement.runOnUiThread(Runnable {
//                bleListView.setVisibility(View.GONE)
//                operaView.setVisibility(View.VISIBLE)
//                tvSerBindStatus.setText("已连接")
//            })
        }

        /**
         * 读操作的回调
         */
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            Log.d(TAG, "onCharacteristicRead()")
            Log.d(TAG, "onCharacteristicRead()+" + characteristic.value[0])
            readData.add(characteristic.value.toString())

        }

        /**
         * 写操作的回调
         */
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Log.d(TAG, "onCharacteristicWrite()  status=$status,value=" + characteristic.value)
        }

        /**
         * 接收到硬件返回的数据
         */
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            Log.d(TAG, "onCharacteristicChanged()" + characteristic.value)
            val data = characteristic.value
//            UiThreadStatement.runOnUiThread(Runnable { addText(tvResponse, bytes2hex(data)) })
        }
    }

}
/**
 * 接收数据线程,低功耗蓝牙传输每次只有20个字节
 */
//class ReadDataThread:Thread()
//{
//    override fun run()
//    {
//        var num:Int=0
//        var byteArray= byteArrayOf()
//        var n:Int=0
//        var readThread:Boolean=true
//        while (true)
//        {
//
//        }
//    }
//}
//thread
//{
//    public void run()
//    {
//
//        int num = 0;
//        byte[] buffer = new byte[1024];
//        byte[] buffer_new = new byte[1024];
//        int n = 0;
//        bRun = true;
//        //接收线程
//        while(true)
//        {
//            try
//            {
//                dataNum = dataCount;
//                while(inputStream.available()==0)
//                {
//                    while(bRun == false){}
//                }
//                while(true)
//                {
//                    num = inputStream.read(buffer);         //读入数据
//                    Log.e("DetectFragment",String.valueOf(buffer));
//                    n=0;
//                    //手机中换行为0a,蓝牙为0d 0a
//                    for(int i=0;i<num;i++)
//                    {
//                        if((buffer[i] == 0x0d)&&(buffer[i+1]==0x0a)){
//                            buffer_new[n] = 0x0a;
//                            i++;
//                        }else{
//                            buffer_new[n] = buffer[i];
//                        }
//                        n++;
//                    }
//                    String s = new String(buffer_new,0,n);
//                    smsg+=s;   //写入接收缓存
//                    MyData = smsg.split("\\|");
//                    dataCount = MyData.length;
//                    if(inputStream.available()==0)break;  //短时间没有数据才跳出进行显示
//                }
//                //发送显示消息，进行显示刷新
//                if (dataNum != dataCount)
//                {
//                    Message message = new Message();
//                    message.what = UPDATE_CHART;
//                    handler.sendMessage(message);
//                }
//            }
//            catch(IOException e)
//            {
//            }
//        }
//    }
//}

