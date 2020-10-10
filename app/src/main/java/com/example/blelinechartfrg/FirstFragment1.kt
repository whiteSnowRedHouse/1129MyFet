//package com.example.blelinechartfrg
//
//import android.bluetooth.BluetoothAdapter
//import android.content.Context
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.FragmentActivity
//import androidx.lifecycle.ViewModelProviders
//import kotlinx.android.synthetic.main.first_fragment.*
//
//class FirstFragment : Fragment() {
//
//    companion object {
//        fun newInstance() = FirstFragment()
//    }
//    private lateinit var viewModel: FirstViewModel
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?
//    ): View? {
//        val binding=inflater.inflate(R.layout.first_fragment, container, false)
//        val mBleUtil = getContext()?.let { it1 -> BleUtil(it1) }
//        button.setOnClickListener(View.OnClickListener {
//            //S1：确定蓝牙有没有打开，如果没有弹出串口询问是否打开蓝牙
//            if (mBleUtil != null) {
//                if (mBleUtil.getBluetoothAdapter()!!.isEnabled) {
//                        Toast.makeText(getActivity(),"蓝牙已经打开",Toast.LENGTH_LONG).show()
//                }
//            }
//            if (mBleUtil!!.getBluetoothAdapter() == null || !mBleUtil!!.getBluetoothAdapter()!!.isEnabled) {
//                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//                startActivityForResult(enableBtIntent, 1)
//            }
//        })
//        button2.setOnClickListener {
//            val context: Context
//            mBleUtil!!.scanLeDevice()
//            val builder =
//                getActivity()?.let { it1 ->
//                    getContext()?.let { it2 ->
//                        androidx.appcompat.app.AlertDialog.Builder(it2)
//                            .setTitle(R.string.ScanReult)
//                            .setAdapter(mBleUtil!!.adapter) { dialogInterface, i ->
//                            }.setPositiveButton(R.string.confirm) {    //为dialog添加确认按钮
//                                    dialogInterface, i ->
//                                Log.d("选择2", i.toString())
//                                Log.d("选择", mBleUtil.adapter.getItem(i + 1).toString())
//                                textView.setText("你真棒！")
//                            }.setNegativeButton(R.string.cancel) {//为dialog添加取消按钮
//                                    dialogInterface, i ->
//                            }
//                    }
//                }
//            builder!!.create()
//            builder.show()
//        }
//
//
//            return binding.rootView
//    }
//
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        viewModel = ViewModelProviders.of(this).get(FirstViewModel::class.java)
//        // TODO: Use the ViewModel
//
//        }
//}
//
//
//
