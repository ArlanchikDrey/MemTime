package com.arlhar_membots.Login_and_Regist;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.arlhar_membots.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Main2Activity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private CircleImageView image_ava;
    private ImageButton change;
    private EditText nametext,familytext;
    private ImageButton edit_username;
    private GoogleApiClient apiclient;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private DatabaseReference databas;
    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private StorageReference store;
    public Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        image_ava=(CircleImageView)findViewById(R.id.circleImage_setting);
        nametext=findViewById(R.id.textName_setting);
        edit_username=findViewById(R.id.edit_usernames);
        familytext=findViewById(R.id.textFamily_setting);
        change=findViewById(R.id.photo_change);

        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(Main2Activity.this);
            }
        });

      /*  GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        apiclient =new GoogleApiClient.Builder(this).
                enableAutoManage(this,this).
                addApi(Auth.GOOGLE_SIGN_IN_API,gso).build();*/
        mDatabase= FirebaseDatabase.getInstance().getReference(); //Ссылка на БД
        user= FirebaseAuth.getInstance().getCurrentUser();
        store= FirebaseStorage.getInstance().getReference();


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    databas= FirebaseDatabase.getInstance().getReference("Users");
                    databas.child(user.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild("name")&&dataSnapshot.hasChild("family")){
                                String name=  dataSnapshot.child("name").getValue(String.class);
                                String family=  dataSnapshot.child("family").getValue(String.class);
                                nametext.setText(name);
                                familytext.setText(family);
                                imageUri= Uri.parse(dataSnapshot.child("Avatar").getValue(String.class));
                                try{
                                    Glide.with(getApplicationContext()).load(imageUri).into(image_ava);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }else{
                                Uri ava=user.getPhotoUrl();;
                                nametext.setEnabled(false);
                                familytext.setEnabled(false);
                                try{
                                    Glide.with(getApplicationContext()).load(ava).into(image_ava);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                change.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Toast.makeText(getApplicationContext(),"Это действие запрещено!(",Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }

                            }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    goLogInScreen();
                }
            }};
        editTextWord();
        }


    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(firebaseAuthListener);
    }

    //выход из настроек
    public void back(View view){
        finish();
    }

    //сохраняем фото в circle
    @Override
    public void onActivityResult(int requestcode, int resultcode, Intent data){
        super.onActivityResult(requestcode,resultcode,data);
        //если запрос верный
        if(requestcode== CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result= CropImage.getActivityResult(data);
            if(resultcode==RESULT_OK){
                    imageUri=result.getUri();
                    image_ava.setImageURI(imageUri);
                    //путь изображения;
                    StorageReference image_path=store.child("profile_avatar").child(user.getUid()+".jpeg");
                    image_path.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            //   если успешно
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(),"Ава успешно изменена!:)",
                                        Toast.LENGTH_SHORT).show();
                                Uri download = task.getResult().getDownloadUrl();
                                //путь к базе
                                DatabaseReference databaseReference= FirebaseDatabase.
                                        getInstance().getReference().child("Users").child(user.getUid()).child("Avatar");
                                databaseReference.setValue(download.toString());


                            } else {
                                String error = task.getException().getMessage();
                                Toast.makeText(getApplicationContext(),
                                        "Ошибка:(" + error, Toast.LENGTH_SHORT).show();
                            }
                            //.....здесь будет код для progress бар (если пригодится)
                        }}); }
            //если ошибка
            else if (resultcode== CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception error=result.getError();
                Toast.makeText(getApplicationContext(), "Ошибка:(", Toast.LENGTH_SHORT).show();
            }}
    }//конец метода

    //обработка edittext только на буквы
    public void editTextWord(){
        nametext.setFilters(new InputFilter[] {
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start,
                                               int end, Spanned dest, int dstart, int dend) {
                        if(source.equals("")){ // for backspace
                            return source;
                        }
                        if(source.toString().matches("[a-zA-Zа-яА-Я]+")){
                            return source;
                        }
                        return "";
                    }
                }
        });
        familytext.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
                if(charSequence.equals("")){
                    return charSequence;
                }
                if(charSequence.toString().matches("[a-zA-Zа-яА-Я]+")){
                    return charSequence;
                }
                return "";
            }
        }});
    }
    //слушатеоь нажатия на кнопку смены имени
    public void onEditName(View view){
         Boolean name,family,name_error,family_error;
         name=(nametext.getText().toString().length() >= 2 && nametext.getText().toString().length()<=10);
         family=(familytext.getText().toString().length() >= 4&& familytext.getText().toString().length()<=13);

         name_error=(nametext.getText().toString().length() <2||nametext.getText().toString().length() >10);
         family_error=(familytext.getText().toString().length() < 4||familytext.getText().toString().length() > 13);

        if (name && family ){
           mDatabase.child("Users").child(user.getUid()).child("name").setValue(nametext.getText().toString());
           mDatabase.child("Users").child(user.getUid()).child("family").setValue(familytext.getText().toString());
            Toast.makeText(getApplicationContext(),"Успешно изменено!:)",
                    Toast.LENGTH_SHORT).show();
            }
            else if(name_error){
            Toast.makeText(getApplicationContext(), "Ошибка:( \n В имени не может быть менее 2 или более 10 букв", Toast.LENGTH_SHORT).show();
            }else if(family_error){
            Toast.makeText(getApplicationContext(), "Ошибка:( \n В фамилии не может быть менее 4 или более 13 букв", Toast.LENGTH_SHORT).show();
        }
        }

    //метод который перебрасывает на StartActiviry после выхода
    private void goLogInScreen() {
        Intent intent = new Intent(this, StartActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    //открытие помощи
    public void onclickHelp(View view){
        Intent intent=new Intent(this,HelpSetting.class);
        startActivity(intent);

    }
    //открытие о нас
    public void onclickDevelop(View view){
        Intent intent=new Intent(this,AboutDevelop.class);
        startActivity(intent);
    }

    //---------метод для кнопки выйти
    public void logOut(View view) {
        firebaseAuth.signOut();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuthListener != null) {
            firebaseAuth.removeAuthStateListener(firebaseAuthListener);
        }
    }



}






