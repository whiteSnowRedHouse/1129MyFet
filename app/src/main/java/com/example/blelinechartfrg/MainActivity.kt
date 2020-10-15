package com.example.blelinechartfrg

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //创建BottomNavigationView，也就是底部导航
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        //创建NavController
//        val nav:NavController=Navigation.findNavController(this,R.id.fragment)
        //底部导航配置
//        val configuration:AppBarConfiguration= AppBarConfiguration.Builder(bottomNavigationView.getMenu()).build()
//        NavigationUI.setupActionBarWithNavController(this,nav,configuration)
//        NavigationUI.setupWithNavController(bottomNavigationView,nav)
// action bar设置
//        val actionBar = getSupportActionBar();

    }

}