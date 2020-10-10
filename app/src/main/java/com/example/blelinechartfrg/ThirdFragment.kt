package com.example.blelinechartfrg

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class ThirdFragment : Fragment() {

    companion object {
        fun newInstance() = ThirdFragment()
    }

    private lateinit var viewModel: ThirdViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.third_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ThirdViewModel::class.java)
        // TODO: Use the ViewModel
    }

}