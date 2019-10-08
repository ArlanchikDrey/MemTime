package com.arlhar_membots.Basic.LentaCycle;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.arlhar_membots.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class LentaFragment extends android.support.v4.app.Fragment {

    private final static String TAG = "MemTime_status";

    private DatabaseReference mDatabase;
    private RecyclerView recyclerView;
    Adapter adapter;
    LinearLayoutManager manager;

    List<MemModel> memList = new ArrayList<>();
    List<String> favourites = new ArrayList<>();
    List<String> memIds = new ArrayList<>();
    HashMap<String, Integer> ratedMems = new HashMap<>();

    MemModel memModel;

    private Boolean isScroll = false;
    private int currentItems, totalItems, scrolloutItems;

    private ProgressBar progressBar;
    Context context;
    private SwipyRefreshLayout mSwipe;//обновление при свайпе

    String current_id;

    FirebaseUser user;
    String userID;

    Integer limit = 20; //Сколько мемов будет прогружаться за один раз
    Boolean deleteRated = false;

    Switch aSwitch;
    Switch switch_deleteRates;
    FragmentManager fragmentManager;
    CycleMain cycleMain;
    ImageButton btn_change;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.lenta_main, container, false);
        mDatabase = FirebaseDatabase.getInstance().getReference(); //Получаем ссылку на бд
        user = FirebaseAuth.getInstance().getCurrentUser(); //Получаем текущего пользователя
        assert user != null;
        userID = user.getUid(); //Получаем его ID
        recyclerView = rootView.findViewById(R.id.recyclerViewLenta);
        progressBar = rootView.findViewById(R.id.progresss);
        btn_change = rootView.findViewById(R.id.change_lenta);
        mSwipe=rootView.findViewById(R.id.swipeLenta);

        getUserFavourites();


        fragmentManager = getFragmentManager();
        cycleMain = new CycleMain();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.lenta_container, cycleMain);
        fragmentTransaction.hide(cycleMain);
        fragmentTransaction.commit();


        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        aSwitch = toolbar.findViewById(R.id.switch1);
        switch_deleteRates = toolbar.findViewById(R.id.switch_deleteRates);

        switch_deleteRates.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                deleteRated = b;
                updateRecyclerView();
            }
        });

        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateRecyclerView();
                progressBar.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                       if (memList!=null){
                           progressBar.setVisibility(View.GONE);
                       }

                    }
                },2000);


            }
        });//обновляем базу по кнопке
        onswipeLenta();//обновляем базу по свайпу



        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked){
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.hide(cycleMain);
                    fragmentTransaction.show(LentaFragment.this);
                    fragmentTransaction.commit();

                }else{
                    cycleMain.aSwitch.setChecked(true);
                    aSwitch.setChecked(false);
                    cycleMain.aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (!isChecked) {
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.hide(cycleMain);
                                fragmentTransaction.show(LentaFragment.this);
                                fragmentTransaction.commit();
                            }
                        }
                    });

                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.hide(LentaFragment.this);
                    fragmentTransaction.show(cycleMain);
                    fragmentTransaction.commit();
                }
            }
        });

