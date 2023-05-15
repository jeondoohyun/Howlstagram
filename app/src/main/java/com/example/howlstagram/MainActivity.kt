package com.example.howlstagram

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.example.howlstagram.databinding.ActivityMainBinding
import com.example.howlstagram.navigation.*
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private var mBinding: ActivityMainBinding? = null
    private val binding get() = mBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(R.layout.activity_main)
        setContentView(binding.root)
        binding.bottomNavigation.setOnNavigationItemSelectedListener(this)

        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)   // 권한 요청

        //DetailView fragment 설정
        binding.bottomNavigation.selectedItemId = R.id.action_home
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_home -> {
                var detailViewFragment = DetailViewFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, detailViewFragment).commit()   // main_content에 detailViewFragment를 띄운다.
                return true
            }
            R.id.action_search -> {
                var gridFragment = GridFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, gridFragment).commit()   // main_content에 detailViewFragment를 띄운다.
                return true
            }
            R.id.action_add_photo -> {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(Intent(this, AddPhotoActivity::class.java))
                }
                return true
            }
            R.id.action_favorite_alarm -> {
                var alarmFragment = AlarmFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, alarmFragment).commit()   // main_content에 detailViewFragment를 띄운다.
                return true
            }
            R.id.action_account -> {
                var userFragment = UserFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, userFragment).commit()   // main_content에 detailViewFragment를 띄운다.
                return true
            }
        }
        return false    // 5가지 조건 다 해당 안될때, false 를 리턴 한다.
    }
}