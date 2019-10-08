package com.arlhar_membots.Login_and_Regist;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.arlhar_membots.Main3Activity;
import com.arlhar_membots.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

import java.util.HashMap;
import java.util.Map;


public class StartActivity extends AppCompatActivity {
        Shimmer shimmer;
        ShimmerTextView shimmerText;
        Button btnLogin, btnReg, btnForgot;
        EditText etEmail, etPassword;
        private FirebaseAuth mAuth; //Данные об авторизации
        FirebaseUser user; //Данные о пользователи
        private DatabaseReference databaseReference ; //Ссылка на БД
        private String email, password;
    //google
    GoogleSignInClient mGoogleClient;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        startShimmer(); //Запуск мигающего текста
        init(); //Инициализация кнопок и полей
        SignInButton sign=findViewById(R.id.google);
        mAuth = FirebaseAuth.getInstance();
        sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = mGoogleClient.getSignInIntent();
                startActivityForResult(signInIntent, 101);
                }
        });
        //Настройки Входа В Аккаунт Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleClient= GoogleSignIn.getClient(this,gso);

    }// конец метода oncreate

    //-----------------------------------------------------------------------------------
    //все связанное с гугл
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
         // Результат, возвращенный при запуске Intent из GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 101) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Вход в Google был успешным, аутентификация с Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
           // Ошибка входа в Google

            }}}//конец метода activresult

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) { // Успешный вход, обновление пользовательского
                            //проверка на наличие данных о пользователе в базе
                            user=mAuth.getCurrentUser();
                            final  String user_id= mAuth.getCurrentUser().getUid();
                            databaseReference=FirebaseDatabase.getInstance().getReference("Users");
                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id)){

                                    }else{
                                        String name_google=user.getDisplayName();
                                        Uri ava=user.getPhotoUrl();
                                        Map<String,String>usermap =new HashMap<>();
                                        usermap.put("Avatar", ava.toString());
                                        usermap.put("name", name_google);
                                        usermap.put("family", "");
                                        databaseReference.child(user_id).setValue(usermap);

                                    }

                                    }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            //переходим в главный экран
                            Intent intent=new Intent(getApplicationContext(),Main3Activity.class);
                            startActivity(intent);
                            finish();
                            Toast.makeText(getApplicationContext(),
                                    "Успешный вход",Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Не удалось войти",Toast.LENGTH_SHORT).show();
                        }
                    }});
    }

    //---------------------------------------------------------------------------------

    //Для всего остального
    private void startShimmer(){
        shimmerText = (ShimmerTextView) findViewById(R.id.shimmer);
        shimmer = new Shimmer();
        shimmer.start(shimmerText);
    }

    private void init(){
            //Кнопки
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnReg = (Button) findViewById(R.id.btnReg);
        btnForgot = (Button) findViewById(R.id.btnForget);
            //Поля
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
    }

    public void onClickLogin(View view){
        email = etEmail.getText().toString();
        password = etPassword.getText().toString();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    user = mAuth.getCurrentUser();
                    Intent intent=new Intent(StartActivity.this,Main3Activity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(StartActivity.this,"Ошибка входа",Toast.LENGTH_SHORT).show();

                }}});
    }

    public void onClickReg(View view){ //При нажатии на "Регистрация"
        Intent intent = new Intent(StartActivity.this, RegistrationActivity.class);
        startActivity(intent);
    }

    public void onClickForget(View view){//при нажатии на забыли пароль
       Intent intent=new Intent(StartActivity.this,ForgotPassword.class);
       startActivity(intent);
    }


}
