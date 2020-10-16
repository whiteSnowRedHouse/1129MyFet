package com.example.blelinechartfrg

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
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

class MainActivity : AppCompatActivity() {

    lateinit var mReceiver: receiver

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
        registerReceiver(mReceiver, filter)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //创建顶部ToolBar菜单
        menuInflater.inflate(R.menu.menu_toolbar, menu)
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
                //通过广播改变title失败，哭唧唧
//                val filter=IntentFilter()
//                filter.addAction("com.example.blelinechartfrg.ACTION_GATT_CONNECTED")
//                val mReceiver=object:BroadcastReceiver() {
//                    override fun onReceive(context: Context?, intent: Intent?) {
//                        item.setTitle("connected")
//                    }
//                }
//                registerReceiver(mReceiver,filter)

            }
        }

        return true
    }

}

class receiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG1, "接收广播")
        when (action) {
            ACTION_GATT_CONNECTED -> {
                Log.d("MainActivity", "GATT连接成功")
//                connected = true
//                updateConnectionState(R.string.connected)
//                (context as? Activity)?.invalidateOptionsMenu()
            }
            ACTION_GATT_DISCONNECTED -> {
                Log.d("MainActivity", "GATT连接失败")
//                connected = false
//                updateConnectionState(R.string.disconnected)
//                (context as? Activity)?.invalidateOptionsMenu()
//                clearUI()
            }
            ACTION_GATT_SERVICES_DISCOVERED -> {
                // Show all the supported services and characteristics on the
//                // user interface.
//                displayGattServices(bluetoothLeService.getSupportedGattServices())
            }
            ACTION_DATA_AVAILABLE -> {
                Log.d(TAG1, intent.getStringExtra("EXTRA_DATA")!!)
//                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA))
            }
        }
    }

}