package com.arlhar_membots.Login_and_Regist;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.arlhar_membots.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegistrationActivity extends AppCompatActivity {
    public  EditText etName,etFamily;
    private Button btnNext;
    //для авы
    CircleImageView image;
    public  Uri imageUri=null;
    private CheckBox checkMail;
    private CheckBox checkPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        etName =findViewById(R.id.editTextName);
        etFamily = findViewById(R.id.editTextfamily);
        checkMail=findViewById(R.id.checkBox);
        checkPhone=findViewById(R.id.checkBox2);
        btnNext = (Button) findViewById(R.id.btnNext);
        image=(CircleImageView)findViewById(R.id.circle);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //открываем галерию
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(RegistrationActivity.this);
            }});//конец слушателя

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Boolean name,family;
                name=(etName.getText().toString().length() >= 2 && etName.getText().toString().length()<=10);
                family=(etFamily.getText().toString().length() >= 4&& etFamily.getText().toString().length()<=13);

                if ((name&&family&&imageUri!=null)){
                    btnNext.setEnabled(true);
                }

            }
        },0, 1, TimeUnit.SECONDS);

        editTextWord();
    }//конец метода oncreate

    //сохраняем фото в circle
    @Override
    public void onActivityResult(int requestcode, int resultcode, Intent data){
        super.onActivityResult(requestcode,resultcode,data);
        //если запрос верный
        if(requestcode== CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result= CropImage.getActivityResult(data);
            if(resultcode==RESULT_OK){
                imageUri=result.getUri();
                image.setImageURI(imageUri);}
        //если ошибка
            else if (resultcode== CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                 Exception error=result.getError();
            }}
    }//конец метода

    //обработка edittext только на буквы
    public void editTextWord(){
        etName.setFilters(new InputFilter[] {
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
        etFamily.setFilters(new InputFilter[]{new InputFilter() {
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

    //Метод срабатывает при нажатии кнопки "продолжить"
    public void nextActivity(View view){
        if(checkMail.isChecked()){
        Intent intent = new Intent(RegistrationActivity.this, RegistrationActivity2.class);
            intent.putExtra("userName", etName.getText().toString());
            intent.putExtra("userFamily", etFamily.getText().toString());
            intent.putExtra("imageUri", imageUri);
        startActivity(intent);

       }
        else{
            Intent intent2=new Intent(RegistrationActivity.this,PhoneAuth.class);
            intent2.putExtra("userName", etName.getText().toString());
            intent2.putExtra("userFamily", etFamily.getText().toString());
            intent2.putExtra("imageUri", imageUri);
            startActivity(intent2);

        }
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); //Анимация сдвига
    }//конец метода nextActivity

    public void onClickBack(View view){
        Intent intent = new Intent(RegistrationActivity.this, StartActivity.class);
        startActivity(intent);
    }}
