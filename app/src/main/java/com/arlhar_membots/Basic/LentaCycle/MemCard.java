package com.arlhar_membots.Basic.LentaCycle;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arlhar_membots.Basic.UserFragment;
import com.arlhar_membots.Main3Activity;
import com.arlhar_membots.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;
import com.mindorks.placeholderview.annotations.swipe.SwipeCancelState;
import com.mindorks.placeholderview.annotations.swipe.SwipeIn;
import com.mindorks.placeholderview.annotations.swipe.SwipeInState;
import com.mindorks.placeholderview.annotations.swipe.SwipeOut;
import com.mindorks.placeholderview.annotations.swipe.SwipeOutState;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Handler;

@SuppressLint("ValidFragment")
@Layout(R.layout.cycle_card)
public class MemCard extends Fragment {
    @View(R.id.imageView_mem)
    private ImageView profileImageView;

    @View(R.id.imageView_likeclick)
    private ImageView likeclickImage;

    @View(R.id.textView_likes)
    private TextView nameAgeTxtlikes;

    @View(R.id.textView_dis)
    private TextView nameAgeTxtdislikes;

    @View(R.id.btn_like)
    private ImageButton btn_like;

    @View(R.id.btn_dislike)
    private ImageButton btn_dislike;

    @View(R.id.share_cycle)
    private ImageButton share;

    @View(R.id.btn_commentss)
    private ImageButton btn_comment;

    @View(R.id.sizecomments_cycle)
    private TextView sizecomments_cycle;

    @View(R.id.progressBarColoda)
    private ProgressBar progressBar;

    @View(R.id.textRatingColoda)
    private TextView textRating;

    public Context mContext;
    private SwipePlaceHolderView mSwipeView;
    private String myMemId;

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String userID = user.getUid();
    private String tags;
    private List<ListItem> listItems;


    private List<String> mFavourites = new ArrayList<>();
    private HashMap<String, Integer> mRatedMems = new HashMap<>();
    MemModel memModel;

    public MemCard(Context context, SwipePlaceHolderView swipeView,
                   String memId, HashMap<String, Integer> ratedMems, List<String> favourites) {
        myMemId = memId;
        mContext = context;
        mSwipeView = swipeView;
        mRatedMems = ratedMems;
        mFavourites = favourites;
    }

