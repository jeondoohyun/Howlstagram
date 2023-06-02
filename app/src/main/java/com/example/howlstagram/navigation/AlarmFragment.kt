package com.example.howlstagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.howlstagram.R
import com.example.howlstagram.databinding.FragmentAlarmBinding
import com.example.howlstagram.databinding.FragmentGridBinding
import com.example.howlstagram.navigation.model.AlarmDTO

class AlarmFragment : Fragment{
    // 좋아요 관련 정보창 4번째 바텀 네비게이션
    constructor() : super()

    private var mBinding: FragmentAlarmBinding? = null
    private val binding get() = mBinding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentAlarmBinding.inflate(inflater, container, false)

        return binding.root
    }

    inner class AlarmRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var alarmDTOList : ArrayList<AlarmDTO> = arrayListOf()


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)

            return CustomViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewholder = (holder as CustomViewHolder)

            when (alarmDTOList[position].kind) {
                0 -> {
                    val str_0 = alarmDTOList[position].userId + getString(R.string.alarm_favorite)
                    viewholder.commentviewitem_textview_profile.text = str_0
                }
                1 -> {  // todo :
                    val str_0 = alarmDTOList[position].userId + getString(R.string.alarm_favorite)
                    viewholder.commentviewitem_textview_profile.text = str_0
                }
                2 -> {
                    val str_0 = alarmDTOList[position].userId + getString(R.string.alarm_favorite)
                    viewholder.commentviewitem_textview_profile.text = str_0
                }
            }

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