/*
        cycleMain.aSwitch.setChecked(false);
        cycleMain.aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked){
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.hide(cycleMain);
                    fragmentTransaction.show(LentaFragment.this);
                    fragmentTransaction.commit();
                }
            }
        });
*/
        return rootView;
    }



    public void getUserFavourites() {

        mDatabase.child("Users").child(userID).child("Favourites")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.i(TAG, "Связь с веткой /Избранные мемы пользователя/ успешно установлена!");
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                            String url = postSnapshot.getValue(String.class);
                            favourites.add(url);
                        }
                        getUserRated();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        String errorText = "Произошла ошибка в загрузке избранных мемов";
                        Toast.makeText(getContext(), errorText, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void getUserRated(){
        mDatabase.child("Users").child(userID).child("Rated")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.i(TAG, "Связь с веткой /Оценки пользователя/ успешно установлена!");
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                            String uid = postSnapshot.getKey();
                            Integer rating = postSnapshot.getValue(Integer.class);
                            ratedMems.put(uid, rating);
                        }
                        getMemList();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        String errorText = "Произошла ошибка в загрузке оценок пользователя";
                        Toast.makeText(getContext(), errorText, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void getMemList(){
        //Алгоритм: если в результате запроса мы добавим 0 новых мемов, то повторить запрос.
        Query query = mDatabase.child("Memes").orderByKey().limitToLast(limit + 1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "Связь с веткой /Мемы/ успешно установлена!");

                Boolean getCurrentId = false;
                ArrayList<MemModel> temporary_list = new ArrayList<>();
                Boolean listIsEmpty = true;
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    memModel = postSnapshot.getValue(MemModel.class);
                    String uid = postSnapshot.getKey();
                    memModel.uid = uid;
                    memIds.add(uid);
                    memModel.inFavourite = favourites.contains(memModel.url);
                    if (ratedMems.containsKey(uid)){
                        memModel.rate = ratedMems.get(uid);
                    }else{
                        memModel.rate = 0;
                    }
                    if (!getCurrentId) {
                        current_id = memModel.uid;
                        getCurrentId = true;
                    }
                    Log.i(TAG, "ID последнего из загруженных сейчас мемов: " + current_id);
                    if ((memModel.rate == 0) || !deleteRated) {
                        temporary_list.add(memModel);
                        listIsEmpty = false;
                    }
                    Log.i(TAG, memModel.uid + " успешно загружен и добавлен в ленту!");
                }


                if (listIsEmpty){
                    createRecyclerView();
                    fetchData();
                }else{
                    Collections.reverse(temporary_list);
                    memList.addAll(temporary_list);
                    createRecyclerView();
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String errorText = "Произошла ошибка в загрузке мемов";
                Toast.makeText(getContext(), errorText, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void createRecyclerView(){
        Log.i(TAG, "Попытка создания ленты с мемами...");
        adapter = new Adapter(memList);
        manager = new LinearLayoutManager(context);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        Log.i(TAG, "Лента успешно создана!");

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                {
                    isScroll = true;

                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                currentItems = manager.getChildCount();
                totalItems = manager.getItemCount();
                scrolloutItems = manager.findFirstVisibleItemPosition();

                if(isScroll && (currentItems + scrolloutItems == totalItems)){
                    isScroll = false;
                    fetchData();

                }
            }}
        );
    }

    private void fetchData() {
        if (!current_id.equals("UID001")){
        progressBar.setVisibility(View.VISIBLE);
        Log.i(TAG, "ID последнего из загруженных сейчас мемов: " + current_id);
        Query query = mDatabase.child("Memes").orderByKey().limitToLast(limit).endAt(current_id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<MemModel> temporary_list = new ArrayList<>();
                Boolean listIsEmpty = true;
                Boolean getCurrentId = false;
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                        memModel = postSnapshot.getValue(MemModel.class);
                        String uid = postSnapshot.getKey();
                        memModel.uid = uid;
                        memModel.inFavourite = favourites.contains(memModel.url);
                        if (ratedMems.containsKey(uid)){
                            memModel.rate = ratedMems.get(uid);
                        }else{
                            memModel.rate = 0;
                        }
                    if (!getCurrentId) {
                        current_id = memModel.uid;
                        getCurrentId = true;
                    }
                        if ((!memIds.contains(uid)) && ((memModel.rate == 0)|| !deleteRated)){
                            temporary_list.add(memModel);
                            memIds.add(uid);
                            listIsEmpty = false;
                            Log.i(TAG, memModel.uid + " успешно загружен и добавлен в ленту!");
                        }
                    }
                    if (listIsEmpty){
                        fetchData();
                        progressBar.setVisibility(View.GONE);
                    }else{
                        Collections.reverse(temporary_list);
                        memList.addAll(temporary_list);

                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                    }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String errorText = "Произошла ошибка в загрузке мемов";
                Toast.makeText(getContext(), errorText, Toast.LENGTH_SHORT).show();
            }
        });

    }}

    private void updateRecyclerView(){
        memList.clear();
        current_id = "";
        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
        getUserFavourites();
    }

    private void onswipeLenta(){
        mSwipe.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                //обновление свайпера
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateRecyclerView();
                        mSwipe.setRefreshing(false);
                    }
                },1500);
            }
        });

    }

}

