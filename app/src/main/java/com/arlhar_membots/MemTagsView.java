package com.arlhar_membots;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.arlhar_membots.Basic.LentaCycle.Adapter;
import com.arlhar_membots.Basic.LentaCycle.MemModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MemTagsView extends AppCompatActivity {

    private String currentTag;
    ProgressBar mtv_progressBar;
    RecyclerView mtv_recyclerView;
    DatabaseReference mDatabase;

    String userID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    List<String> favourites = new ArrayList<>();
    HashMap<String, Integer> ratedMems = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mem_tags_view);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mtv_progressBar = findViewById(R.id.mtv_progressBar);
        mtv_recyclerView = findViewById(R.id.mtv_recyclerView);
        mtv_recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = getIntent();
        Log.i("MemTagsView", "Распаковка тега прошла успешно!");
        currentTag = intent.getStringExtra("Tag");
        Log.i("MemTagsView", "Найденный тег: " + currentTag);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        this.setTitle("#" + currentTag);
        Log.i("MemTagsView", "Заголовок успешо создан!");
        getAllMemesWithTag();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    List<MemModel> memes = new ArrayList<>();

    private void getAllMemesWithTag() {
        Log.i("MemTagsView", "getAllMemesWithTag() запущен");
        mtv_progressBar.setVisibility(View.VISIBLE);
        getUserFavourites();
    }

    private void setRecyclerView() {
        Log.i("MemTagsView", "setRecyclerView() запущен");
        if (memes.isEmpty()) {
            Toast.makeText(this, "Мемов с таким тэгом еще нет :(", Toast.LENGTH_SHORT).show();
            Log.i("MemTagsView", "Список пуст");
        } else {
            Log.i("MemTagsView", "Метод успешно получил список");
            Collections.reverse(memes);
            Adapter adapter = new Adapter(memes);
            mtv_recyclerView.setAdapter(adapter);
        }
    }

    public void getUserFavourites() {

        mDatabase.child("Users").child(userID).child("Favourites")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            String url = postSnapshot.getValue(String.class);
                            favourites.add(url);
                        }
                        getUserRated();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        String errorText = "Произошла ошибка в загрузке избранных мемов";
                        Toast.makeText(getApplicationContext(), errorText, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void getUserRated() {
        mDatabase.child("Users").child(userID).child("Rated")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            String uid = postSnapshot.getKey();
                            Integer rating = postSnapshot.getValue(Integer.class);
                            ratedMems.put(uid, rating);
                        }
                        getMemes();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        String errorText = "Произошла ошибка в загрузке оценок пользователя";
                        Toast.makeText(getApplicationContext(), errorText, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getMemes() {
        mDatabase.child("Memes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("MemTagsView", "onDataChange() запущен");
                for (DataSnapshot mem : dataSnapshot.getChildren()) {
                    MemModel memModel = mem.getValue(MemModel.class); //Инициализируем модель
                    String tags = null;
                    if (memModel != null) {
                        tags = memModel.tags;
                    }
                    Log.i("MemTagsView", "Получена строка тегов: " + tags);
                    int i = 0;
                    StringBuilder tag; //Создаем строку ТЕГ
                    char[] tags_c = new char[0]; //Разбиваем строку с тегами на символы
                    if (tags != null) {
                        tags_c = tags.toCharArray();
                    }
                    while (tags_c[i] != '@') { //Проходимся по каждому символу пока не наткнемся на  @
                        tag = new StringBuilder(); //Обнуляем строку Тег
                        while (tags_c[i] != '%') { //Пока не наткнемся на знак %
                            tag.append(tags_c[i]); //Добавляем к тегу символ
                            i++;
                        }
                        Log.i("MemTagsView", "Найден тег: " + tag);
                        if (tag.toString().equals(currentTag)) {
                            Log.i("MemTagsView", "Тег совпадает с требуемым.");
                            String uid = mem.getKey();
                            if (memModel != null) {
                                memModel.uid = uid;
                                memModel.inFavourite = favourites.contains(memModel.url);
                                if (ratedMems.containsKey(uid)) {
                                    memModel.rate = ratedMems.get(uid);
                                } else {
                                    memModel.rate = 0;
                                }
                                memes.add(memModel);
                                Log.i("MemTagsView", "Мем успешно добавлен в список.");
                            }
                        }
                        i++;
                    }
                }
                mtv_progressBar.setVisibility(View.GONE);
                setRecyclerView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}