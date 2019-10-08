package com.arlhar_membots;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.arlhar_membots.Basic.HomeFragment;
import com.arlhar_membots.Basic.LentaCycle.Commentik;
import com.arlhar_membots.Basic.LentaCycle.Lenta_and_cycle;
import com.arlhar_membots.Basic.UserFragment;


public class Main3Activity extends AppCompatActivity  {

    BottomNavigationView bottomNav;
    FrameLayout container;
    android.support.v4.app.FragmentManager fragmentManager;
    HomeFragment home;
    Lenta_and_cycle lenta_and_cycle;
    UserFragment userFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);


       if( !(isOnline(this))){

       }else{
           bottomNav = findViewById(R.id.bottom_navigation);
           bottomNav.setOnNavigationItemSelectedListener(navListener);

           fragmentManager = getSupportFragmentManager ();
           container = findViewById(R.id.fragment_container);

           lenta_and_cycle = new Lenta_and_cycle();
           android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
           fragmentTransaction.add(R.id.fragment_container, lenta_and_cycle);
           fragmentTransaction.hide(lenta_and_cycle);
           fragmentTransaction.commit();

           home = new HomeFragment();
           fragmentTransaction = fragmentManager.beginTransaction();
           fragmentTransaction.add(R.id.fragment_container, home);
           //fragmentTransaction.hide(home);
           fragmentTransaction.commit();


           userFragment = new UserFragment();
           fragmentTransaction = fragmentManager.beginTransaction();
           fragmentTransaction.add(R.id.fragment_container, userFragment);
           fragmentTransaction.hide(userFragment);
           fragmentTransaction.commit();




       }



    }


    public BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()) {
                        case R.id.navigation_home:
                            setTitle(getString(R.string.app_name));
                            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.hide(userFragment);
                            fragmentTransaction.hide(lenta_and_cycle);
                            fragmentTransaction.show(home);
                            fragmentTransaction.commit();
                            break;
                        case R.id.navigation_dashboard:
                            setTitle(getString(R.string.title_dashboard));

                            fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.hide(home);
                            fragmentTransaction.hide(userFragment);
                            fragmentTransaction.show(lenta_and_cycle);
                            fragmentTransaction.commit();
                            break;
                        case R.id.navigation_notifications:
                            setTitle(getString(R.string.title_notifications));

                            fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.hide(lenta_and_cycle);
                            fragmentTransaction.hide(home);
                            fragmentTransaction.show(userFragment);
                            fragmentTransaction.commit();
                            break;
                    }

                    return true;
                }  };

    //метод для проверки наличия подключения к сети
    public boolean isOnline(Context context)
    {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting())
        {
            return true;
        }
        View parentLayout = findViewById(android.R.id.content);
        Snackbar.make(parentLayout, "Нет подключения к интернету:(", Snackbar.LENGTH_LONG).show();
        return false;
    }


}
