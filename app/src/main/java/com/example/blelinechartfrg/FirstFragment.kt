package com.example.blelinechartfrg

import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.LineChart

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FirstFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FirstFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.first_fragment, container, false)
        val mBleUtil = getActivity()?.let { BleUtil(it) }
        val textView: TextView = rootView.findViewById(R.id.textView)
        val openBluetoothbutton: Button = rootView.findViewById(R.id.button)//打开蓝牙的按钮
        val scanButton: Button = rootView.findViewById(R.id.button2)//蓝牙扫描按钮
        val connectButton: Button = rootView.findViewById(R.id.button3)//蓝牙扫描按钮
        val lineChart1: LineChart = rootView.findViewById(R.id.lineChart1)//通道1数据
        val lineChart2: LineChart = rootView.findViewById(R.id.lineChart2)//通道2数据
        val readText: TextView = rootView.findViewById(R.id.textView3)//蓝牙读出的数据
        val mBluetoothBService: BluetoothService? = null
        val TAG = "蓝牙service"
        /* BluetoothLeService绑定的回调函数 */
//        val mServiceConnection: ServiceConnection = object : ServiceConnection {
//            override fun onServiceConnected(
//                componentName: ComponentName,
//                service: IBinder
//            ) {
//               mBluetoothBService=get
//                if (!com.example.blelinechartfrg.mBluetoothLeService.initialize()) {
//                    Log.e(
//                        TAG,
//                        "Unable to initialize Bluetooth"
//                    )
//                    finish()
//                }
//                // Automatically connects to the device upon successful start-up
//                // initialization.
//                // 根据蓝牙地址，连接设备
//                com.example.blelinechartfrg.mBluetoothLeService.connect(mDeviceAddress)
//            }
//
//            override fun onServiceDisconnected(componentName: ComponentName) {
//                com.example.blelinechartfrg.mBluetoothLeService = null
//            }
//        }

        var myLineChartData: LineChartView1//创建ViewModel对象
        var x = 0
        var y = 0
        myLineChartData =
            ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get<LineChartView1>(
                LineChartView1::class.java
            )
        getActivity()?.let {
            myLineChartData.getVoltage1().observe(it, Observer { aDouble -> x = aDouble.toInt() })
        }
        getActivity()?.let {
            myLineChartData.getCurrent1().observe(it, Observer { aDouble -> y = aDouble.toInt() })
        }
        lineChart1.setData(myLineChartData.initLineChart())
        lineChart1.invalidate()
        lineChart2.setData(myLineChartData.initLineChart())
        lineChart2.invalidate()

        openBluetoothbutton.setOnClickListener(View.OnClickListener {
            //S1：确定蓝牙有没有打开，如果没有弹出串口询问是否打开蓝牙
            if (mBleUtil != null) {
                if (mBleUtil.getBluetoothAdapter()!!.isEnabled) {
                    Toast.makeText(getActivity(), "蓝牙已经打开", Toast.LENGTH_LONG).show()
                }
            }
            if (mBleUtil!!.getBluetoothAdapter() == null || !mBleUtil!!.getBluetoothAdapter()!!.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, 1)
            }
        })
        var bluetoothDevice: BluetoothDevice
        var bluetoothGatt: BluetoothGatt
        scanButton.setOnClickListener(View.OnClickListener {
            mBleUtil!!.scanLeDevice()
            val builder = AlertDialog.Builder(getActivity())
            builder.setTitle(R.string.ScanReult)
                .setAdapter(mBleUtil.adapter) { dialogInterface, i ->
                    Log.d("选择", i.toString())
                    bluetoothDevice = mBleUtil.adapter.getItem(i)!!
                    bluetoothGatt =
                        activity?.let { it1 -> mBleUtil.connnectGATTServer(it1, bluetoothDevice) }!!
                    if (bluetoothGatt != null) {
                        Toast.makeText(activity, "蓝牙已连接", Toast.LENGTH_LONG).show()
                    }
                }
            builder!!.create()
            builder.show()
        })
        connectButton.setOnClickListener(View.OnClickListener {
            mBleUtil!!.readData()
//            readText.setText(mBleUtil!!.readData().toString())


        })

        return rootView
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FirstFragment.
         */
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
}