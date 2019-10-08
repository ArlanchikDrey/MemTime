package com.arlhar_membots.Basic.LentaCycle;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.Toolbar;

import com.arlhar_membots.R;

/**
 * Created by admin on 28.03.2018.
 */

public class Lenta_and_cycle extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.lenta_and_cicler, container, false);

        //вызываем первой домашнюю страницу
        getActivity().getSupportFragmentManager().beginTransaction().
               add(R.id.lenta_container,new LentaFragment()).commit();




        return rootView;
    }


  /*/  public BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    switch (item.getItemId()) {
                        case R.id.lenta_vert:
                            selectedFragment=new LentaFragment();
                            break;
                        case R.id.lenta_horiz:
                            selectedFragment=new CycleMain();
                            break;

                    }
                    getActivity().getSupportFragmentManager().beginTransaction().
                            replace(R.id.lenta_container,selectedFragment).commit();
                    return true;
                }  }; /*/





}
