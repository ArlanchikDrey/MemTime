package com.arlhar_membots.Basic;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.arlhar_membots.Basic.LentaCycle.MemModel;
import com.arlhar_membots.R;
import com.bumptech.glide.Glide;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;




public class RecyclerPopAdapter extends RecyclerView.Adapter<RecyclerPopAdapter.ViewHolder> {
    private List<MemModel> memes;


     public RecyclerPopAdapter(List<MemModel> memes){
        this.memes=memes;
    }

    @Override
    public RecyclerPopAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.card_popul,parent,false);
        return new RecyclerPopAdapter.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(RecyclerPopAdapter.ViewHolder holder, int position) {
         holder.getAdapterPosition();
        final MemModel memModel=memes.get(position);
        Picasso.with(holder.itemView.getContext()).load(memModel.url).into(holder.image);

        holder.image.setOnClickListener(view -> {
            //открываем в окне
            Dialog dialog;
            dialog=new Dialog(holder.itemView.getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_pop);
            //отображаем фото и обрабатываем нажатия
            ImageView im=(ImageView)dialog.findViewById(R.id.imageDialog);
            Glide.with(holder.itemView.getContext())
                    .load(holder.image.getDrawable())
                    .into(im);
            dialog.show();
        });


    }

    @Override
    public int getItemCount() {
        return memes.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        ViewHolder(View itemView) {
            super(itemView);
            image=itemView.findViewById(R.id.imagepopul);
        }
    }
}




