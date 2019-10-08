package com.arlhar_membots.Login_and_Regist;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.arlhar_membots.Main3Activity;
import com.arlhar_membots.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PhoneAuth extends AppCompatActivity {
    private static final String TAG = "PhoneAuth";
    private EditText phoneText;
    private EditText codeText;
    private Button sendBut;
    private Button resendBut;
    private Button verifyBut;
    String number,userName, userFamily;
    Uri imageUri;
    private CountryCodePicker ccp;

    private String phoneVerificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            verificationCallbacks;
    private PhoneAuthProvider.ForceResendingToken resendToken;//повтор отправки
    private FirebaseAuth auth;
    FirebaseUser user;
    StorageReference storage;
    DatabaseReference databaseReferen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);
        phoneText = (EditText) findViewById(R.id.numText);
        codeText = (EditText) findViewById(R.id.codeText);
        verifyBut = (Button) findViewById(R.id.setCode);
        sendBut = (Button) findViewById(R.id.setNum);
        resendBut= (Button) findViewById(R.id.restartNum);
        Intent intent2= getIntent();
        userName = intent2.getStringExtra("userName");
        userFamily = intent2.getStringExtra("userFamily");
        imageUri = intent2.getParcelableExtra("imageUri");
        //код номера
        ccp=(CountryCodePicker)findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(phoneText);
        resendBut.setEnabled(true);
        verifyBut.setEnabled(true);

        auth = FirebaseAuth.getInstance();
        databaseReferen = FirebaseDatabase.getInstance().getReference();
        storage= FirebaseStorage.getInstance().getReference();
    }

    //метод кнопки отправки кода на номер
    public void onSetNum(View view){
        number=ccp.getFullNumberWithPlus();
        setUpVerifCallbacks();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,//номер для проверки
                60,//продолжительность тайм аута
                TimeUnit.SECONDS,
                this,
                verificationCallbacks, resendToken);
    }

    //метод кнопки повтора отправки смс
    public void onresNum(View view){
        number=ccp.getFullNumberWithPlus();
        setUpVerifCallbacks();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,//номер для проверки
                60,//продолжительность тайм аута
                TimeUnit.SECONDS,
                this,
                verificationCallbacks, resendToken);
    }

    private void setUpVerifCallbacks(){//настройка проверки обратных вызовов
        verificationCallbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                resendBut.setEnabled(false);
                verifyBut.setEnabled(false);
                codeText.setText("");
                signInWithPhoneAuthCredential(credential);
            }
            @Override
            public void onVerificationFailed(FirebaseException e) {//ошибка проверки(исключение)
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Неверный запрос
                    Log.d(TAG, "Недопустимые учетные данные: "
                            + e.getLocalizedMessage());
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    //  превышена квота СМС
                    Log.d(TAG, "Превышена квота СМС.");}
            }
            @Override
            public void onCodeSent(String verificationId,//по отправленному коду
                                   PhoneAuthProvider.ForceResendingToken token) {
                phoneVerificationId = verificationId;
                resendToken = token;
                verifyBut.setEnabled(true);
                sendBut.setEnabled(false);
                resendBut.setEnabled(true);
            }};
    }//конец метода setUp

    //метод для входа с проверкой учетной записи телефона
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            codeText.setText("");
                            resendBut.setEnabled(false);
                            verifyBut.setEnabled(false);
                            user = task.getResult().getUser();
                            loadphone();
                            Intent intent=new Intent(PhoneAuth.this,Main3Activity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            if (task.getException() instanceof
                                    FirebaseAuthInvalidCredentialsException) {
                                // Введенный проверочный код недействителен
                                Toast.makeText(getApplicationContext(),"Код недействителен:(",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }});
    }//конец метода signInWithPhoneAuthCredential

    public void onCode(View view) {//метод для кнопки проверки кода
        String code = codeText.getText().toString();
        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(phoneVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    public void loadphone(){
        //преобразуем ввод в переменную
        final String user_name = userName;
        final String user_family = userFamily;
        //условие,проверяем ввод пользователя на пустоту
        if(!TextUtils.isEmpty(user_name)&&!TextUtils.isEmpty(user_family)&&imageUri!=null){
            //id текущего пользователя
            final  String user_id= auth.getCurrentUser().getUid();
            Toast.makeText(PhoneAuth.this,
                    "ID:"+user_id,Toast.LENGTH_SHORT).show();
            //путь изображения
            StorageReference image_path=storage.child("profile_avatar").child(user_id+".jpeg");
            //положить uri
            image_path.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    //   если успешно
                    if (task.isSuccessful()) {
                        Uri download = task.getResult().getDownloadUrl();
                        //положить имя пользователя и аву
                        Map<String, String> usermap = new HashMap<>();
                        usermap.put("Avatar", download.toString());
                        usermap.put("name", user_name);
                        usermap.put("family", user_family);
                        //путь к базе
                        databaseReferen.child("Users").child(user_id).setValue(usermap);

                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(PhoneAuth.this,
                                "Ошибка:" + error, Toast.LENGTH_SHORT).show();
                    }
                    //.....здесь будет код для progress бар (если пригодится)
                }});
        }} //конец метода load
}
