package com.example.howlstagram.navigation.model

import java.util.*

// data class (VO 클래스), dto : data transfer object
data class ContentDTO(var explain: String? = null,
                      var imageUrl: String? = null,
                      var uid: String? = null,  // 유저 식별자
                      var userId: String? = null,
                      var timestamp: Long? = null,
                      var favoriteCount: Int = 0,
                      var favorites: MutableMap<String, Boolean> = HashMap(),

                      // dto 클래스에서 변수를 생성하면 서버로 ContentDTO클래스를 전송했을때 서버에 생성한 변수대로 firebase 서버에 저장된다.(AddPhotoActivity에서 서버로 송신)
                      var test: String? = null) {

    // 댓글관리 데이터
    data class Comment(var uid : String? = null,
                       var userId : String? = null,
                       var comment : String? = null,
                       var timestamp : Long? = null)

}
