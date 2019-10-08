package com.arlhar_membots.Login_and_Regist;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.arlhar_membots.Basic.UserFragment;
import com.arlhar_membots.Main3Activity;
import com.arlhar_membots.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity2 extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;
    private EditText editmail,editpass, editpassRepeat;
    String userName, userFamily;
    Uri imageUri;
    StorageReference store;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registartion2);
        editmail=findViewById(R.id.rEmail); //EDITTEXT
        editpass=findViewById(R.id.rPassword);
        editpassRepeat=findViewById(R.id.rPasswordRepeat);
        mAuth= FirebaseAuth.getInstance();

        Intent intent = getIntent();
        userName = intent.getStringExtra("userName");
        userFamily = intent.getStringExtra("userFamily");
        imageUri = intent.getParcelableExtra("imageUri");
        store= FirebaseStorage.getInstance().getReference();

    }//конец метода oncreate



    public void registration(final String email, final String pass){
        mAuth.createUserWithEmailAndPassword(email,pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){ //если успешная регистрация
                    Toast.makeText(RegistrationActivity2.this,"Регистрация прошла успешно",
                                    Toast.LENGTH_SHORT).show();
                    mAuth.signInWithEmailAndPassword(email, pass);
                    Intent intent=new Intent(RegistrationActivity2.this,Main3Activity.class);
                    startActivity(intent);
                    user = mAuth.getCurrentUser();
                    load();
                }else{
                    try {
                        throw task.getException(); //отлов ошибрк
                    } catch(FirebaseAuthWeakPasswordException e) {
                        editpass.setError(getString(R.string.error_weak_password)); //Слишком простой пароль
                        editpass.requestFocus();
                    } catch(FirebaseAuthInvalidCredentialsException e) {
                        editmail.setError(getString(R.string.error_invalid_email)); //Неверный формат почты
                        editmail.requestFocus();
                    } catch(FirebaseAuthUserCollisionException e) {
                        editmail.setError(getString(R.string.error_user_exists)); //Пользователь существует
                        editmail.requestFocus();
                    } catch(Exception e) {
                        Toast.makeText(RegistrationActivity2.this,"Регистрация провалена" + task.getException(), //Другая ошибка
                                Toast.LENGTH_SHORT).show();
                    }
                }}});
   }//конец метода регистрации

    public void onClickReg(View view){
        if ((!editmail.getText().toString().equals("")) && (!editpass.getText().toString().equals("")) //Если поле1 не пустое, поле2 не пустое
                && (!editpassRepeat.getText().toString().equals("")) //Поле3 не пустое
                && (editpass.getText().toString().equals(editpassRepeat.getText().toString()))){ //Совпадают пароли
            registration(editmail.getText().toString(),editpass.getText().toString()); //регистрация
        }else{
            if (editmail.getText().toString().equals("")){ //Если пустое поле1
                editmail.setError(getString(R.string.emptyMail));
                editmail.requestFocus();
            }
            if (editpass.getText().toString().equals("")){ //Если пустое поле2
                editpass.setError(getString(R.string.emptyPass));
                editpass.requestFocus();
            }
            if (editpassRepeat.getText().toString().equals("")){ //Если пустое поле3
                editpassRepeat.setError(getString(R.string.emptyPass));
                editpassRepeat.requestFocus();
            }else{
            if (!editpass.getText().toString().equals(editpassRepeat.getText().toString())){ //Если пароли не совпадают
                editpassRepeat.setError(getString(R.string.equalPass));
                editpassRepeat.requestFocus();
            }}}}


    public void load(){

        //преобразуем ввод в переменную
        final String user_name = userName;
        final String user_family = userFamily;
        //условие,проверяем ввод пользователя на пустоту
        if(!TextUtils.isEmpty(user_name)&&!TextUtils.isEmpty(user_family)&&imageUri!=null){

            //id текущего пользователя
            final  String user_id= mAuth.getCurrentUser().getUid();
        Toast.makeText(RegistrationActivity2.this,
                "ID:"+user_id,Toast.LENGTH_SHORT).show();
            //путь изображения
            StorageReference image_path=store.child("profile_avatar").child(user_id+".jpeg");
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
                       DatabaseReference databaseReference= FirebaseDatabase.
                               getInstance().getReference().child("Users").child(user_id);
                            databaseReference.setValue(usermap);

                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(RegistrationActivity2.this,
                                "Ошибка:" + error, Toast.LENGTH_SHORT).show();
                    }
                    //.....здесь будет код для progress бар (если пригодится)
                }});
        }} //конец метода load

    public void onClickBack(View view){
        finish();
    }


    @Override //Метод срабатывает при закрытии Активити
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right); //Анимация сдвига
    }


}
