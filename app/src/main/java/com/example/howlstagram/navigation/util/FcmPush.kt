package com.example.howlstagram.navigation.util

import com.example.howlstagram.navigation.model.PushDTO
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.squareup.okhttp.*
import java.io.IOException

class FcmPush {     // 푸시를 전송해 주는 클래스, 원래 제3서버로 만들어야 하는데 안드로이드 안에서 구현해버림

    // todo : 파이어베이스 콘솔에서 푸시를 보내도 안오는거면 파이어베이스 푸시 콘솔과 앱이 연결이 안되엇다는 뜻?
    var JSON = MediaType.parse("application/json; charset=utf-8")      // 헤더값
    var url = "https://fcm.googleapis.com/fcm/send"      // 푸시 데이터를 보낼 파이어베이스 url 주소
    var serverKey = "AIzaSyCajUk1iZjJ0Fnntg0SAo8gYbZz3i0nsdk"   // push api키
    var gson : Gson? = null
    var okHttpClient : OkHttpClient? = null

    companion object {
        var instance = FcmPush()
    }

    init {
        gson = Gson()
        okHttpClient = OkHttpClient()
    }

    fun sendMessage(destinationUid : String, title : String, message : String) {

        // 상대방의 uid를 이용해서 푸시 토큰을 받아온다
        FirebaseFirestore.getInstance().collection("pushtokens").document(destinationUid).get().addOnCompleteListener {
            if (it.isSuccessful) {
                var token = it?.result?.get("pushToken").toString()

                var pushDTO = PushDTO()
                pushDTO.to = token
                pushDTO.notification.title = title
                pushDTO.notification.body = message

                // okhttp를 통해서 데이터를 firebase push에 보낸다
                var body = RequestBody.create(JSON, gson?.toJson(pushDTO))  // pushDTO를 json으로 만들어준다
                var request = Request.Builder()
                    .addHeader("Content-Type", "application/json")      // 헤더에 contentType을 json으로 설정
                    .addHeader("Authorization", "key="+serverKey)    // api키 추가
                    .url(url)
                    .post(body)
                    .build()

                // google fcm 서버에 전송
                okHttpClient?.newCall(request)?.enqueue(object : Callback {     // 객체  object : Callback {}   직접 수동으로 입력 해야함
                    override fun onFailure(request: Request?, e: IOException?) {
                        println("푸시실패")
                    }

                    override fun onResponse(response: Response?) {
                        println("푸시"+response?.body()?.string()) // 전송 성공했을때만 프린트 찍는다
                    }

                })


            }
        }
    }
}