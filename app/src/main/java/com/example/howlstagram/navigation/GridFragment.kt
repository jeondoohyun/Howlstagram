package com.example.howlstagram.navigation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.howlstagram.R
import com.example.howlstagram.databinding.FragmentGridBinding
import com.example.howlstagram.databinding.FragmentUserBinding
import com.example.howlstagram.navigation.model.ContentDTO
import com.google.firebase.firestore.FirebaseFirestore

class GridFragment : Fragment{
    // 2번째 바텀 네비게이션 검색 화면
    constructor() : super()

    private var mBinding: FragmentGridBinding? = null
    private val binding get() = mBinding!!

    var firestore : FirebaseFirestore? = null



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentGridBinding.inflate(inflater, container, false)

        firestore = FirebaseFirestore.getInstance()

        binding.gridfragmentRecyclerview.adapter = UserFragmentRecyclerViewAdapter()
        binding.gridfragmentRecyclerview.layoutManager = GridLayoutManager(activity, 3)

        return binding.root
    }



    // fragment_user layout 의 recyclerView가 사용할 어댑터
    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()

        init {
            // 데이터베이스 값들 읽어 오기, whereEqualTo("uid", uid) 쿼리를 입력하여 내가 입력한 uid에 대한 값만 받아 오도록 함
            firestore?.collection("images")?.addSnapshotListener { querySnapshot, error ->
                if (querySnapshot == null) return@addSnapshotListener   // 안정성을 위해서 스냅샷이 널일경우 바로 리턴

                Log.e("로그확인_1","${querySnapshot.documents.size}")
                // Get data from server
                for (snapshot in querySnapshot.documents) {
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                }
                notifyDataSetChanged()  // 리사이클러뷰 새로고침
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels / 3    // 화면폭 3분의1크기 가져오기

            var imageview = ImageView(parent.context)
            imageview.layoutParams = LinearLayoutCompat.LayoutParams(width, width)
            return CustomViewHolder(imageview)

        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageview = (holder as CustomViewHolder).imageview  // CustomViewHolder로 캐스팅
            Glide.with(holder.imageview.context).load(contentDTOs[position].imageUrl).apply(
                RequestOptions().centerCrop()).into(imageview)
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        inner class CustomViewHolder(var imageview: ImageView) : RecyclerView.ViewHolder(imageview) {   // 파라미터에 var imageview를 상속 받는 ViewHolder(imageview)에 넘겨준다

        }

    }
}