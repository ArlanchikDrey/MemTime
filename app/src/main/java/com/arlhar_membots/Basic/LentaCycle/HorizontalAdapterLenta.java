package com.arlhar_membots.Basic.LentaCycle;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.arlhar_membots.MemTagsView;
import com.arlhar_membots.R;

import java.util.List;

public class HorizontalAdapterLenta extends RecyclerView
        .Adapter<HorizontalAdapterLenta.HorizontalViewHolder> {
    private Context context;
    List<String> listItems;


    public HorizontalAdapterLenta(List<String> listItems, Context context) {
        this.context = context;
        this.listItems = listItems;
        }


    @Override
    public HorizontalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.tag_card_lenta, parent, false);

        return new HorizontalAdapterLenta.HorizontalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final HorizontalAdapterLenta.HorizontalViewHolder holder, int position) {
        final String listItem = listItems.get(position);
        holder.tagText.setText("#"+listItem);
        holder.tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sTag = listItem;
                Intent intent = new Intent(holder.context, MemTagsView.class);
                intent.putExtra("Tag", sTag);
                holder.context.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return listItems.size();

    }

    public class HorizontalViewHolder extends RecyclerView.ViewHolder {

        TextView tagText;
        CardView tag;
        Context context;


        public HorizontalViewHolder(View view) {
            super(view);
            tagText = view.findViewById(R.id.tagTextView);
            tag = view.findViewById(R.id.tag);
            context = view.getContext();
        }
    }
}