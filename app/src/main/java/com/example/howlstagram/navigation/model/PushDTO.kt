package com.example.howlstagram.navigation.model



data class PushDTO(
    var to : String? = null,    // 푸시를 받는 사람의 토큰 값
    var notification : Notification = Notification()
) {
    data class Notification(
        var body : String? = null,  // 푸시 내용
        var title : String? = null  // 푸시 제목
    )
}