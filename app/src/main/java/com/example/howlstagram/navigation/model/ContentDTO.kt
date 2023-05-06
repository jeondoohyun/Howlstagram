package com.example.howlstagram.navigation.model

import java.util.*

// data class (VO 클래스)
data class ContentDTO(var explain: String? = null,
                      var imageUrl: String? = null,
                      var uid: String? = null,  // 유저 식별자
                      var userId: String? = null,
                      var timestamp: Long? = null,
                      var favoriteCount: Int = 0,
                      var favorites: Map<String, Boolean> = HashMap()) {

    // 댓글관리 데이터
    data class Comment(var uid : String? = null,
                       var userId : String? = null,
                       var comment : String? = null,
                       var timestamp : Long? = null)

}
