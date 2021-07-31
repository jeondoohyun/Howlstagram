package com.example.howlstagram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null     // Authentication 객체 변수만 만들어서 null 넣어둠. 코틀린은 초기화 자동화가 아니라 null을 직접 넣어줘야함.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        email_login_button.setOnClickListener {
            signInAndSignup()
        }
    }

    fun signInAndSignup() {
        auth?.createUserWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())?.addOnCompleteListener {
            it ->
                if (it.isSuccessful) {
                    // 아이디가 생성이 완료 되었을때
                    moveMainPage(it.result?.user)
                } else if (!it.exception?.message.isNullOrEmpty()) {
                    // 아이디가 생성되지 않았을때
                    Toast.makeText(this, it.exception?.message,Toast.LENGTH_SHORT).show()
                } else {
                    // 회원가입도 아니고 에러도 안났을때 > 로그인일때
                    signInEmail()
                }
        }
    }

    fun signInEmail() {
        auth?.signInWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())?.addOnCompleteListener {
                it ->
            if (it.isSuccessful) {
                // 로그인이 성공하였을때
                moveMainPage(it.result?.user)
            } else {
                // 아이디 or 비밀번호가 틀렸을때
                Toast.makeText(this, it.exception?.message,Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun moveMainPage(user:FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}