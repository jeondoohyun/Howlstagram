package com.example.howlstagram.navigation

import android.content.Intent
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
import com.example.howlstagram.LoginActivity
import com.example.howlstagram.MainActivity
import com.example.howlstagram.R
import com.example.howlstagram.databinding.FragmentUserBinding
import com.example.howlstagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserFragment : Fragment{
    constructor() : super()

    private var mBinding: FragmentUserBinding? = null
    private val binding get() = mBinding!!

//    var fragmentView : View? = null
    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    var auth : FirebaseAuth? = null
    var currentUserUid : String? = null     // 내 계정인지 상대방 계정인지 판단하는 데이터

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_user, container, false)

        // 코틀린 바인딩
        mBinding = FragmentUserBinding.inflate(inflater, container, false)

        uid = arguments?.getString("destinationUid")    // 이전 화면에서 넘어온값을 받는다
        Log.e("로그확인","$uid")
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserUid = auth?.currentUser?.uid

        if(uid == currentUserUid) {     // 같을때는 나의 userFragment페이지
            binding.accountBtnFollowSignout?.text = getString(R.string.signout)
            binding.accountBtnFollowSignout?.setOnClickListener {
                activity?.finish()
                startActivity(Intent(activity, LoginActivity::class.java))
                auth?.signOut()  // 파이어베이스 auth 값에 signout을 전송함
            }

        } else {        // 상대방 userFragment페이지
            binding.accountBtnFollowSignout?.text = getString(R.string.follow)
            var mainactivity = (activity as MainActivity)
            mainactivity?.binding.toolbarUsername?.text = arguments?.getString("userId")
            mainactivity?.binding.toolbarBtnBack?.setOnClickListener {
                mainactivity.binding.bottomNavigation.selectedItemId = R.id.action_home     // 바텀 네비게이션이 홈이 눌려지게 처리
            }
            mainactivity?.binding.toolbarTitleImage?.visibility = View.GONE
            mainactivity?.binding.toolbarUsername?.visibility = View.VISIBLE
            mainactivity?.binding.toolbarBtnBack?.visibility = View.VISIBLE
        }

        binding.accountRecyclerview.adapter = UserFragmentRecyclerViewAdapter()
        binding.accountRecyclerview.layoutManager = GridLayoutManager(activity, 3)

        return binding.root
    }

    // fragment_user layout 의 recyclerView가 사용할 어댑터
    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()

        init {
            // 데이터베이스 값들 읽어 오기, whereEqualTo("uid", uid) 쿼리를 입력하여 내가 입력한 uid에 대한 값만 받아 오도록 함
            firestore?.collection("images")?.whereEqualTo("uid", uid)?.addSnapshotListener { querySnapshot, error ->
                if (querySnapshot == null) return@addSnapshotListener   // 안정성을 위해서 스냅샷이 널일경우 바로 리턴

                Log.e("로그확인_1","${querySnapshot.documents.size}")
                // Get data from server
                for (snapshot in querySnapshot.documents) {
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                }
                binding.accountTvPostCount.text = contentDTOs.size.toString()
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
            Glide.with(holder.imageview.context).load(contentDTOs[position].imageUrl).apply(RequestOptions().centerCrop()).into(imageview)
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        inner class CustomViewHolder(var imageview: ImageView) : RecyclerView.ViewHolder(imageview) {   // 파라미터에 var imageview를 상속 받는 ViewHolder(imageview)에 넘겨준다
            
        }

    }
}