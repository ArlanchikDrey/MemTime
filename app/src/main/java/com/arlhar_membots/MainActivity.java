package com.arlhar_membots;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.arlhar_membots.Login_and_Regist.WelcomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser==null){
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            startActivity(intent);

        }else{
            Intent intent = new Intent(MainActivity.this, Main3Activity.class);
            startActivity(intent);
            finish();
        }
    }



}


