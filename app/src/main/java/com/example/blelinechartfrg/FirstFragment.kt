package com.example.blelinechartfrg

import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlin.concurrent.thread

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FirstFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
val TAG1 = "Fragment页"

class FirstFragment : Fragment(), View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var bleService: BLEService? = null
    var bluetoothDevice: BluetoothDevice? = null
    lateinit var connectBinder: BLEService.ConnectBinder
    lateinit var scanButton: Button
    lateinit var testButton: Button
    lateinit var saveFileButton: Button
    lateinit var value2: TextView
    lateinit var editText1Vg: EditText
    lateinit var editText2Vds: EditText
    lateinit var mBleUtil: BleUtil
    lateinit var mReceiver: receiver
    lateinit var handler: Handler


    //创建ServiceConnection的匿名内部类
    val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            TODO("Not yet implemented")
        }

        //activity与service绑定成功的时候用
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            connectBinder = service as BLEService.ConnectBinder
            bluetoothDevice?.let { connectBinder.connect(it) }
            bleService = connectBinder.getService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.first_fragment, container, false)
        mBleUtil = context?.let { BleUtil(it) }!!
        scanButton = rootView.findViewById(R.id.button1)
        editText1Vg = rootView.findViewById(R.id.editText1)//获得Vg
        editText2Vds = rootView.findViewById(R.id.editText2)//获得Vds
        testButton = rootView.findViewById(R.id.button2)
        saveFileButton = rootView.findViewById(R.id.button3)
        value2 = rootView.findViewById(R.id.textView4)
        scanButton?.setOnClickListener(this)
        testButton?.setOnClickListener(this)
        saveFileButton?.setOnClickListener(this)
        handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                val bundle: Bundle = msg.data
                if (bundle.getString("value") != null) {
                    Log.d(TAG1, bundle.getString("value")!!)
                    value2.setText(bundle.getString("value"))
                }
            }

        }
        return rootView
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FirstFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onClick(v: View?) {
        if (scanButton == v) {
            mBleUtil!!.scanLeDevice()
            CreatAlertDialog()
            receiveBroadcast()
            Log.d(TAG1 + "bleService.toString()", bleService.toString())
        }
        if (testButton == v) {
            Log.d(TAG1, "testButton被点击了")
            val intent = Intent(context, BLEService::class.java)
            intent.putExtra(
                "SET_DATA",
                editText1Vg.text.toString() + "/" + editText2Vds.text.toString()
            )
            context?.startService(intent)
            Log.d(TAG1, editText1Vg.text.toString() + editText2Vds.text.toString())
        }

    }

    //接收状态连接广播
    private fun receiveBroadcast() {
        mReceiver = receiver()
        val filter = IntentFilter()
        filter.addAction("com.example.blelinechartfrg.ACTION_GATT_CONNECTED")
        filter.addAction("com.example.blelinechartfrg.ACTION_DATA_AVAILABLE")
        filter.addAction("com.example.blelinechartfrg.ACTION_GATT_SERVICES_DISCOVERED")
        filter.addAction("com.example.blelinechartfrg.ACTION_HC42_DISCOVERED")
        filter.addAction("com.example.blelinechartfrg.ACTION_DATA_AVAILABLE")
        context?.registerReceiver(mReceiver, filter)
    }

    private fun openService() {
        Log.d(TAG1, "绑定服务")
        val intent = Intent(context, BLEService::class.java)
        context?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun CreatAlertDialog() {
        val builder = AlertDialog.Builder(getActivity())
        builder.setTitle(R.string.ScanReult)
            .setAdapter(mBleUtil.adapter) { dialogInterface, i ->
                bluetoothDevice = mBleUtil.adapter.getItem(i)!!
                mBleUtil.stopScanDevice()
                Log.d(TAG1, bluetoothDevice.toString())
                openService()
            }
        builder!!.create()
        builder.show()
    }

    inner class receiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            val updateConnect = 1
            val updatDisconnect = 2
            Log.d(TAG1, "接收广播")
            when (action) {
                ACTION_GATT_CONNECTED -> {
                    Log.d("MainActivity", "GATT连接成功")
                }
                ACTION_GATT_DISCONNECTED -> {
                    Log.d("MainActivity", "GATT连接失败")

                }
                ACTION_GATT_SERVICES_DISCOVERED -> {
                    Log.d("MainActivity:", "发现GATT服务")
                    println(
                        "BroadcastReceiver :"
                                + "device SERVICES_DISCOVERED"
                    )
                }
                ACTION_HC42_DISCOVERED -> {
                    Log.d("MainActivity:", "ACTION_HC42_DISCOVERED")
                    Log.d("MainActivity:", intent.extras?.getString("EXTRA_DATA").toString())
                    val intent = Intent(context, BLEService::class.java)
                    intent.putExtra("EXTRA_DATA", "readData")
                    context.startService(intent)

                }
                ACTION_DATA_AVAILABLE -> {
                    Log.d(TAG1, "ACTION_DATA_AVAILABLE")
                    val str = intent.extras?.getString(EXTRA_DATA)
                    Log.d(TAG1, "BroadcastReceiver onData:" + str)
                    thread {
                        val msg = Message()
                        val bundle = Bundle()
                        bundle.putString("value", str)
                        msg.data = bundle
                        handler.sendMessage(msg)
                    }
                    println("BroadcastReceiver onData:")
                }
            }
        }


        private fun displayData(rev_string: String) {
            var rev_str: String = ""
            rev_str += rev_string
            Thread(Runnable {
                println("rev:$rev_str")
            })
        }

    }
}



