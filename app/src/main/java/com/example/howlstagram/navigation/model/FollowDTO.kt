package com.example.howlstagram.navigation.model

data class FollowDTO (
    var followerCount : Int = 0,
    var followers : MutableMap<String, Boolean> = HashMap(),    // 중복 팔로워를 방지 하기 위한 변수

    var followingCount : Int = 0,
    var followings : MutableMap<String, Boolean> = HashMap()   // 중복 팔로잉을 방지 하기 위한 변수
)

