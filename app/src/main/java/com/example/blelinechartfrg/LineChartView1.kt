package com.example.blelinechartfrg

import android.graphics.Color
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

/**
 * @author  白雪红房子
 * @date  2020/10/9 23:31
 * @version 1.0
 */
class LineChartView1: ViewModel() {
    //官方文档：https://developer.android.google.cn/topic/libraries/architecture/viewmodel
    // ViewModel 类旨在以注重生命周期的方式存储和管理界面相关的数据。ViewModel 类让数据可在发生屏幕旋转等配置更改后继续留存。
    //savedInstanceState不适合保存复杂数据，使用ViewModel可以使程序模块化，数据管理和显示分开。
    private var voltage1: MutableLiveData<Double>? = null
    private var current1: MutableLiveData<Double>? = null
    fun getVoltage1(): MutableLiveData<Double> {
        if (voltage1 == null) {
            voltage1 = MutableLiveData()
            voltage1!!.value = 0.toDouble()
        }
        return voltage1!!
    }

    fun getCurrent1(): MutableLiveData<Double> {
        if (current1 == null) {
            current1 = MutableLiveData()
            current1!!.value = 0.toDouble()
        }
        return current1!!
    }

    fun addVoltage1(n: Double) {
        voltage1!!.value = voltage1!!.value!! + n
    }

    fun addCurrent1(n: Double) {
        current1!!.value = current1!!.value!! + n
    }

    fun initLineChart(): LineData {
        val entries = ArrayList<Entry>() //单条线的数据
        for (i in 0..4) {
            entries.add(Entry(1.0F, 2.0F))
            entries.add(Entry(2.0F, 3.0F))
            entries.add(Entry(3.0F, 4.0F))
            entries.add(Entry(4.0F, 5.0F))
        }
        val lineDataSet = LineDataSet(entries, "测试数据") //entries添加到数据集
        lineDataSet.color = Color.BLUE //折线数据集
        lineDataSet.valueTextColor = Color.RED
        val lineData = LineData(lineDataSet) //折线图的数据添加当前数据集
        return lineData
    }
}