package com.arlhar_membots.Basic.LentaCycle;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Документация
 * ------------переменные------------
 * editText-для ввода текста пользователя
 * btn_send-кнопка для отправки текста в базу
 * uid-это id мема,получаем его из класса LentaFragment
 * comment_key-ключ комментария
 * comList-массив модели коммента
 * comIds- массив id комментов
 * ratedComments- ключ и значение оценивания(включает uid как ключ)
 * mSwipe- свайп

 * */
public class Commentik extends Activity {
    private EditText editText;//
    private FloatingActionButton btn_send;
    private ImageButton btn_back;
    RecyclerView recyclerView;
    private SwipyRefreshLayout mSwipe;
    LinearLayoutManager layoutManager;
    List<CommentList> comList;
    List<String> comIds = new ArrayList<>();
    HashMap<String, Integer> ratedComments = new HashMap<>();

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference(); //Ссылка на БД
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String comment_key;
    static String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.commentik);
        isOnline(this);
        uid = getIntent().getStringExtra("mem_uid");

        editText = findViewById(R.id.editText_comment);
        btn_send = findViewById(R.id.btn_send);
        mSwipe=findViewById(R.id.swipeRefreshLayout);
        btn_back=findViewById(R.id.btnBack);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });//назад в ленту

        comList = new ArrayList<CommentList>();
        getUserRated();

        recyclerView = findViewById(R.id.recycler_comment);
        recyclerView.setHasFixedSize(true);//набор имеет фиксированный Размер
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new Comment_adapter(comList));

        //слушатель свайпа для обновления адаптера
        mSwipe.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                //обновление свайпера
                new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getUserRated();
                            mSwipe.setRefreshing(false);
                        }
                    },1500);
                    }
        });
        mSwipe.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.CYAN);

        }

    //ветвь оценки user
    public void getUserRated() {
        mDatabase.child("Users").child(user.getUid()).child("Rated_comment")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            String uid = postSnapshot.getKey();
                            Integer rating = postSnapshot.getValue(Integer.class);
                            ratedComments.put(uid, rating);
                        }
                        updateRecycler();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        String errorText = "Произошла ошибка в загрузке оценок пользователя";
                        Toast.makeText(getApplicationContext(), errorText, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void updateRecycler() {
        DatabaseReference mData = FirebaseDatabase.getInstance().getReference().child("Memes").child(uid).child("Comments");
        Query query = mData.limitToLast(10000);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            CommentList commentiki;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                comList.clear();
                ArrayList<CommentList> temporary_list = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    commentiki = postSnapshot.getValue(CommentList.class);
                    comment_key = postSnapshot.getKey();
                    commentiki.comment_key = comment_key;
                    comList.add(commentiki);
                    comIds.add(comment_key);
                    if (ratedComments.containsKey(comment_key)) {
                        commentiki.rate = ratedComments.get(comment_key);
                    } else {
                        commentiki.rate = 0;
                    }
                    if ((commentiki.rate == 0)) {
                        temporary_list.add(commentiki);

                    }
                }
                recyclerView.getAdapter().notifyDataSetChanged();
                recyclerView.scrollToPosition(comList.size() - 1);
                if(recyclerView.getAdapter().getItemCount()==0){
                    recyclerView.setAdapter(new EmptyAdapter());
                }else{
                    recyclerView.setAdapter(new Comment_adapter(comList));
                }

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    //метод для кнопки отправить коммент
    public void send(View view) {
        if(!isOnline(this)){

        }else{
            if (editText.getText().toString().equals("")) {
                Toast.makeText(getApplicationContext(), "Ошибка,пустой комментарий", Toast.LENGTH_SHORT).show();
            } else {

                comment_key = mDatabase.child("Memes").child(uid).child("Comments").push().getKey();
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM в HH:mm");
                String date_time = simpleDateFormat.format(calendar.getTime());

                Map<String, Object> usermap = new HashMap<>();
                usermap.put("author", user.getUid());
                usermap.put("text_comments", editText.getText().toString());
                usermap.put("time", date_time);
                usermap.put("likes_com", 0);

                mDatabase.child("Memes").child(uid).child("Comments").child(comment_key)
                        .setValue(usermap);

                mDatabase.child("Memes").child(uid).child("size_comment").addListenerForSingleValueEvent(new ValueEventListener() {
                    Integer sizeComments;

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        sizeComments = dataSnapshot.getValue(Integer.class);
                        mDatabase.child("Memes").child(uid).child("size_comment").setValue(sizeComments + 1);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("error", databaseError.toString());

                    }

                });

                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getUserRated();

                    }
                }, 1000);

                editText.setText("");

            }
        }

    }

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





