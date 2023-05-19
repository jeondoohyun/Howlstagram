package com.example.howlstagram

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.howlstagram.databinding.ActivityLoginBinding
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


class LoginActivity : AppCompatActivity() {
    /*
    - 안드로이드스튜디오에서 firebase연결
    1. tool탭 > firebase > authentication 클릭
    2. connect to your app 클릭후 firebase사이트에서 프로젝트 생성후 안드로이드 스튜디오와 연결
    3. 코드에서 회원가입 및 로그인 코드를 작성후 firebase 콘솔 화면(웹페이지)에서 email사용을 허용 하면 회원가입이 완료되면 유저 정보가 firebase에 저장된다.

    - Google login
    Google login은 로그인 플랫폼, Firebase는 서버 플랫폼.
    Google이랑 Firebase랑 같은 회사라고 Firebase안에 Google login이 있는것이 아니다.
    Google로그인은 Google developer 사이트에서 앱 등록을 따로 할 필요는 없다.

    - Facebook login
    Facebook developer사이트 에서 앱등록과 해시키 등록을 하고 firebase에서 이용해야한다.
    firebase에서 facebook login기능을 활성화 시킬때 앱ID, 앱 비밀번호 OAuth 리디렉션 URI를 등록해야 한다.
    앱ID, 앱 비밀번호는 facebook developer사이트 설정에서 복붙해서 firebase에 넣는다.
    OAuth 리디렉션 URI는 firebase에서 복사 해서 facebook developer 사이트에 붙여 넣는다.
    */


    var auth : FirebaseAuth? = null     // Authentication 객체 변수만 만들어서 null 넣어둠. 코틀린은 초기화 자동화가 아니라 null을 직접 넣어줘야함.
    var googleSigninClient : GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001
    var callbackManager : CallbackManager? = null   // facebook 로그인 결과를 가져오는 콜백매니저

    private var mBinding: ActivityLoginBinding? = null
    private val binding get() = mBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

//        if (auth?.currentUser != null) {
//            Log.e("바로 로그인", "진입")
//            var intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//            finish()
//        }

        binding.emailLoginButton.setOnClickListener {
            signInAndSignup()
        }

        binding.googleSignInButton.setOnClickListener {
            googleLogin()
        }

