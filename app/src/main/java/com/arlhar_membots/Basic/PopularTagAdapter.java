package com.arlhar_membots.Basic;

import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import com.arlhar_membots.Basic.LentaCycle.Adapter;
import com.arlhar_membots.Basic.LentaCycle.MemModel;
import com.arlhar_membots.R;
import com.bumptech.glide.Glide;

import java.util.List;
import java.util.zip.Inflater;

public class PopularTagAdapter extends RecyclerView.Adapter<PopularTagAdapter.ViewHolder> {
    List<MemModel> list;

    public PopularTagAdapter(List<MemModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PopularTagAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.popular_tags, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final MemModel memModel=list.get(position);

        Glide.with(holder.itemView.getContext()).load(memModel.url).into(holder.imageView);

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //открываем в окне
                Dialog dialog;
                dialog=new Dialog(holder.itemView.getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_pop);
                //отображаем фото и обрабатываем нажатия
                ImageView im=(ImageView)dialog.findViewById(R.id.imageDialog);
                Glide.with(holder.itemView.getContext())
                        .load(holder.imageView.getDrawable())
                        .into(im);
                dialog.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ViewHolder(View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.imageViewTag);
        }
    }
}
