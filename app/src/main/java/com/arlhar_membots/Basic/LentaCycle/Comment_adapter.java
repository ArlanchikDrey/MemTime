package com.arlhar_membots.Basic.LentaCycle;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.arlhar_membots.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;


public class Comment_adapter extends RecyclerView.Adapter<Comment_adapter.ViewHolder> {

      List<CommentList> list_comments; //Лист с комментами, который приходит в адаптер
    private String name,family,Avatar;
    private Integer rate;


    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference(); //Ссылка на БД
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); //Ссылка на текущего пользователя (чтобы получить ID ользователя)
    private FirebaseAuth auth;


    public Comment_adapter(List<CommentList> list_comments){

        this.list_comments = list_comments;

    }

    @NonNull
    @Override
    public Comment_adapter.ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.comment_item, parent, false);
        return new Comment_adapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {



        final CommentList commentList=list_comments.get(position);
        holder.textTimes.setText(commentList.time);
        holder.text_comment.setText(commentList.text_comments);
        if(commentList.likes_com!=null){
            holder.likes_comText.setText(String.valueOf(commentList.likes_com));
        }


        if (commentList.rate == 0){ //Если у мема нет оценки
            holder.like_comment.setImageResource(R.drawable.like_unpress_mini); //Ставим картинку: дизлайк НЕ нажат
        }else {
            if (commentList.rate == 1) { //Если у мема стоит ЛАЙК
                holder.like_comment.setImageResource(R.drawable.like_pressedmini); //Ставим картинку: лайк НАЖАТ
            }}
         //отображение имени и авы
        mDatabase.child("Users").child(commentList.author).child("name").
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        name=dataSnapshot.getValue(String.class);
                        holder.text_name.setText(name);


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        mDatabase.child("Users").child(commentList.author).child("family").
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        family=dataSnapshot.getValue(String.class);
                        holder.text_name.setText(name+" "+family);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        mDatabase.child("Users").child(commentList.author).child("Avatar").
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Avatar=dataSnapshot.getValue(String.class);
                        try{
                            Glide.with(holder.itemView.getContext()).load(Avatar).into(holder.image_ava);

                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                });

        //нажатие на лайк коммента(11.10.2018)
            holder.like_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (commentList.rate){ //И в данный момент...
                        case 0: //не стоит НИКАКОЙ оценки
                            mDatabase.child("Memes").child(Commentik.uid).child("Comments").child(commentList.comment_key)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    CommentList commentList1=dataSnapshot.getValue(CommentList.class);
                                    mDatabase.child("Memes").child(Commentik.uid).child("Comments").
                                            child(dataSnapshot.getKey()).child("likes_com").setValue(commentList1.likes_com+1);
                                    commentList.likes_com=commentList1.likes_com+1;
                                    holder.likes_comText.setText(String.valueOf(commentList.likes_com));
                                    mDatabase.child("Users").child(user.getUid()).child("Rated_comment").child(dataSnapshot.getKey()).setValue(1);
                                    holder.like_comment.setImageResource(R.drawable.like_pressedmini);
                                    commentList.rate = 1;
                                    }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    String text = "Произошла ошибка!";
                                    Toast.makeText(holder.itemView.getContext(), text, Toast.LENGTH_SHORT).show();
                                }
                            });

                            break;
                        case 1:
                            mDatabase.child("Memes").child(Commentik.uid).child("Comments").child(commentList.comment_key)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    CommentList commentList1=dataSnapshot.getValue(CommentList.class);
                                    mDatabase.child("Memes").child(Commentik.uid).child("Comments").
                                            child(dataSnapshot.getKey()).child("likes_com").setValue(commentList1.likes_com-1);
                                    commentList.likes_com=commentList1.likes_com-1;
                                    holder.likes_comText.setText(String.valueOf(commentList.likes_com));
                                    mDatabase.child("Users").child(user.getUid()).child("Rated_comment").child(dataSnapshot.getKey()).setValue(0);
                                    holder.like_comment.setImageResource(R.drawable.like_unpress_mini);
                                    commentList.rate = 0;

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    String text = "Произошла ошибка!";
                                    Toast.makeText(holder.itemView.getContext(), text, Toast.LENGTH_SHORT).show();
                                }
                            });

                            break;


                    }}});

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case DialogInterface.BUTTON_POSITIVE:
                                //YES
                                //Удаление

                                auth=FirebaseAuth.getInstance();
                                String user_id= auth.getCurrentUser().getUid();
                                if(commentList.author.equals(user_id)){
                                    mDatabase.child("Memes").child(Commentik.uid).child("Comments").
                                            child(commentList.comment_key).removeValue();
                                    mDatabase.child("Users").child(user_id).child("Rated_comment").
                                            child(commentList.comment_key).removeValue();//удаление оценки
                                    Toast.makeText(holder.itemView.getContext(), "Комментарий успешно удален", Toast.LENGTH_SHORT).show();

                                    //слушатель для уменьшения количества комментов при удалении
                                    mDatabase.child("Memes").child(Commentik.uid).child("size_comment").
                                            addListenerForSingleValueEvent(new ValueEventListener() {
                                                Integer sizeComments;
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    sizeComments=dataSnapshot.getValue(Integer.class);
                                                    if(sizeComments>=1){
                                                        mDatabase.child("Memes").child(Commentik.uid).child("size_comment").setValue(sizeComments-1);

                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }

                                            });

                                    removeItem(list_comments.get(position));//удаляем item
                                }else{
                                    Toast.makeText(holder.itemView.getContext(), "Ошибка удаления", Toast.LENGTH_SHORT).show();
                                }

                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                //NO
                                builder.create().dismiss();
                                break;
                        }
                    }
                };

                builder.setMessage("Удалить коммент?")
                        .setPositiveButton("Да", dialogClickListener)
                        .setNegativeButton("Нет", dialogClickListener).show();

                return true;
            }
        });
        }//конец onBindViewHolder


    private void removeItem(CommentList url) {
        int position = list_comments.indexOf(url);
        list_comments.remove(position);
        notifyItemRemoved(position);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        },1000);


    }


    @Override
    public  int getItemCount() {
        return list_comments.size() ;
    }


    class ViewHolder extends RecyclerView.ViewHolder{
        de.hdodenhof.circleimageview.CircleImageView image_ava;
        TextView text_name,textTimes,text_comment,likes_comText;
        ImageButton like_comment;
        ViewHolder(View itemView) {
            super(itemView);
            image_ava=itemView.findViewById(R.id.comment_avatar);
            text_name=itemView.findViewById(R.id.text_name);
            textTimes=itemView.findViewById(R.id.textTimes);
            text_comment=itemView.findViewById(R.id.text_comment);
            like_comment=itemView.findViewById(R.id.like_btn_comment);
            likes_comText=itemView.findViewById(R.id.text_likescomment);

            
        }
    }
}
