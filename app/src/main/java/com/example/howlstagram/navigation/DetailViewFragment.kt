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
import com.bumptech.glide.Glide
import com.example.howlstagram.R
import com.example.howlstagram.databinding.ActivityMainBinding
import com.example.howlstagram.databinding.FragmentDetailBinding
import com.example.howlstagram.navigation.model.ContentDTO
import com.google.firebase.firestore.FirebaseFirestore

class DetailViewFragment : Fragment{
    constructor() : super()

    var firestore : FirebaseFirestore? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail, container, false)

        // Fragment binding 하는법
        val binding = FragmentDetailBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()

        binding.detailviewfragmentRecyclerview.adapter = DetailViewRecyclerViewAdapter()
        binding.detailviewfragmentRecyclerview.layoutManager = LinearLayoutManager(activity)


        return binding.root
    }

    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf()

        init {


            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, error ->
                contentDTOs.clear()
                contentUidList.clear()
                for (snapshot in querySnapshot!!.documents) {
                    var item = snapshot.toObject(ContentDTO::class.java)
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



        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

    }
}