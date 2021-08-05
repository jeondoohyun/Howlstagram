package com.example.howlstagram.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.howlstagram.R
import com.example.howlstagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM = 0;
    var storage : FirebaseStorage? = null
    var photoUri : Uri? = null
    var auth : FirebaseAuth? = null // firebase storage 생성후에 firestore 할때 만드는것.
    var firestore : FirebaseFirestore? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)

        addphoto_btn_upload.setOnClickListener {
            contentUpload()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_FROM_ALBUM) {
            if (resultCode == Activity.RESULT_OK) {
                // 사진을 선택 했을때 이미지의 경로가 넘어옴
                photoUri = data?.data
                addphoto_image.setImageURI(photoUri)
            } else {
                // 사진을 선택 하지 않고 취소를 눌렀을때
                finish()
            }
        }
    }

    fun contentUpload() {
        // Make filename

        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"  // 사진의 이름이 중복되지않고 고유의 이름이 저장되도록 함. 중복되면 이전 사진이 사라짐

        var storageRef = storage?.reference?.child("images")?.child(imageFileName)    // images(폴더명), imageFileName(파일명)

        // 업로드 방식 2개 있음(Promise method, Callback method)
        // 1. Promise method
//        storageRef?.putFile(photoUri!!)?.continueWithTask {
//            return@continueWithTask storageRef.downloadUrl
//        }?.addOnSuccessListener {
//            var contentDTO = ContentDTO()
//
//            // Insert downloadUrl of image
//            contentDTO.imageUrl = it.toString()
//
//            // Insert uid of user, uid가 뭐지? 뭐에다 쓰는거지?
//            contentDTO.uid = auth?.currentUser?.uid
//
//            // Insert userId
//            contentDTO.userId = auth?.currentUser?.email
//
//            // Insert explain of content
//            contentDTO.explain = addphoto_edit_explain.text.toString()
//
//            // Insert timestamp
//            contentDTO.timestamp = System.currentTimeMillis()
//
//            firestore?.collection("images")?.document()?.set(contentDTO)    // contentDTO 데이터를 images collection안에다 넣는다.
//
//            setResult(Activity.RESULT_OK)
//
//            finish()
//        }

        // addphoto_edit_explain에 입력되는 내용은 왜 firebase storage에 안올라가지?
        // FileUpload
        // 2. Callback 방식
        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener {
                var contentDTO = ContentDTO()

                // Insert downloadUrl of image
                contentDTO.imageUrl = it.toString()

                // Insert uid of user, uid가 뭐지? 뭐에다 쓰는거지?
                contentDTO.uid = auth?.currentUser?.uid

                // Insert userId
                contentDTO.userId = auth?.currentUser?.email

                // Insert explain of content
                contentDTO.explain = addphoto_edit_explain.text.toString()

                // Insert timestamp
                contentDTO.timestamp = System.currentTimeMillis()

                firestore?.collection("images")?.document()?.set(contentDTO)    // contentDTO 데이터를 images collection안에다 넣는다.
                // favoriteCount와 favorites는 데이터 설정을 안했다. firestore에 초기값으로 올라간다.

                setResult(Activity.RESULT_OK)

                finish()
            }
        }
    }
}