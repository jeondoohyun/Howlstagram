package com.example.howlstagram.navigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.howlstagram.R
import com.example.howlstagram.databinding.ActivityCommentBinding
import com.example.howlstagram.navigation.model.AlarmDTO
import com.example.howlstagram.navigation.model.ContentDTO
import com.example.howlstagram.navigation.util.FcmPush
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CommentActivity : AppCompatActivity() {

    private var mBinding : ActivityCommentBinding? = null
    private val binding get() = mBinding!!

    var contentUid : String? = null
    var destinationUid : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        contentUid = intent.getStringExtra("contentUid")
        destinationUid = intent.getStringExtra("destinationUid")

        binding.commentRecyclerview.adapter = CommentRecyclerviewAdapter()
        binding.commentRecyclerview.layoutManager = LinearLayoutManager(this)

        binding.commentBtnSend.setOnClickListener {
            var comment = ContentDTO.Comment()
            comment.userId = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.comment = binding.commentEditMessage.text.toString()    // 입력한 댓글 내용
            comment.timestamp = System.currentTimeMillis()      //현재시간

            // db에 넣기
            FirebaseFirestore.getInstance().collection("images").document(contentUid!!).collection("comments").document().set(comment)  // 파이어베이스 데이터베이스에 images 콜렉션 안에 contentUid로 된 다큐먼트안에 또다시 comments라는 콜렉션생성

            binding.commentEditMessage.setText("")

            commentAlarm(destinationUid!!, binding.commentEditMessage.text.toString())
        }
    }

    fun commentAlarm(destinationUid : String, message : String) {
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
        alarmDTO.kind = 1
        alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
        alarmDTO.timeStamp = System.currentTimeMillis()
        alarmDTO.message = message

        // 파이어베이스에 위에서 세팅한 alarmDTO 객체 저장하기
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

        // fcm 전송
        var msg = FirebaseAuth.getInstance().currentUser?.email + getString(R.string.alarm_comment) + " of" + message
        FcmPush.instance.sendMessage(destinationUid, "Howlsta", msg)

    }

    inner class CommentRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var comments : ArrayList<ContentDTO.Comment> = arrayListOf()
        init {
            // init에서 db 데이터 받아오기
            FirebaseFirestore.getInstance()
                .collection("images")
                .document(contentUid!!)
                .collection("comments")
                .orderBy("timestamp")       // 순서대로 나열
                .addSnapshotListener { querySnapshot, error ->
                    comments.clear()
                    if (querySnapshot == null) return@addSnapshotListener
                    
                    for (snapshot in querySnapshot.documents!!) {
                        comments.add(snapshot.toObject(ContentDTO.Comment::class.java)!!)
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
            return CustomViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = holder as CustomViewHolder

            view.commentviewitem_textview_comment.text = comments[position].comment
            view.commentviewitem_textview_profile.text = comments[position].userId

            FirebaseFirestore.getInstance()
                .collection("profileImages")
                .document(comments[position].uid!!)     // 코멘트를 입력한 프로필사진의 주소가 넘어옴
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        var url = it.result!!["image"]
                        Glide.with(holder.itemView.context).load(url).apply(RequestOptions().circleCrop()).into(view.commentviewitem_imageview_profile)
                    }
                }
        }

        override fun getItemCount(): Int {
            return comments.size
        }

        private inner class CustomViewHolder(view : View) : RecyclerView.ViewHolder(view) {    // view를 상속받은 RecyclerView.ViewHolder()에 넣은것
            val commentviewitem_imageview_profile = view.findViewById<ImageView>(R.id.commentviewitem_imageview_profile)
            val commentviewitem_textview_profile = view.findViewById<TextView>(R.id.commentviewitem_textview_profile)
            val commentviewitem_textview_comment = view.findViewById<TextView>(R.id.commentviewitem_textview_comment)

        }




    }
}