package com.example.howlstagram

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.example.howlstagram.databinding.ActivityMainBinding
import com.example.howlstagram.navigation.*
import com.example.howlstagram.navigation.util.FcmPush
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private var mBinding: ActivityMainBinding? = null
    val binding get() = mBinding!!

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
        setToolbarDefault()
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

                // 화면 전환 될때 uid값 넘겨주기
                var bundle = Bundle()
                var uid = FirebaseAuth.getInstance().currentUser?.uid
                bundle.putString("destinationUid", uid)
                userFragment.arguments = bundle

                supportFragmentManager.beginTransaction().replace(R.id.main_content, userFragment).commit()   // main_content에 detailViewFragment를 띄운다.
                return true
            }
        }
        return false    // 5가지 조건 다 해당 안될때, false 를 리턴 한다.
    }

    fun setToolbarDefault() {
        binding.toolbarUsername.visibility = View.GONE
        binding.toolbarBtnBack.visibility = View.GONE
        binding.toolbarTitleImage.visibility = View.VISIBLE
    }

    // 앱이 백그라운드 처리 됫을때 푸시가 잘오는지 확인 하기 위함. 버튼 대신 만들어놓음
//    override fun onStop() {
//        super.onStop()
//        FcmPush.instance.sendMessage("GQ0nSBojNsM8pCWaufRZogCQeWF3", "hi", "bye")
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Fragment화면을 띄우는 틀이 되는 화면은 MainActivity이기 때문에 여기서 onActivityResult 오버라이드 함수를 만든다.
        if (requestCode == UserFragment.PICK_PROFILE_FROM_ALBUM &&
                resultCode == RESULT_OK) {
            var imageUri = data?.data   // 사진경로(경로에 있는 사진을 꺼내오는것과 같은것)
            var uid = FirebaseAuth.getInstance().currentUser?.uid

            // 사진 서버저장
            var storageRef = FirebaseStorage.getInstance().reference.child("userProfileImages").child(uid!!)    // 파이어베이스 storage에 저장
            storageRef.putFile(imageUri!!).continueWithTask {
                return@continueWithTask storageRef.downloadUrl
            }.addOnSuccessListener {
                var map = HashMap<String, Any>()
                map["image"] = it.toString()
                FirebaseFirestore.getInstance().collection("profileImages").document(uid).set(map)  // 파이어베이스 database에 저장
            }
        }
    }
}