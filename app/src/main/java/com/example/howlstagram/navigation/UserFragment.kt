package com.example.howlstagram.navigation

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
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
import com.example.howlstagram.navigation.model.FollowDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserFragment : Fragment{
    // 내 페이지화면, 5번째 바텀 네비게이션
    constructor() : super()

    private var mBinding: FragmentUserBinding? = null   // layout xml파일명에 맞게 객체가 자동으로 생성되어 있음
    private val binding get() = mBinding!!

//    var fragmentView : View? = null
    var firestore : FirebaseFirestore? = null
    var uid : String? = null    // 상대방 uid, uid가 계정마다 고유한 값인가??
    var auth : FirebaseAuth? = null
    var currentUserUid : String? = null     // 내 계정 uid

    // static 변수 선언
    companion object {
        var PICK_PROFILE_FROM_ALBUM = 10
    }

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
            mainactivity?.binding?.toolbarUsername?.text = arguments?.getString("userId")
            mainactivity?.binding?.toolbarBtnBack?.setOnClickListener {
                mainactivity.binding.bottomNavigation.selectedItemId = R.id.action_home     // 바텀 네비게이션이 홈이 눌려지게 처리
            }
            mainactivity?.binding?.toolbarTitleImage?.visibility = View.GONE
            mainactivity?.binding?.toolbarUsername?.visibility = View.VISIBLE
            mainactivity?.binding?.toolbarBtnBack?.visibility = View.VISIBLE
            binding.accountBtnFollowSignout?.setOnClickListener {
                requestFollow()
            }
        }

        binding.accountRecyclerview.adapter = UserFragmentRecyclerViewAdapter()
        binding.accountRecyclerview.layoutManager = GridLayoutManager(activity, 3)

        // 프로필 사진 클릭 했을때 사진첩에서 사진 고르기
        binding.accountIvProfile.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            activity?.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM)

        }
        getProfileImage()
        getFollowerAndFollowing()
        return binding.root
    }

    // 팔로우 카운트가 바뀌는 코드
    fun getFollowerAndFollowing() {
        // uid는 내 페이지에서는 내 uid이고 상대방 페이지에서는 상대방 uid이다
        firestore?.collection("users")?.document(uid!!)?.addSnapshotListener { snapshot, error ->
            if (snapshot == null) return@addSnapshotListener
            var followDTO = snapshot.toObject(FollowDTO::class.java)
            if (followDTO?.followingCount != null) {
                binding.accountTvFollowingCount.text = followDTO?.followingCount?.toString()
            }
            if (followDTO?.followerCount != null) {
                binding.accountTvFollowerCount.text = followDTO?.followerCount?.toString()

                if (followDTO?.followers?.containsKey(currentUserUid!!) == true) {
                    binding.accountBtnFollowSignout.text = getString(R.string.follow_cancel)
                    binding.accountBtnFollowSignout.background?.setColorFilter(ContextCompat.getColor(requireActivity(), R.color.colorLightGray), PorterDuff.Mode.MULTIPLY)
                } else {
                    if (uid != currentUserUid) {    // 안정성을 위해 상대방 uid인지 한번더 확인
                        binding.accountBtnFollowSignout.text = getString(R.string.follow)
                        binding.accountBtnFollowSignout.background?.colorFilter = null      // 컬러 삭제
                    }
                }

            }
        }

    }

    fun requestFollow() {
        // 내가 하는 팔로잉
        var tsDocFollowing = firestore?.collection("users")?.document(currentUserUid!!)
        firestore?.runTransaction {
            var followDTO = it.get(tsDocFollowing!!).toObject(FollowDTO::class.java)    // FollowDTO로 캐스팅
            if (followDTO == null) {
                followDTO = FollowDTO()
                followDTO!!.followingCount = 1  // 최초 값이기 때문에 1이다
                followDTO!!.followers[uid!!] = true

                it.set(tsDocFollowing, followDTO)   // 파이어베이스 db로 저장
                return@runTransaction
            }

            if (followDTO.followings.containsKey(uid)) {    // 내가한 팔로잉 목록에 상대방 uid가 있다면 내가 팔로잉했다는거니까 팔로우 취소 버튼을 누르는것.
                // 내가 팔로우 한 상태이기 때문에 팔로잉을 해제
                followDTO?.followingCount = followDTO?.followingCount!! - 1
                followDTO?.followers?.remove(uid)   // 해당 uid를 리스트에서 삭제
            } else {
                // 팔로우 안한 상태라 팔로잉 하는 상태로 변경
                followDTO?.followingCount = followDTO?.followingCount!! + 1
                followDTO?.followers!![uid!!] = true  // 리스트에 해당 uid 키 값에 대해 true로 저장
            }
            it.set(tsDocFollowing, followDTO)
            return@runTransaction
        }

        // 내가 팔로잉한 계정에 접근 하는 코드
        var tsDocFollower = firestore?.collection("users")?.document(uid!!)     // 상대방 uid
        firestore?.runTransaction {
            var followDTO = it.get(tsDocFollower!!).toObject(FollowDTO::class.java)
            if (followDTO == null) {
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1  // 최초 값을 넣는거기 때문에 1이다
                followDTO!!.followers[currentUserUid!!] = true  // 상대방 계정에 나의 uid를 넣는다. 잘 이해가 안된다

                it.set(tsDocFollower, followDTO!!)  // db에 값 넣어주기
                return@runTransaction
            }

            if (followDTO!!.followers.containsKey(currentUserUid)) {
                // 내가 상대방을 팔로우를 했을때이기 때문에 팔로우를 취소하는 코드 작성
                followDTO!!.followerCount = followDTO!!.followerCount - 1
                followDTO!!.followers.remove(currentUserUid!!)  // 나의 uid를 삭제
            } else {
                // 내가 상대방을 팔로우를 안했을때
                followDTO!!.followerCount = followDTO!!.followerCount + 1
                followDTO!!.followers[currentUserUid!!] = true  // 나의 uid를 추가
            }
            it.set(tsDocFollower, followDTO!!)  // db에 값 저장
            return@runTransaction
        }


    }

    // 이미지를 서버에서 받아 오는 함수
    fun getProfileImage() {
        firestore?.collection("profileImages")?.document(uid!!)?.addSnapshotListener { snapshot, error ->
            if (snapshot == null) return@addSnapshotListener
            if (snapshot.data != null) {
                var url = snapshot?.data!!["image"]
                Glide.with(requireActivity()).load(url).apply(RequestOptions().circleCrop()).into(binding.accountIvProfile)
            }
        }
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