        binding.facebookLoginButton.setOnClickListener {
            facebookLogin()
        }

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))  // default_web_client_id를 찾지 못한다면 경로에 직접 가서 string.xml파일에 복붙한다. C:\AndroidProject\Project\Howlstagram\app\build\generated\res\google-services\debug\values\values.xml
            .requestEmail()
            .build()
        googleSigninClient = GoogleSignIn.getClient(this, gso)
        callbackManager = CallbackManager.Factory.create()
    }

    override fun onStart() {
        super.onStart()
        moveMainPage(auth?.currentUser)  //자동 로그인, 아이디랑 비밀번호 확인하는것은 아니고 여기선 그냥 무조건 자동 로그인, currentUser값이 com.google.firebase.auth.internal.zzx@d5d3be5 이런식으로 뜨는데 매번 바뀜

        Log.e("유저확인","${auth?.currentUser}")
    }

    fun facebookLogin() {
        LoginManager.getInstance()
                .logInWithReadPermissions(this, Arrays.asList("public_profile","email"))    // 페이스북에 권한 요청(프로필 이미지랑, 이메일을 요청)

        LoginManager.getInstance()
                .registerCallback(callbackManager, object : FacebookCallback<LoginResult>{
                    override fun onSuccess(result: LoginResult?) {
                        handleFacebookAccessToken(result?.accessToken)
                    }

                    override fun onCancel() {
                    }

                    override fun onError(error: FacebookException?) {
                    }

                })
    }

    fun handleFacebookAccessToken(token : AccessToken?) {
        var credential = FacebookAuthProvider.getCredential(token?.token!!)
        auth?.signInWithCredential(credential)  // firebase에 넘기기
                ?.addOnCompleteListener { it ->
                    if (it.isSuccessful) {
                        // 로그인이 성공하였을때
                        moveMainPage(it.result?.user)
                    } else {
                        // 아이디 or 비밀번호가 틀렸을때
//                    Toast.makeText(this, it.exception?.message,Toast.LENGTH_SHORT).show()
                        Toast.makeText(this, "이메일 및 비밀번호를 확인하세요.", Toast.LENGTH_SHORT).show()
                    }
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        Log.e("구글로그인","$requestCode")
        if (requestCode == GOOGLE_LOGIN_CODE) {
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data!!)   // 구글에서 넘겨 주는 로그인 결과값 받기
            Log.e("구글로그인_2","${result!!.isSuccess}, $data")
            if(result!!.isSuccess){   // 응답 받은 결과값이 성공 했을때      // todo : 응답 값 false 뜸, 수정 할것, 네비게이션바 클릭된 아이콘 연하게 되있는데 진하게 처리하기, 구글 및 페이스북 같은 외부 플랫폼 로그인 사용할때 로그인 반응 안하는 문제 원인이 뭔지 확실하게 정리 할것
                var account = result.signInAccount
                firebaseAuthWithGoogle(account)
            }
        }
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        var credential = GoogleAuthProvider.getCredential(account?.idToken, null)   // account의 Token을 넣어 AuthCredential을 만든다.
        auth?.signInWithCredential(credential)  // 그 credential을 넣어 준다. firebase에 넘기기
            ?.addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    // 로그인이 성공하였을때
                    moveMainPage(it.result?.user)
                } else {
                    // 아이디 or 비밀번호가 틀렸을때
//                    Toast.makeText(this, it.exception?.message,Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, "이메일 및 비밀번호를 확인하세요.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun googleLogin() {
        var signInIntent = googleSigninClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
    }

    fun signInAndSignup() {
        if ( !TextUtils.isEmpty(binding.emailEdittext?.text.toString()) && !TextUtils.isEmpty(binding.passwordEdittext?.text.toString())) {
            auth?.createUserWithEmailAndPassword(binding.emailEdittext?.text.toString(), binding.passwordEdittext?.text.toString())?.addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    // 아이디가 생성이 완료 되었을때
                    moveMainPage(it.result?.user)
                } else if (it.exception?.message.isNullOrEmpty()) {
                    // 아이디가 생성되지 않았을때
//                    Toast.makeText(this, it.exception?.message,Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, "이메일 및 비밀번호를 확인하세요.", Toast.LENGTH_SHORT).show()

                } else {
                    // 회원가입도 아니고 에러도 안났을때 > 로그인일때
                    signInEmail()
                }
            }
        } else {
            Toast.makeText(this, "이메일, 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
        }

    }

    fun signInEmail() {
        auth?.signInWithEmailAndPassword(binding.emailEdittext.text.toString(), binding.passwordEdittext.text.toString())?.addOnCompleteListener { it ->
            if (it.isSuccessful) {
                // 로그인이 성공하였을때
                moveMainPage(it.result?.user)
            } else {
                // 아이디 or 비밀번호가 틀렸을때
//                Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
                Toast.makeText(this, "이메일 또는 비밀번호를 확인하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun moveMainPage(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

//    fun printHashKey() {
//        try {
//            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
//            for (signature in info.signatures) {
//                val md: MessageDigest = MessageDigest.getInstance("SHA")
//                md.update(signature.toByteArray())
//                val hashKey = String(Base64.encode(md.digest(), 0))
//                Log.i("TAG", "printHashKey() Hash Key: $hashKey")
//            }
//        } catch (e: NoSuchAlgorithmException) {
//            Log.e("TAG", "printHashKey()", e)
//        } catch (e: Exception) {
//            Log.e("TAG", "printHashKey()", e)
//        }
//    }
}