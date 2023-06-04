package com.example.howlstagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.howlstagram.R
import com.example.howlstagram.databinding.FragmentAlarmBinding
import com.example.howlstagram.databinding.FragmentGridBinding
import com.example.howlstagram.navigation.model.AlarmDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AlarmFragment : Fragment{
    // 좋아요 관련 정보창 4번째 바텀 네비게이션
    constructor() : super()

    private var mBinding: FragmentAlarmBinding? = null
    private val binding get() = mBinding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentAlarmBinding.inflate(inflater, container, false)

        binding.alarmfragmentRecyclerview.adapter = AlarmRecyclerviewAdapter()
        binding.alarmfragmentRecyclerview.layoutManager = LinearLayoutManager(activity)

        return binding.root
    }

    inner class AlarmRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var alarmDTOList : ArrayList<AlarmDTO> = arrayListOf()

        init {
            val uid = FirebaseAuth.getInstance().currentUser?.uid

            // whereEqualTo를 통해서 필터링을하여 원하는 db값만 수신
            FirebaseFirestore.getInstance().collection("alarms").whereEqualTo("destinationUid", uid).addSnapshotListener { querySnapshot, error ->
                alarmDTOList.clear()
                if (querySnapshot == null) return@addSnapshotListener

                for (snapshot in querySnapshot.documents) {
                    alarmDTOList.add(snapshot.toObject(AlarmDTO::class.java)!!)
                }
                notifyDataSetChanged()
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)

            return CustomViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewholder = (holder as CustomViewHolder)

            // 이미지 불러오기, document에 상대방의 uid를 넣으면 상대방의 이미지 경로를 얻을수 있다.
            FirebaseFirestore.getInstance().collection("profileImages").document(alarmDTOList[position].uid!!).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val url = it.result!!["image"]
                    Glide.with(viewholder.itemView.context).load(url).apply(RequestOptions().circleCrop()).into(viewholder.commentviewitem_imageview_profile)
                }
            }

            when (alarmDTOList[position].kind) {
                0 -> {
                    val str_0 = alarmDTOList[position].userId + getString(R.string.alarm_favorite)
                    viewholder.commentviewitem_textview_profile.text = str_0
                }
                1 -> {
                    val str_0 = alarmDTOList[position].userId + " " + getString(R.string.alarm_comment) + " of " + alarmDTOList[position].message
                    viewholder.commentviewitem_textview_profile.text = str_0
                }
                2 -> {
                    val str_0 = alarmDTOList[position].userId + " " + getString(R.string.alarm_follow)
                    viewholder.commentviewitem_textview_profile.text = str_0
                }
            }
            viewholder.commentviewitem_textview_comment.visibility = View.INVISIBLE

        }

        override fun getItemCount(): Int {
            return alarmDTOList.size
        }

        inner class CustomViewHolder(view : View) : RecyclerView.ViewHolder(view) {
            val commentviewitem_imageview_profile = view.findViewById<ImageView>(R.id.commentviewitem_imageview_profile)
            val commentviewitem_textview_profile = view.findViewById<TextView>(R.id.commentviewitem_textview_profile)
            val commentviewitem_textview_comment = view.findViewById<TextView>(R.id.commentviewitem_textview_comment)
        }

    }
}