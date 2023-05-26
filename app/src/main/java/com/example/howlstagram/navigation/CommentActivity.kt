package com.example.howlstagram.navigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.howlstagram.R
import com.example.howlstagram.databinding.ActivityCommentBinding
import com.example.howlstagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CommentActivity : AppCompatActivity() {

    private var mBinding : ActivityCommentBinding? = null
    private val binding get() = mBinding!!

    var contentUid : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        contentUid = intent.getStringExtra("contentUid")

        binding.commentBtnSend.setOnClickListener {
            var comment = ContentDTO.Comment()
            comment.userId = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.comment = binding.commentEditMessage.text.toString()    // 입력한 댓글 내용
            comment.timestamp = System.currentTimeMillis()      //현재시간

            // db에 넣기
            FirebaseFirestore.getInstance().collection("images").document(contentUid!!).collection("comments").document().set(comment)

            binding.commentEditMessage.setText("")
        }
    }
}