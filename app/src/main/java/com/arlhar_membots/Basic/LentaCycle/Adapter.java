package com.arlhar_membots.Basic.LentaCycle;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;

import static android.provider.ContactsContract.Intents.Insert.ACTION;

/**
 Как работает класс MemModel?
 В нем есть несколько пунктов, касаемых информации о меме
 1) UID - идентификатор мема
 2) URL - url изображения
 3) TAGS - Теги для мема в формате тег%тег%тег%тег%@
 4) likes - количество лайков
 5) dislikes - количество дизлайков
 6) rate - текущая оценка
 0 - нет оценки на меме
 1 - поставлен лайк
 2 - поставлен дизлайк
 7) inFavourite
 true - Мем находится в избранном
 false - Меме не находится в избранном
 */


public class Adapter extends RecyclerView.Adapter<Adapter.MemViewHolder>{
    public Context context; //Контекст
    private int lastPosition=-1;

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference(); //Ссылка на БД
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); //Ссылка на текущего пользователя (чтобы получить ID ользователя)

    private List<MemModel> list; //Лист с мемами, который приходит в адаптер

    public Adapter(List<MemModel> list){

        this.list = list; //Присваиваем нашему лист, значение, которое пришло сюда в виде аргумента

    }

    @Override
    public MemViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        return new MemViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item_lenta, parent, false));
    }

    @Override
    public void onBindViewHolder(final MemViewHolder holder, int position){
        final MemModel memModel = list.get(position); //Получаем экземпляр объекта MemModel
        Glide.with(context).load(memModel.url).into(holder.memImage);
        holder.tv_likes.setText(memModel.likes.toString()); //Устанавливаем значение лайков в счетчик
        holder.tv_dislikes.setText(memModel.dislikes.toString()); //Устанавливаем значение дизлайков в счетчик
        Float rating = (Float.valueOf(memModel.likes) / Float.valueOf(memModel.likes + memModel.dislikes)) * 100; //Получаем рейтинг
        holder.txtProgress.setText(Math.round(Float.parseFloat(rating.toString())) + "%"); //Устаналиваем рейтинг в счетчик
        holder.progressBar.setProgress(Math.round(rating)); //Устанавливаем значение кольца
        holder.size_comment.setText(String.valueOf(memModel.size_comment));

            if (memModel.inFavourite) { //Если Мем находится в избранном
                holder.btn_izbr.setText("Удалить из избранного"); //Устанавливаем кнопку "Удалить из избранного"
            } else { //Если мем НЕ находится в избранном
                holder.btn_izbr.setText("Добавить в избранное"); //Устанавливаем кнопку "Добавить в избранное"
            }

        if (memModel.rate == 0){ //Если у мема нет оценки
            holder.but_like.setImageResource(R.drawable.like_unpressed); //Ставим картинку: дизлайк НЕ нажат
            holder.but_dislike.setImageResource(R.drawable.dislike_unpressed); //Ставим картинку: лайк НЕ нажат
        }else {
            if (memModel.rate == 1) { //Если у мема стоит ЛАЙК
                holder.but_like.setImageResource(R.drawable.like_pressed); //Ставим картинку: лайк НАЖАТ
                holder.but_dislike.setImageResource(R.drawable.dislike_unpressed); //Ставим картинку: дизлайк НЕ нажат
            } else {
                if (memModel.rate == 2) { //Если у мема стоит ДИЗЛАЙК
                    holder.but_like.setImageResource(R.drawable.like_unpressed); //Ставим картинку: лайк НЕ нажат
                    holder.but_dislike.setImageResource(R.drawable.dislike_pressed); //Ставим картинку: дизлайк НАЖАТ
                }
            }
        }

        int i = 0;
        String tag; //Создаем строку ТЕГ
        List<String> listItems = new ArrayList<>(); //Создаем лист, куда будем класть теги
        char[] tags_c = memModel.tags.toCharArray(); //Разбиваем строку с тегами на символы
        while (tags_c[i] != '@'){ //Проходимся по каждому символу пока не наткнемся на  @
            tag = ""; //Обнуляем строку Тег
            while (tags_c[i] != '%'){ //Пока не наткнемся на знак %
                tag += tags_c[i]; //Добавляем к тегу символ
                i++;
            }
            listItems.add(tag); //Добавляем тег в лист
            i++;
        }
        holder.recyclerViewTags.setHasFixedSize(true);
        holder.recyclerViewTags.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false));
        holder.recyclerViewTags.setAdapter(new HorizontalAdapterLenta(listItems, context)); //Устанавливаем адаптер тегам


        holder.btn_izbr.setOnClickListener(new View.OnClickListener() { //По нажатии на кнопку избранное
            @Override
            public void onClick(View view) {
                if (memModel.inFavourite){ //Если в момент нажатия мем уже был в ибранном
                    removeFromFavourites(memModel.url); //Удалить мем из избранного
                    holder.btn_izbr.setText("Добавить в избранное"); //Поменять кнопку на значение "Добавить в избранное"
                    memModel.inFavourite = false; //Установить значение inFavourite в этом меме на FALSE
                }else{ //ЕСЛИ МЕМ ЕЩЕ НЕ В ИЗБРАННОМ
                    addToFavourites(memModel.url); //Добавить мем в избранное
                    holder.btn_izbr.setText("Удалить из избранного"); //Поменять кнопку на значение "Удалить из избранного"
                    memModel.inFavourite = true; //Установить значение inFavourite в этом меме на TRUE
                }

            }
        });

        holder.but_like.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) { //Если пользователь нажимает на кнопку ЛАЙК
                switch (memModel.rate){ //И в данный момент...
                    case 0: //не стоит НИКАКОЙ оценки
                        mDatabase.child("Memes").child(memModel.uid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                MemModel memModel_now = dataSnapshot.getValue(MemModel.class); //Получаем новые данные об этом меме (обновляем информацию) - вдруг кто то уже лайков 100 влепить успел
                                mDatabase.child("Memes").child(dataSnapshot.getKey())
                                        .child("likes").setValue(memModel_now.likes + 1); //Увеличиваем кол-во лайков в базе на 1
                                memModel.likes = memModel_now.likes + 1; //Увеличиваем кол-во лайков ЗДЕСЬ на 1 и отображаем
                                memModel.dislikes = memModel_now.dislikes; //Дизлайки так просто отображаем (без изменений)
                                holder.tv_likes.setText(memModel.likes.toString());
                                holder.tv_dislikes.setText(memModel.dislikes.toString());
                                Float rating = (Float.valueOf(memModel.likes) / Float.valueOf(memModel.likes + memModel.dislikes)) * 100;
                                holder.txtProgress.setText(Math.round(Float.parseFloat(rating.toString())) + "%");
                                holder.progressBar.setProgress(Math.round(rating));
                                mDatabase.child("Users").child(user.getUid()).child("Rated").child(dataSnapshot.getKey()).setValue(1); //Добавляем в ветку "Оцененный" новый мем и ставим ему значение 1
                                holder.but_like.setImageResource(R.drawable.like_pressed); //делаем кнопку НАЖАТОЙ
                                memModel.rate = 1; //Устанавливаем значение rate в этом меме на 1
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                String text = "Произошла ошибка!";
                                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    case 1:
                        //Уменьшить лайк на 1
                        //Удалить ветку
                        //Графика
                        mDatabase.child("Memes").child(memModel.uid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                MemModel memModel_now = dataSnapshot.getValue(MemModel.class);
                                mDatabase.child("Memes").child(dataSnapshot.getKey())
                                        .child("likes").setValue(memModel_now.likes - 1);
                                memModel.likes = memModel_now.likes - 1;
                                memModel.dislikes = memModel_now.dislikes;
                                holder.tv_likes.setText(memModel.likes.toString());
                                holder.tv_dislikes.setText(memModel.dislikes.toString());
                                Float rating = (Float.valueOf(memModel.likes) / Float.valueOf(memModel.likes + memModel.dislikes)) * 100;
                                holder.txtProgress.setText(Math.round(Float.parseFloat(rating.toString())) + "%");
                                holder.progressBar.setProgress(Math.round(rating));
                                mDatabase.child("Users").child(user.getUid()).child("Rated").child(dataSnapshot.getKey()).removeValue();
                                holder.but_like.setImageResource(R.drawable.like_unpressed);
                                memModel.rate = 0;
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                String text = "Произошла ошибка!";
                                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    case 2:

                        mDatabase.child("Memes").child(memModel.uid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                MemModel memModel_now = dataSnapshot.getValue(MemModel.class);
                                mDatabase.child("Memes").child(dataSnapshot.getKey())
                                        .child("likes").setValue(memModel_now.likes + 1);
                                mDatabase.child("Memes").child(dataSnapshot.getKey())
                                        .child("dislikes").setValue(memModel_now.dislikes - 1);
                                memModel.likes = memModel_now.likes + 1;
                                memModel.dislikes = memModel_now.dislikes - 1;
                                holder.tv_likes.setText(memModel.likes.toString());
                                holder.tv_dislikes.setText(memModel.dislikes.toString());
                                Float rating = (Float.valueOf(memModel.likes) / Float.valueOf(memModel.likes + memModel.dislikes)) * 100;
                                holder.txtProgress.setText(Math.round(Float.parseFloat(rating.toString())) + "%");
                                holder.progressBar.setProgress(Math.round(rating));
                                mDatabase.child("Users").child(user.getUid()).child("Rated").child(dataSnapshot.getKey()).setValue(1);
                                holder.but_like.setImageResource(R.drawable.like_pressed);
                                holder.but_dislike.setImageResource(R.drawable.dislike_unpressed);
                                memModel.rate = 1;
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                String text = "Произошла ошибка!";
                                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                }
            }
        });

        holder.but_dislike.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                switch (memModel.rate){
                    case 0:
                        mDatabase.child("Memes").child(memModel.uid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                MemModel memModel_now = dataSnapshot.getValue(MemModel.class);
                                mDatabase.child("Memes").child(dataSnapshot.getKey())
                                        .child("dislikes").setValue(memModel_now.dislikes + 1);
                                memModel.likes = memModel_now.likes ;
                                memModel.dislikes = memModel_now.dislikes + 1;
                                holder.tv_likes.setText(memModel.likes.toString());
                                holder.tv_dislikes.setText(memModel.dislikes.toString());
                                Float rating = (Float.valueOf(memModel.likes) / Float.valueOf(memModel.likes + memModel.dislikes)) * 100;
                                holder.txtProgress.setText(Math.round(Float.parseFloat(rating.toString())) + "%");
                                holder.progressBar.setProgress(Math.round(rating));
                                mDatabase.child("Users").child(user.getUid()).child("Rated").child(dataSnapshot.getKey()).setValue(2);
                                holder.but_dislike.setImageResource(R.drawable.dislike_pressed);
                                memModel.rate = 2;
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                String text = "Произошла ошибка!";
                                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    case 1:
                        mDatabase.child("Memes").child(memModel.uid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                MemModel memModel_now = dataSnapshot.getValue(MemModel.class);
                                mDatabase.child("Memes").child(dataSnapshot.getKey())
                                        .child("likes").setValue(memModel_now.likes - 1);
                                mDatabase.child("Memes").child(dataSnapshot.getKey())
                                        .child("dislikes").setValue(memModel_now.dislikes + 1);
                                memModel.likes = memModel_now.likes - 1;
                                memModel.dislikes = memModel_now.dislikes + 1;
                                holder.tv_likes.setText(memModel.likes.toString());
                                holder.tv_dislikes.setText(memModel.dislikes.toString());
                                Float rating = (Float.valueOf(memModel.likes) / Float.valueOf(memModel.likes + memModel.dislikes)) * 100;
                                holder.txtProgress.setText(Math.round(Float.parseFloat(rating.toString())) + "%");
                                holder.progressBar.setProgress(Math.round(rating));
                                mDatabase.child("Users").child(user.getUid()).child("Rated").child(dataSnapshot.getKey()).setValue(2);
                                holder.but_like.setImageResource(R.drawable.like_unpressed);
                                holder.but_dislike.setImageResource(R.drawable.dislike_pressed);
                                memModel.rate = 2;
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                String text = "Произошла ошибка!";
                                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    case 2:
                        mDatabase.child("Memes").child(memModel.uid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                MemModel memModel_now = dataSnapshot.getValue(MemModel.class);
                                mDatabase.child("Memes").child(dataSnapshot.getKey())
                                        .child("dislikes").setValue(memModel_now.dislikes - 1);
                                memModel.likes = memModel_now.likes;
                                memModel.dislikes = memModel_now.dislikes - 1;
                                holder.tv_likes.setText(memModel.likes.toString());
                                holder.tv_dislikes.setText(memModel.dislikes.toString());
                                Float rating = (Float.valueOf(memModel.likes) / Float.valueOf(memModel.likes + memModel.dislikes)) * 100;
                                holder.txtProgress.setText(Math.round(Float.parseFloat(rating.toString())) + "%");
                                holder.progressBar.setProgress(Math.round(rating));
                                mDatabase.child("Users").child(user.getUid()).child("Rated").child(dataSnapshot.getKey()).removeValue();
                                holder.but_dislike.setImageResource(R.drawable.dislike_unpressed);
                                memModel.rate = 0;
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                String text = "Произошла ошибка!";
                                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                }
            }
        });

        holder.btn_comment.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {

                Intent intent= new Intent(context,Commentik.class );
                intent.putExtra("image_url",memModel.url);
                intent.putExtra("mem_uid",memModel.uid);
                context.startActivity(intent);
                }
        });

        holder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_SEND);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_TEXT,memModel.url);
                holder.itemView.getContext().startActivity(intent);
            }
        });

        if(memModel.size_comment==null){
            mDatabase.child("Memes").child(memModel.uid).child("size_comment").setValue(0);


        }else if(memModel.size_comment>0){
            mDatabase.child("Memes").child(memModel.uid).child("size_comment").addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    memModel.size_comment=dataSnapshot.getValue(Integer.class);
                    holder.size_comment.setText(String.valueOf(memModel.size_comment));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        }


        @Override
    public int getItemCount(){
        return list.size();
    }


    private void addToFavourites(String url){
        mDatabase= FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(user.getUid()).child("Favourites")
                .push().setValue(url).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Добавлено в избранное!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Произошла ошибка при попытке добавления в избранное",
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


    class MemViewHolder extends RecyclerView.ViewHolder{

        public ProgressBar progressBar,progressPhoto;
        public RecyclerView recyclerViewTags;
        public TextView tv_likes, tv_dislikes, txtProgress,size_comment;
        public ImageButton but_like,but_dislike,btn_comment,share;
        public ImageView memImage;
        public Button btn_izbr;


        public MemViewHolder(View itemView){
            super(itemView);
            context = itemView.getContext();
            txtProgress = itemView.findViewById(R.id.txtProgress); //текст внутри кольца
            progressBar = itemView.findViewById(R.id.progressBar); //кольцо рейтинга
            progressPhoto = itemView.findViewById(R.id.progressBar_item_photo); //кольцо рейтинга
            memImage = itemView.findViewById(R.id.memImage); //Место для мема
            recyclerViewTags = itemView.findViewById(R.id.recyclerViewTags); //RecyclerView тэгов
            tv_likes = itemView.findViewById(R.id.textView_likes); //Лайки
            tv_dislikes = itemView.findViewById(R.id.textView_dislikes); //Дизлайки
            but_like=itemView.findViewById(R.id.btn_like); //Кнопка ЛАЙК
            but_dislike=itemView.findViewById(R.id.btn_dislike); //Кнопка ДИЗЛАЙК
            btn_comment=itemView.findViewById(R.id.btn_comment); //Кнопка комментов
            btn_izbr=itemView.findViewById(R.id.button); //Кнопка ДОБАВИТЬ В ИЗБРАННОЕ
            size_comment=itemView.findViewById(R.id.sizecomments);
            share=itemView.findViewById(R.id.share_lenta);

        }
    }

}

