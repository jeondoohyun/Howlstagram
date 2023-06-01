package com.example.howlstagram.navigation.model

import java.util.*

// data class (VO 클래스), dto : data transfer object
data class ContentDTO(var explain: String? = null,
                      var imageUrl: String? = null,
                      var uid: String? = null,  // 유저 식별자, 각 이메일마다 부여되는 uid
                      var userId: String? = null,   // 이메일
                      var timestamp: Long? = null,
                      var favoriteCount: Int = 0,
                      var favorites: MutableMap<String, Boolean> = HashMap(),

                      // dto 클래스에서 변수를 생성하면 서버로 ContentDTO클래스를 전송했을때 서버에 생성한 변수대로 firebase 서버에 저장된다.(AddPhotoActivity에서 서버로 송신)
                      var test: String? = null) {

    // 댓글관리 데이터, 데이터 클래스 안에 또 데이터 클래스가 있네?
    data class Comment(var uid : String? = null,
                       var userId : String? = null,
                       var comment : String? = null,
                       var timestamp : Long? = null)

}
