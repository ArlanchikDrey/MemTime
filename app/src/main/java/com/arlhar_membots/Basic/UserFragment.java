package com.arlhar_membots.Basic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arlhar_membots.Login_and_Regist.Main2Activity;
import com.arlhar_membots.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserFragment extends Fragment {
    private static final int PICK_IMAGE = 1000;

    CircleImageView imgv;//для авы
    private Activity mActivity;
    private TextView textName, textIzbran, textIzbran2;
    private ImageButton btn_delete;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private FirebaseUser user;
    private DatabaseReference datreference;

    //настройки
    ImageView image;
    android.support.v7.widget.Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.userfragment, container, false);
        toolbar = rootView.findViewById(R.id.toolbarchik);


        //добавление аватарки из галерии
        textName = rootView.findViewById(R.id.textViewname);
        textIzbran = rootView.findViewById(R.id.text_Izbran);
        textIzbran2 = rootView.findViewById(R.id.text_Izbran2);
        btn_delete = rootView.findViewById(R.id.btn_delete);

        imgv = rootView.findViewById(R.id.imageView);

        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container2,
                new FragmentMems()).commit();
        firebaseAuth = FirebaseAuth.getInstance();
        datreference = FirebaseDatabase.getInstance().getReference();
        user = firebaseAuth.getCurrentUser();

        try { // получаем ветвь с массивом избранных из базы и отображаем
            datreference.child("Users").child(user.getUid()).child("Favourites").addValueEventListener(new ValueEventListener() {
                List<String> size_izbran = new ArrayList<>();

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    size_izbran.clear();
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        String url = postSnapshot.getValue(String.class);
                        size_izbran.add(url);
                    }
                    textIzbran.setText("Избранное" + "(" + String.valueOf(size_izbran.size()) + ")");

                    if (size_izbran.size() == 0) {
                        textIzbran2.setText("Здесь хранятся ваши избранные мемы");
                    } else {
                        textIzbran2.setText("");
                        btn_delete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Snackbar snackbar = Snackbar
                                        .make(view, "Вы действительно хотите удалить все?", Snackbar.LENGTH_LONG)
                                        .setAction("Да", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                datreference.child("Users").child(user.getUid()).child("Favourites").removeValue();
                                            }
                                        });
                                snackbar.setActionTextColor(Color.RED);
                                snackbar.show();

                            }
                        });
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            });
            firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuthgoogle) {
                    if (user != null) {

                    } else {
                    }
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }


        //настройки и message
        image = rootView.findViewById(R.id.imageButton2);

        set_mes();

        return rootView;
    }

    //фрагмент связывается с активностью
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
    }

    //Вызывается, когда фрагмент отвязывается от активности
    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    private void doAction() {
        if (mActivity == null) {
            return;
        }
        firebaseAuth.addAuthStateListener(firebaseAuthListener);
        try {
            datreference.child("Users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (mActivity == null) {
                        return;
                    }
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String family = dataSnapshot.child("family").getValue(String.class);
                    textName.setText(name + " " + family);
                    String uri = dataSnapshot.child("Avatar").getValue(String.class);
                    Glide.with(mActivity).load(uri).into(imgv);


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // метод при старте фрагмента
    @Override
    public void onStart() {
        super.onStart();
        //метод,в котором есть слушатель с базой, и который получает и отображает данные профиля
        doAction();


    }

    //настройки
    public void set_mes() {
      image.setImageResource(R.drawable.icon_setting);
        //обработчик нажатий настроек
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), Main2Activity.class);
                startActivity(intent);
            }
        });

    }


}

