package com.example.howlstagram.navigation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.howlstagram.R
import com.example.howlstagram.databinding.ActivityMainBinding
import com.example.howlstagram.databinding.FragmentDetailBinding
import com.example.howlstagram.navigation.model.AlarmDTO
import com.example.howlstagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DetailViewFragment : Fragment{
    // 홈에 팔로우 피드가 올라오는 화면, 1번째 바텀 네비게이션
    constructor() : super()

    var firestore : FirebaseFirestore? = null
    var uid : String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail, container, false)

        // Fragment binding 하는법, layout중에 fragment_detail 바인딩
        val binding = FragmentDetailBinding.inflate(inflater, container, false)

        firestore = FirebaseFirestore.getInstance()

        uid = FirebaseAuth.getInstance().currentUser?.uid       // 게시물을 식별 하는 uid?

        binding.detailviewfragmentRecyclerview.adapter = DetailViewRecyclerViewAdapter()
        binding.detailviewfragmentRecyclerview.layoutManager = LinearLayoutManager(activity)


        return binding.root
    }

    // fragment_detail layout에 있는 recyclerView에서 사용할 어댑터 만들기
    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()     // home화면에 뜨는 모든 계시물?
        var contentUidList : ArrayList<String> = arrayListOf()      // contentDTOs랑 무슨 차이지?

        init {
            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, error ->
                contentDTOs.clear()
                contentUidList.clear()

                if (querySnapshot == null) return@addSnapshotListener
                
                // 서버로 부터 데이터 받기
                for (snapshot in querySnapshot!!.documents) {
                    var item = snapshot.toObject(ContentDTO::class.java)    // 받아온 querySnapshot documents 값을 ContentDTO로 캐스팅
                    contentDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {   // 메모리를 아끼기 위해 커스텀 홀더를 만드는것
            val detailviewitem_profile_textview = view.findViewById<TextView>(R.id.detailviewitem_profile_textview)
            val detailviewitem_profile_image = view.findViewById<ImageView>(R.id.detailviewitem_profile_image)
            val detailviewitem_imageview_content = view.findViewById<ImageView>(R.id.detailviewitem_imageview_content)
            val detailviewitem_favorite_imageview = view.findViewById<ImageView>(R.id.detailviewitem_favorite_imageview)
            val detailviewitem_comment_imageview = view.findViewById<ImageView>(R.id.detailviewitem_comment_imageview)
            val detailviewitem_favoritecounter_textview = view.findViewById<TextView>(R.id.detailviewitem_favoritecounter_textview)
            val detailviewitem_explain_textview = view.findViewById<TextView>(R.id.detailviewitem_explain_textview)

        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewholder = (holder as CustomViewHolder)

            //User Id
            viewholder.detailviewitem_profile_textview.text = contentDTOs!![position].userId

            //Image
            Glide.with(viewholder.itemView.context).load(contentDTOs!![position].imageUrl).into(viewholder.detailviewitem_imageview_content)

            //Explain of content
            viewholder.detailviewitem_explain_textview.text = contentDTOs!![position].explain

            //Likes
            viewholder.detailviewitem_favoritecounter_textview.text = "Likes " + contentDTOs!![position].favoriteCount.toString()

            //ProfileImage
            Glide.with(viewholder.itemView.context).load(contentDTOs!![position].imageUrl).into(viewholder.detailviewitem_profile_image)    // url주소 아직 적용안됨

            // 좋아요 버튼에 이벤트 달기
            viewholder.detailviewitem_favorite_imageview.setOnClickListener {
                favoriteEvent(position)
            }

            // 예전에 내가 눌렀던 좋아요면 화면에 바로 좋아요 이미지가 표시되게끔 처리 해주기
            if (contentDTOs!![position].favorites.containsKey(uid)) {   // favorites 맵 리스트에 uid 게시물이 포함되어 있다는것은 내가 좋아요를 눌렀던 게시물이라는것
                viewholder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite)       // todo : 좋아요 눌렀을때 이미지 바뀌는게 너무 느림
            } else {
                viewholder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite_border)
            }

            // profile image를 클릭 하였을때
            viewholder.detailviewitem_profile_image.setOnClickListener {
                var fragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid", contentDTOs[position].uid)
                bundle.putString("userId", contentDTOs[position].userId)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, fragment)?.commit()
            }

            // 말풍선 아이콘 클릭하면 CommentActivity로 화면 전환
            viewholder.detailviewitem_comment_imageview.setOnClickListener {
                var intent = Intent(it.context, CommentActivity::class.java)
                intent.putExtra("contentUid", contentUidList[position])
                intent.putExtra("destinationUid", contentDTOs[position].uid)
                startActivity(intent)
            }

        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        fun favoriteEvent(position : Int) {
            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])     // 내가 선택한 게시물(컨텐츠)의 uid를 받아와서 좋아요 눌러주는 이벤트

            // 데이터를 불러 오기 위해서는 Transaction을 불러 와야함
            firestore?.runTransaction {
                var contentDTO = it.get(tsDoc!!).toObject(ContentDTO::class.java)   // ContentDTO로 캐스팅

                if (contentDTO!!.favorites.containsKey(uid)) {  // 게시물의 좋아요가 눌러져 있는 상태일경우
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount!! - 1
                    contentDTO?.favorites?.remove(uid)   // 내가 좋아요 누른 게시물의 리스트. 좋아요 해제 한거니까 리스트에서도 해당 uid를 뺀다

                } else {    // 좋아요가 안눌러져 있는경우, 좋아요 카운트가 하나 증가
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount!! + 1
                    contentDTO?.favorites!![uid!!] = true
                    favoriteAlarm(contentDTOs[position].uid!!)  // contentDTO는 바로 위에서 데이터를 받은 객체 이고, contentDTOs는 뭐지?
                }
                it.set(tsDoc, contentDTO)   // 이벤트 완료 한후 transaction을 서버로 다시 보낸다
            }
        }

        fun favoriteAlarm(destinationUid : String) {
            var alarmDTO = AlarmDTO()
            alarmDTO.destinationUid = destinationUid
            alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
            alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
            alarmDTO.kind = 0
            alarmDTO.timeStamp = System.currentTimeMillis()

            // 파이어 스토어에 위에서 세팅한 alarmDTO 데이터값을 넣어준다. alarms라는 컬렉션을 만들어 그 안에 저장한다.
            FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO) //

        }

    }
}