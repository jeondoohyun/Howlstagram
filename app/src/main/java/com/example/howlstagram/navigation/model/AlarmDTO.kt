package com.example.howlstagram.navigation.model

data class AlarmDTO(
    var destinationUid : String? = null,
    var userId : String? = null,        // 이메일
    var uid : String? = null,
    var kind : Int? = null,     // 어떤 타입의 메세지 종류인지 알수 있는 변수(좋아요인지, 댓글인지 등?)
    var message : String? = null,
    var timeStamp : Long? = null

)