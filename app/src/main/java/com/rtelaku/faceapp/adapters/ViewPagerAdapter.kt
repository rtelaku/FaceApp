package com.rtelaku.faceapp.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.rtelaku.faceapp.ui.fragments.faceEmotion.FaceEmotionFragment
import com.rtelaku.faceapp.ui.fragments.faceFilter.FaceFilterFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {

        return when(position) {
            FACE_EMOTION_FRAGMENT -> FaceEmotionFragment()
            FACE_FILTER_FRAGMENT -> FaceFilterFragment()
            else -> FaceEmotionFragment()
        }
    }

    override fun getItemCount(): Int {
        return ADAPTER_ITEM_SIZE
    }

    companion object {
        private const val FACE_EMOTION_FRAGMENT = 0
        private const val FACE_FILTER_FRAGMENT = 1
        private const val ADAPTER_ITEM_SIZE = 2
    }
}