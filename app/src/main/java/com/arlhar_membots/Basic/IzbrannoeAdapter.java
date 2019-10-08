package com.arlhar_membots.Basic;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;


public  class IzbrannoeAdapter extends RecyclerView.Adapter<IzbrannoeAdapter.ViewHolder> {
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
    Dialog dialog;
    private Context context;
    List<String> izbral;

    public IzbrannoeAdapter(Context context,List<String>list){
        this.context=context;
        this.izbral=list;
    }

     class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
         ViewHolder(View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.imageIzbra);
        }
    }

    @Override
    public IzbrannoeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_layout_izbran,parent,false);

        return new IzbrannoeAdapter.ViewHolder(view);
    }
    //создаем по позициям с пикассо и вызываем слушатель нажатия на фото
    @Override
    public void onBindViewHolder(IzbrannoeAdapter.ViewHolder holder, final int position) {

        Picasso.with(context).load(izbral.get(position)).resize(145,150).into(holder.imageView);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //открываем в окне
                dialog=new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_pop);
                //отображаем фото и обрабатываем нажатия
                ImageView im = dialog.findViewById(R.id.imageDialog);
                Picasso.with(context)
                        .load(izbral.get(position))
                        .into(im, new Callback() {
                            @Override
                            public void onSuccess() {}
                            @Override
                            public void onError() {
                                Toast.makeText(context, "Ошибка загрузки", Toast.LENGTH_SHORT).show();}
                        });
                dialog.show();
            }
        });

        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case DialogInterface.BUTTON_POSITIVE:
                                //YES
                                removeFromFavourites(izbral.get(position));
                                removeItem(izbral.get(position));
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                //NO
                                builder.create().dismiss();
                                break;
                        }
                    }
                };

                builder.setMessage("Удалить мем из избранного?")
                        .setPositiveButton("Да", dialogClickListener)
                            .setNegativeButton("Нет", dialogClickListener).show();

                return true;
            }
        });
    }

    private void removeItem(String url) {
        int position = izbral.indexOf(url);
        izbral.remove(position);
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
    public int getItemCount() {
        return izbral.size();
    }


    public void removeFromFavourites(String url){

        Query query = mDatabase.child("Users").child(user.getUid())
                .child("Favourites").orderByValue().equalTo(url);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String key="";
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()){
                    key = childSnapshot.getKey();
                }

                Log.i("DANTEST", "key = " + key);
                mDatabase.child("Users").child(user.getUid()).child("Favourites")
                        .child(key).removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
