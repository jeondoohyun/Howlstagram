package com.example.howlstagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.howlstagram.R
import com.example.howlstagram.navigation.model.ContentDTO
import com.google.firebase.firestore.FirebaseFirestore

class DetailViewFragment : Fragment{
    constructor() : super()

    var firestore : FirebaseFirestore? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail, container, false)
        firestore = FirebaseFirestore.getInstance()
        return view
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

        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewholder = (holder as CustomViewHolder).itemView

            //User Id
//            viewholder.
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

    }
}