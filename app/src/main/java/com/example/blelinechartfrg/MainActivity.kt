package com.example.blelinechartfrg

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    lateinit var mReceiver: receiver
    val connected = 1
    val disconnected = 2
    lateinit var handler: Handler


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //创建BottomNavigationView，也就是底部导航
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        setSupportActionBar(toolbar)
        //创建NavController
        val nav: NavController = Navigation.findNavController(this, R.id.fragment)
        //底部导航配置
        val configuration: AppBarConfiguration =
            AppBarConfiguration.Builder(bottomNavigationView.getMenu()).build()
        NavigationUI.setupActionBarWithNavController(this, nav, configuration)
        NavigationUI.setupWithNavController(bottomNavigationView, nav)
        //接收蓝牙连接成功的广播
//        mReceiver= receiver()


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
        registerReceiver(mReceiver, filter)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //创建顶部ToolBar菜单
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    connected -> {
                        val item: MenuItem = menu!!.findItem(R.id.is_connect)
                        item.setTitle("connected")
                    }
                    disconnected -> {
                        val item: MenuItem = menu!!.findItem(R.id.is_connect)
                        item.setTitle("disconnected")
                    }
                }
            }
        }
        return true


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val bleUtil = BleUtil(this)
        when (item.itemId) {
            //如果蓝牙没有打开，那么打开蓝牙。如果蓝牙已经打开，那么关闭蓝牙
            R.id.bluetooth -> if (bleUtil != null) {
                if (item.title.equals("open_bluetooth")) {
                    Log.d("蓝牙打开情况：", item.title.toString())
                    Log.d("蓝牙打开情况：", item.title.equals("open_bluetooth").toString())
                    //如果蓝牙适配器可以获得，表示蓝牙已经打开，显示蓝牙打开按钮
                    if (bleUtil.getBluetoothAdapter()!!.isEnabled) {
                        item.setIcon(R.drawable.ic_baseline_bluetooth_searching_24)
                        Toast.makeText(this, "蓝牙已经打开", Toast.LENGTH_LONG).show()
                    } else {
                        item.setIcon(R.drawable.ic_baseline_bluetooth_24)
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivityForResult(enableBtIntent, 1)
                        item.setIcon(R.drawable.ic_baseline_bluetooth_searching_24)
                        Toast.makeText(this, "蓝牙已经打开", Toast.LENGTH_LONG).show()
                    }
                    item.setTitle("bluetooth_has_opened")
                } else {
                    bleUtil.closeBluetooth()
                    item.setIcon(R.drawable.ic_baseline_bluetooth_24)
                    item.setTitle("open_bluetooth")
                }
            }
            R.id.is_connect -> {

            }
        }

        return true
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
                    thread {
                        val msg = Message()
                        msg.what = connected
                        handler.sendMessage(msg)
                    }
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
//                println("BroadcastReceiver onData:")
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

