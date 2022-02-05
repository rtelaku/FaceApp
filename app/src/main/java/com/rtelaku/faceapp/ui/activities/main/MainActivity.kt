package com.rtelaku.faceapp.ui.activities.main

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.rtelaku.faceapp.adapters.ViewPagerAdapter
import com.rtelaku.faceapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var adapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        setContentView(binding.root)

        setUpViewPagerAndTabLayout()
    }

    private fun setUpViewPagerAndTabLayout() {
        binding.viewPager.adapter = createViewPagerAdapter()

        TabLayoutMediator(binding.faceAPITabs, binding.viewPager){ tab, position ->
            when(position) {
                0 -> tab.text = "Face Emotions"
                1 -> tab.text = "Face Filter"
            }
        }.attach()
    }

    private fun createViewPagerAdapter() : ViewPagerAdapter {
        adapter = ViewPagerAdapter(this);
        return adapter
    }
}