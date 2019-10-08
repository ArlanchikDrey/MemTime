package com.arlhar_membots.Basic;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.arlhar_membots.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class FragmentMems extends android.support.v4.app.Fragment {
    private DatabaseReference mDatabase;
    private FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
     RecyclerView recyclerView;
     GridLayoutManager layoutManager;
     List<String> izbral=new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vieww = inflater.inflate(R.layout.mems_izbrannoe, container, false);
        recyclerView=vieww.findViewById(R.id.izbran_recycler);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(user.getUid()).child("Favourites").
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        izbral.clear();
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                            String url = postSnapshot.getValue(String.class);
                            izbral.add(url);
                        }
                        Collections.reverse(izbral);

                        layoutManager=new GridLayoutManager(getContext(), 3);
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setHasFixedSize(true);//набор имеет фиксированный Размер
                        IzbrannoeAdapter adapter= new IzbrannoeAdapter(getContext(),izbral);
                        recyclerView.setAdapter(adapter);


                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });




        return vieww;
    }

}