    private void memLike(){
        switch (memModel.rate){
            case 0:
                //Графика
                btn_like.setImageResource(R.drawable.like_pressed);
                btn_dislike.setImageResource(R.drawable.dislike_unpressed);
                //Изменение значений счетчика
                nameAgeTxtlikes.setText(String.valueOf(Integer.valueOf(nameAgeTxtlikes.getText().toString()) + 1));
                //Далее постоянный слушатель сам подтянет новые значения в TextView
                //Изменение значения в базе данных
                mDatabase.child("Memes").child(myMemId).child("likes")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Integer count_likes = dataSnapshot.getValue(Integer.class);
                                count_likes++;
                                mDatabase.child("Memes").child(myMemId).child("likes")
                                        .setValue(count_likes);
                                mDatabase.child("Users").child(userID).child("Rated").child(myMemId)
                                        .setValue(1);
                                memModel.rate = 1;
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                onError();

                            }
                        });
                break;
            case 1:
                //Графика
                btn_like.setImageResource(R.drawable.like_unpressed);
                btn_dislike.setImageResource(R.drawable.dislike_unpressed);
                //Изменение значений счетчика
                nameAgeTxtlikes.setText(String.valueOf(Integer.valueOf(nameAgeTxtlikes.getText().toString()) - 1));
                //Далее постоянный слушатель сам подтянет новые значения в TextView
                //Изменение значения в базе данных
                mDatabase.child("Memes").child(myMemId).child("likes")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Integer count_likes = dataSnapshot.getValue(Integer.class);
                                count_likes--;
                                mDatabase.child("Memes").child(myMemId).child("likes")
                                        .setValue(count_likes);
                                mDatabase.child("Users").child(userID).child("Rated").child(myMemId)
                                        .removeValue();
                                memModel.rate = 0;
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                onError();
                            }
                        });
                break;
            case 2:
                //Графика
                btn_like.setImageResource(R.drawable.like_pressed);
                btn_dislike.setImageResource(R.drawable.dislike_unpressed);
                //Изменение значений счетчика
                nameAgeTxtlikes.setText(String.valueOf(Integer.valueOf(nameAgeTxtlikes.getText().toString()) + 1));
                nameAgeTxtlikes.setText(String.valueOf(Integer.valueOf(nameAgeTxtdislikes.getText().toString()) - 1));
                //Далее постоянный слушатель сам подтянет новые значения в TextView
                //Изменение значения в базе данных
                mDatabase.child("Memes").child(myMemId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                MemModel likedisModel = dataSnapshot.getValue(MemModel.class);
                                likedisModel.likes++;
                                likedisModel.dislikes--;
                                mDatabase.child("Memes").child(myMemId).child("likes")
                                        .setValue(likedisModel.likes);
                                mDatabase.child("Memes").child(myMemId).child("dislikes")
                                        .setValue(likedisModel.dislikes);
                                mDatabase.child("Users").child(userID).child("Rated").child(myMemId)
                                        .setValue(1);
                                memModel.rate = 1;
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                onError();
                            }
                        });
                break;
        }
    }//метод для лайков

    private void dis(){
        {
            switch (memModel.rate){
                case 0:
                    //Графика
                    btn_like.setImageResource(R.drawable.like_unpressed);
                    btn_dislike.setImageResource(R.drawable.dislike_pressed);
                    //Изменение значений счетчика
                    nameAgeTxtdislikes.setText(String.valueOf(Integer.valueOf(nameAgeTxtdislikes.getText().toString()) + 1));
                    //Далее постоянный слушатель сам подтянет новые значения в TextView
                    //Изменение значения в базе данных
                    mDatabase.child("Memes").child(myMemId).child("dislikes")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Integer count_dislikes = dataSnapshot.getValue(Integer.class);
                                    count_dislikes++;
                                    mDatabase.child("Memes").child(myMemId).child("dislikes")
                                            .setValue(count_dislikes);
                                    mDatabase.child("Users").child(userID).child("Rated").child(myMemId)
                                            .setValue(2);
                                    memModel.rate = 2;
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    onError();

                                }
                            });
                    break;
                case 1:
                    //Графика
                    btn_like.setImageResource(R.drawable.like_unpressed);
                    btn_dislike.setImageResource(R.drawable.dislike_pressed);
                    //Изменение значений счетчика
                    nameAgeTxtlikes.setText(String.valueOf(Integer.valueOf(nameAgeTxtlikes.getText().toString()) - 1));
                    nameAgeTxtlikes.setText(String.valueOf(Integer.valueOf(nameAgeTxtdislikes.getText().toString()) + 1));
                    //Далее постоянный слушатель сам подтянет новые значения в TextView
                    //Изменение значения в базе данных
                    mDatabase.child("Memes").child(myMemId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    MemModel likedisModel = dataSnapshot.getValue(MemModel.class);
                                    likedisModel.likes--;
                                    likedisModel.dislikes++;
                                    mDatabase.child("Memes").child(myMemId).child("likes")
                                            .setValue(likedisModel.likes);
                                    mDatabase.child("Memes").child(myMemId).child("dislikes")
                                            .setValue(likedisModel.dislikes);
                                    mDatabase.child("Users").child(userID).child("Rated").child(myMemId)
                                            .setValue(2);
                                    memModel.rate = 2;
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    onError();
                                }
                            });
                    break;
                case 2:
                    //Графика
                    btn_like.setImageResource(R.drawable.like_unpressed);
                    btn_dislike.setImageResource(R.drawable.dislike_unpressed);
                    //Изменение значений счетчика
                    nameAgeTxtdislikes.setText(String.valueOf(Integer.valueOf(nameAgeTxtdislikes.getText().toString()) - 1));
                    //Далее постоянный слушатель сам подтянет новые значения в TextView
                    //Изменение значения в базе данных
                    mDatabase.child("Memes").child(myMemId).child("dislikes")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Integer count_dislikes = dataSnapshot.getValue(Integer.class);
                                    count_dislikes--;
                                    mDatabase.child("Memes").child(myMemId).child("dislikes")
                                            .setValue(count_dislikes);
                                    mDatabase.child("Users").child(userID).child("Rated").child(myMemId)
                                            .removeValue();
                                    memModel.rate = 0;
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    onError();
                                }
                            });
                    break;
            }
        }
    }//метод для дизов

    @SuppressLint("ClickableViewAccessibility")
    @Resolve
    private void onResolved() {
        listItems = new ArrayList<>();
        //При первом запуске
        mDatabase.child("Memes").child(myMemId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        memModel = dataSnapshot.getValue(MemModel.class);
                        assert memModel != null;
                        Glide.with(mContext).load(memModel.url).into(profileImageView); //Изображение
                        nameAgeTxtlikes.setText(String.valueOf(memModel.likes)); //Лайки
                        nameAgeTxtdislikes.setText(String.valueOf(memModel.dislikes)); //Дизлайки
                        sizecomments_cycle.setText(String.valueOf(memModel.size_comment));//комменты
                        Float rating = (Float.valueOf(memModel.likes) /
                                Float.valueOf(memModel.likes + memModel.dislikes)) * 100; //Получаем рейтинг

                        textRating.setText(Math.round(Float.parseFloat(rating.toString())) + "%"); //Устаналиваем рейтинг в счетчик
                        progressBar.setProgress(Math.round(rating)); //Устанавливаем значение кольца


                        if (mRatedMems.containsKey(myMemId)){
                            memModel.rate = mRatedMems.get(myMemId);
                            switch (mRatedMems.get(myMemId)){
                                case 1:
                                    //Like
                                    btn_like.setImageResource(R.drawable.like_pressed);
                                    btn_dislike.setImageResource(R.drawable.dislike_unpressed);
                                    break;
                                case 2:
                                    //Dislike
                                    btn_like.setImageResource(R.drawable.like_unpressed);
                                    btn_dislike.setImageResource(R.drawable.dislike_pressed);
                                    break;
                            }
                        }else{
                            //Все кнопки выставить на НЕ НАЖАТЫ
                            memModel.rate = 0;
                            btn_like.setImageResource(R.drawable.like_unpressed);
                            btn_dislike.setImageResource(R.drawable.dislike_unpressed);
                        }
                        memModel.inFavourite = mFavourites.contains(memModel.url);
                        if (memModel.inFavourite){
                           // btn_favourite.setImageResource(R.drawable.star_pressed);
                        }else{
                          //  btn_favourite.setImageResource(R.drawable.star_unpressed);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        //Слушатель на каждый раз
        mDatabase.child("Memes").child(myMemId).
                addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MemModel memModel = dataSnapshot.getValue(MemModel.class);
                nameAgeTxtlikes.setText(String.valueOf(memModel.likes)); //Лайки
                nameAgeTxtdislikes.setText(String.valueOf(memModel.dislikes)); //Дизлайки
                sizecomments_cycle.setText(String.valueOf(memModel.size_comment));
                Float rating = (Float.valueOf(memModel.likes)
                        / Float.valueOf(memModel.likes + memModel.dislikes)) * 100; //Получаем рейтинг
                textRating.setText(Math.round(Float.parseFloat(rating.toString())) + "%"); //Устаналиваем рейтинг в счетчик
                progressBar.setProgress(Math.round(rating)); //Устанавливаем значение кольца

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onError();
            }
        });

        //двойное нажатие на фото
        profileImageView.setOnTouchListener(new android.view.View.OnTouchListener() {
            final long[] startTime1 = {System.currentTimeMillis()};
            final long[] elapsedTime1 = {0};
            @Override
            public boolean onTouch(android.view.View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                {
                    elapsedTime1[0] = System.currentTimeMillis() - startTime1[0];
                    if (elapsedTime1[0] > 500)
                    {
                        startTime1[0] = System.currentTimeMillis();
                        return false;
                    }
                    else
                    {
                        if (elapsedTime1[0] > 50)
                        {
                            memLike(); //метод для кнопки лайка
                            startTime1[0] = System.currentTimeMillis();

                                switch (memModel.rate){
                                    case 0:
                                        likeclickImage.setVisibility(android.view.View.VISIBLE);
                                        likeclickImage.setImageResource(R.drawable.like_pressed);
                                        new android.os.Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                likeclickImage.setVisibility(android.view.View.GONE);
                                            }
                                        },1000);
                                        break;
                                    case 1:
                                        likeclickImage.setVisibility(android.view.View.VISIBLE);
                                        likeclickImage.setImageResource(R.drawable.like_unpressed);
                                        new android.os.Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                likeclickImage.setVisibility(android.view.View.GONE);
                                            }
                                        },1000);
                                        break;

                                    case 2:
                                        likeclickImage.setVisibility(android.view.View.VISIBLE);
                                        likeclickImage.setImageResource(R.drawable.like_pressed);
                                        new android.os.Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                likeclickImage.setVisibility(android.view.View.GONE);
                                            }
                                        },1000);
                                        break;
                                        }
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        btn_like.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View view) {
                memLike();
            }
        });

        btn_dislike.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View view) {
                dis();
            }
        });

        btn_comment.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View view) {

                Intent intents= new Intent(mContext, Commentik.class);
                intents.putExtra("image_url",memModel.url);
                intents.putExtra("mem_uid",myMemId);
                mContext.startActivity(intents);
                }
        });

        share.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View view) {
                PopupMenu popupMenu = new PopupMenu(mContext, view);
                popupMenu.inflate(R.menu.share_favourite);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.menu1:
                                if (memModel.inFavourite){
                                    String text = "Удалено из избранного";
                                    Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
                                    memModel.inFavourite = false;
                                    removeFromFavourites(memModel.url);
                                }else{
                                    String text = "Добавлено в избранное!";
                                    Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
                                    memModel.inFavourite = true;
                                    addToFavourites(memModel.url);
                                }
                                return true;
                            case R.id.menu2:
                                Intent intent=new Intent(Intent.ACTION_SEND);
                                intent.setType("*/*");
                                intent.putExtra(Intent.EXTRA_TEXT,memModel.url);
                                mContext.startActivity(intent);
                                return true;

                        }
                        return true;
                    }
                });
                popupMenu.show();


            }
        });


    }


    /** public void addNewTag(String tag){

     recyclerView.setHasFixedSize(true);
     recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
     tags = memModel.tags; //Тэги
     Log.i("FBtest", tags);
     char[] tags_c = tags.toCharArray();
     String tag;
     int i = 0;
     while (tags_c[i] != '@'){
     tag = "";
     while (tags_c[i] != '%'){
     tag += tags_c[i];
     i++;
     }
     addNewTag(tag);
     i++;
     }
     recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL,false));
     recyclerView.setAdapter(new HorizontalAdapter(listItems, mContext));

     //Добавляет данные в массив
        ListItem listItem = new ListItem(tag);
        listItems.add(listItem);
    }*/

    @SwipeOut
    private void onSwipedOut() {
        Log.i("EVENT9", "onSwipedOut");
            String memIdHere = makeNewUid();
            mSwipeView.addView(new MemCard(mContext, mSwipeView, memIdHere, mRatedMems, mFavourites));


    }

    @SwipeCancelState
    private void onSwipeCancelState() {
        Log.i("EVENT9", "onSwipeCancelState");
    }

    @SwipeIn
    private void onSwipeIn() {
        Log.i("EVENT9", "onSwipedIn");
            String memIdHere = makeNewUid();
            Log.i("EVENT9", memIdHere);
            mSwipeView.addView(new MemCard(mContext, mSwipeView, memIdHere, mRatedMems, mFavourites));



    }

    @SwipeInState
    private void onSwipeInState() {
        Log.i("EVENT9", "onSwipeInState");
    }

    @SwipeOutState
    private void onSwipeOutState() {
        Log.i("EVENT9", "onSwipeOutState");
    }

    private static Integer LIMIT =3;
    private String makeNewUid(){
        String current_id = myMemId;
        Log.i("EVENT9", current_id);
        current_id = current_id.replaceAll("UID", "");
        Log.i("EVENT9", current_id);
        Integer current_mem_nomber = Integer.valueOf(current_id);
        Log.i("EVENT9",current_mem_nomber.toString());
       // current_mem_nomber -= LIMIT;
        if (current_mem_nomber > 0) {

            Log.i("EVENT9", current_mem_nomber.toString());
            String new_uid = "UID";
            switch (current_mem_nomber.toString().length()) {
                case 1:
                    new_uid += "00";
                    break;
                case 2:
                    new_uid += "0";
                    break;
            }
            new_uid += current_mem_nomber.toString();

            return new_uid;
        }else{
            return "";
        }
    }
    public void onError(){
        String text = "Произошла ошибка связи с сервером.";
        Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
    }

    private void addToFavourites(String url){
        mDatabase= FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(user.getUid()).child("Favourites")
                .push().setValue(url).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(mContext, "Добавленно в избранное!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(mContext, "Произошла ошибка при попытке добавления в избранное",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeFromFavourites(String url){
        Log.i("DANTEST", url);
        Query query = mDatabase.child("Users").child(user.getUid())
                .child("Favourites").orderByValue().equalTo(url);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String key="";
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()){
                    key = childSnapshot.getKey();
                }

                Log.i("DANTEST", "key = "+key);
                mDatabase.child("Users").child(user.getUid()).child("Favourites")
                        .child(key).removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



}