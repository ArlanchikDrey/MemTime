package com.arlhar_membots.Basic.LentaCycle;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.arlhar_membots.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mindorks.placeholderview.SwipeDecor;
import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class CycleMain extends Fragment{

    SwipePlaceHolderView mSwipeView;
    Context mContext;
    Switch aSwitch;
    private View rootView;
    DatabaseReference mDatabase;
    String userID = Objects.requireNonNull(FirebaseAuth.
            getInstance().getCurrentUser()).getUid();

    List<String> favourites = new ArrayList<>();
    HashMap<String, Integer> ratedMems = new HashMap<>();


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.cycle_main, container, false);
        mSwipeView =rootView.findViewById(R.id.swipeView);

        Toolbar toolbar = rootView.findViewById(R.id.toolbar2);

        aSwitch = toolbar.findViewById(R.id.switch2);
        aSwitch.setChecked(true);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        getUserFavourites();
        onUpdateColoda();
        return rootView;
    }

    //обновляем колоду
    private void onUpdateColoda(){
        ImageButton change_coloda=rootView.findViewById(R.id.change_coloda);
        ProgressBar progBar=rootView.findViewById(R.id.progressBar_coloda);

        change_coloda.setOnClickListener(view -> {
            mSwipeView.removeAllViews();
            progBar.setVisibility(View.VISIBLE);
            new Handler().postDelayed(() -> {
                getUserRated();
                progBar.setVisibility(View.GONE);
            },1000);
        });
    }

    public void getUserFavourites() {

        mDatabase.child("Users").child(userID).child("Favourites")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                            String url = postSnapshot.getValue(String.class);
                            favourites.add(url);
                        }
                        getUserRated();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        String errorText = "Произошла ошибка в загрузке избранных мемов";
                        Toast.makeText(mContext, errorText, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void getUserRated(){
        mDatabase.child("Users").child(userID).child("Rated")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                            String uid = postSnapshot.getKey();
                            Integer rating = postSnapshot.getValue(Integer.class);
                            ratedMems.put(uid, rating);
                        }
                        getFirstMems();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        String errorText = "Произошла ошибка в загрузке оценок пользователя";
                        Toast.makeText(mContext, errorText, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getFirstMems(){
        Query query = mDatabase.child("Memes");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mContext = getContext();
                mSwipeView.getBuilder()
                        .setDisplayViewCount(3)
                        .setSwipeDecor(new SwipeDecor()
                                .setPaddingTop(110)
                                .setRelativeScale(0.1f));

                List<String> mem_list = new ArrayList<>();

                for(DataSnapshot memSnapshot: dataSnapshot.getChildren()){
                    String mem_uid = memSnapshot.getKey();
                    mem_list.add(mem_uid);
                }
                Collections.reverse(mem_list);
                for(int i = 0; i < mem_list.size(); i++){
                    mSwipeView.addView(new MemCard(mContext, mSwipeView, mem_list.get(i), ratedMems, favourites));
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                String text = "Произошла ошибка.";
                Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
            }
        });

    }
}
