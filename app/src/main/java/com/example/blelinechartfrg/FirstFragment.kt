package com.example.blelinechartfrg

import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment

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

    lateinit var editText1Vg: EditText
    lateinit var editText2Vds: EditText
    lateinit var mBleUtil: BleUtil
    lateinit var mReceiver: receiver

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
        scanButton?.setOnClickListener(this)
        testButton?.setOnClickListener(this)
        saveFileButton?.setOnClickListener(this)

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
}
