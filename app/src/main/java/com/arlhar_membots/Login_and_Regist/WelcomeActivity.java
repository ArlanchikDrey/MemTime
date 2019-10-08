package com.arlhar_membots.Login_and_Regist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.arlhar_membots.R;

public class WelcomeActivity extends AppCompatActivity {

    private Button but_wel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        but_wel=(Button)findViewById(R.id.but_welcome) ;
        but_wel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WelcomeActivity.this, StartActivity.class);
                startActivity(intent);
            }
        });

    }


}

