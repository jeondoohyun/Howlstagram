package com.example.howlstagram.navigation.model

data class AlarmDTO(
    var destinationUid : String? = null,
    var userId : String? = null,        // 이메일
    var uid : String? = null,
    var kind : Int? = null,     // 0 : like alarm, 1 : comment alarm, 2 : follow alarm
    var message : String? = null,
    var timeStamp : Long? = null

)