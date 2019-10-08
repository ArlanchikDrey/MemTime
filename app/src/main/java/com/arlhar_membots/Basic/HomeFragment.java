package com.arlhar_membots.Basic;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arlhar_membots.Basic.LentaCycle.Commentik;
import com.arlhar_membots.Basic.LentaCycle.MemModel;
import com.arlhar_membots.MemTagsView;
import com.arlhar_membots.R;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


/**
 * [Обозначания] - Текущая неделя: {Период начавшийся ровно неделю назад
 * и заканчивающийся в данный момент}
 * [1 неделя = 604800000(ms)]
 * <p>
 * getCurrentDate - Данный метод возвращает текущую дату в Миллисекундах
 * {Отсчет идет от 1 января 1970 года}
 * <p>
 * getLimitDate - Данный метод возвращает дату, с которой началась текущая неделя
 * <p>
 * initMemesRequest - Создаем лист с мемами, входящими в данный период и передает
 * его в filterMemes
 * <p>
 * filterMemes - сортирует мемы по возрастанию количества лайков, после чего
 * передает данный список методу ****
 * <p>
 * addWeekTop - данный метод добавляет мемы в адаптер
 * <p>
 * onAutoText - метод для действий с автозаполнением
 * <p>
 * onScrollChange - метод для отслеживания позиции скролла популярных
 * <p>
 * getMemTags - метод со слушателями для создания массива
 * initTagsView - подметод с инициализацией textView и Recycler
 * <p>
 *
 **/
public class HomeFragment extends Fragment {
    //-----Самые важные переменные
    private View rootView;
    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

    //--------Переменные экземпляра для популярных мемов
    private RecyclerView recyclerViewPop;
    private final static Long WEEK = 604800000L + 1L;
    private final static Integer WEEK_MEMES_LIMIT = 10;
    List<MemModel> weekMemes = new ArrayList<>();
    private ProgressBar progressBar;
    private final static String DOT = "●";
    private final static String CIRCLE = "○";
    private TextView text_change;
    private LinearLayoutManager layoutManager;

    //-------Переменные экзамепляра для поиска по тэгам
    private AutoCompleteTextView autoCompleteText;// Автоматическое Заполнение для поиска по тэгам
    private ImageButton btn_seatch;


    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle onSaveInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
         rootView= inflater.inflate(R.layout.homefragment, container, false);

         //------Инициализация переменных для популярных мемов
        recyclerViewPop = rootView.findViewById(R.id.recyclerhomepop);
        progressBar=rootView.findViewById(R.id.progressBar);
        text_change=rootView.findViewById(R.id.textView_change);

        layoutManager=new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        recyclerViewPop.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // переменная позиции скролла в текущий момент
                int scrolloutItems = layoutManager.findFirstVisibleItemPosition();

                // передаем эту перменную в параметр метода
                onScrollChange(scrolloutItems);
            }
        });

        addWeekTop();
        onAutoText();
        getMemTags();
        return rootView;
    }

    private void onAutoText() {
        //------Инициализация переменных для поиска тэгов
        autoCompleteText = rootView.findViewById(R.id.AutoCompleteTextView);
        btn_seatch = rootView.findViewById(R.id.btn_seatch);
        final ArrayList<String> tagsList = new ArrayList<>();

        try {
            dbRef.child("MemTimeHelper").child("FavouriteLibrary").addListenerForSingleValueEvent
                    (new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot tagSnapshot : dataSnapshot.getChildren()) {
                                String tags = tagSnapshot.getKey();
                                tagsList.add(tags);
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
            autoCompleteText.setAdapter(new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_dropdown_item_1line, tagsList));
            btn_seatch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String sTag = autoCompleteText.getText().toString();
                    Intent intent = new Intent(getContext(), MemTagsView.class);
                    intent.putExtra("Tag", sTag);
                    startActivity(intent);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            System.out.print("Проблема скорее всего в запросе");
        }

    }

    private void addWeekTop() {
        //-------Вызываем слушатель ко списку мемов
        dbRef.child("Memes").orderByKey().limitToLast(WEEK_MEMES_LIMIT)
                .addValueEventListener(new ValueEventListener() {
                    MemModel memModel;

                    @Override
                    public void onDataChange(DataSnapshot memesSnapshot) {
                        weekMemes.clear();
                        for (DataSnapshot memSnapshot : memesSnapshot.getChildren()) {
                            //-------в класс модели получаем значения
                            memModel = memSnapshot.getValue(MemModel.class);

                            Long currentDate = getCurrentDate();
                            Long limitDate = getLimitDate(currentDate);

                            /*-------если время мема укладывается в 7 дней,то
                             * добавляем в массив list значения лайков*/
                            if (memModel.TIMESTAMP >= limitDate) {
                                //массив всех мемов
                                weekMemes.add(memModel);
                                //сортируем данный массив по лайкам
                                Collections.sort(weekMemes, new Comparator<MemModel>() {
                                    @Override
                                    public int compare(MemModel memModel, MemModel t1) {
                                        return memModel.likes.compareTo(t1.likes);
                                    }
                                });
                                Collections.reverse(weekMemes);

                            }

                        }

                        recyclerViewPop.setLayoutManager(layoutManager);
                        recyclerViewPop.setAdapter(new RecyclerPopAdapter(weekMemes));
                        progressBar.setVisibility(View.GONE);


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void onScrollChange(int scrollPosition){
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < scrollPosition; i++) {
            current.append(CIRCLE);
        }
        current.append(DOT);
        for (int i = scrollPosition+1; i < weekMemes.size(); i++) {
            current.append(CIRCLE);
        }
        text_change.setText(current);

    }

    private static Long getCurrentDate() {
        Date date = new Date();
        return date.getTime();
    }

    private static Long getLimitDate(Long currentDate) {
        return currentDate - WEEK;
    }

    private void getMemTags(){
        //массивы для хранения мемов,исходя из тэгов
        List<MemModel> tagMemList=new ArrayList<>();
        List<MemModel> tagMemList2=new ArrayList<>();
        List<MemModel> tagMemList3=new ArrayList<>();
        List<MemModel> tagMemList4=new ArrayList<>();
        List<MemModel> tagMemList5=new ArrayList<>();

        //слушатель для получения массивов и иx тэгов
        dbRef.child("Memes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot mem:dataSnapshot.getChildren()){
                    MemModel model=mem.getValue(MemModel.class);

                    String tags = null;
                    if (model != null) {
                        tags = model.tags;
                        String finalTags = tags;

                        // 2 слушатель,который получает популярные тэги из базы
                        // проверяет на наличие этих тэгов в модели мема и передает в массив
                        dbRef.child("PopularTag").addListenerForSingleValueEvent
                                (new ValueEventListener() {
                         @Override
                         public void onDataChange(DataSnapshot dataSnapshot) {
                             for(DataSnapshot keys:dataSnapshot.getChildren()){
                                 //ключ служит позицией тэгов
                                 String key=keys.getKey();
                                 int position=Integer.parseInt(key);

                                 String tag=keys.getValue(String.class);
                                 if(finalTags.equals(tag)){
                                     switch (position){
                                         case 0:
                                             tagMemList.add(model);
                                             initTagsView(tag,Integer.parseInt(key),tagMemList);
                                             break;
                                         case 1:
                                             tagMemList2.add(model);
                                             initTagsView(tag,Integer.parseInt(key),tagMemList2);
                                             break;
                                         case 2:
                                             tagMemList3.add(model);
                                             initTagsView(tag,Integer.parseInt(key),tagMemList3);
                                             break;
                                         case 3:
                                             tagMemList4.add(model);
                                             initTagsView(tag,Integer.parseInt(key),tagMemList4);
                                             break;
                                         case 4:
                                             tagMemList5.add(model);
                                             initTagsView(tag,Integer.parseInt(key),tagMemList5);
                                             break;

                                     }
                                     }
                             }
                         }

                         @Override
                         public void onCancelled(DatabaseError databaseError) {

                         }
                     });


                    }
                    }
                    }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initTagsView(String tag,int position,List<MemModel> tagMemList){
        //-----Иницализация переменных для подборки тэгов
        TextView textTag1=rootView.findViewById(R.id.textTag1);
        TextView textTag2=rootView.findViewById(R.id.textTag2);
        TextView textTag3=rootView.findViewById(R.id.textTag3);
        TextView textTag4=rootView.findViewById(R.id.textTag4);
        TextView textTag5=rootView.findViewById(R.id.textTag5);

        RecyclerView viewTag1=rootView.findViewById(R.id.viewTag1);
        RecyclerView  viewTag2=rootView.findViewById(R.id.viewTag2);
        RecyclerView viewTag3=rootView.findViewById(R.id.viewTag3);
        RecyclerView viewTag4=rootView.findViewById(R.id.viewTag4);
        RecyclerView viewTag5=rootView.findViewById(R.id.viewTag5);

        //если массив мемов больше x,то передаем в адаптеры
        if (tagMemList.size()>1) {
            //удаляем с тэга последние 2 символа
            String tags=tag.substring(0,tag.length()-2);
            switch (position) {
                case 0:
                    textTag1.setText("#" + tags);
                    viewTag1.setLayoutManager(new LinearLayoutManager(getContext(),
                            LinearLayoutManager.HORIZONTAL, false));
                    viewTag1.setAdapter(new PopularTagAdapter(tagMemList));
                    break;
                case 1:
                    textTag2.setText("#" + tags);
                    viewTag2.setLayoutManager(new LinearLayoutManager(getContext(),
                            LinearLayoutManager.HORIZONTAL, false));
                    viewTag2.setAdapter(new PopularTagAdapter(tagMemList));
                    break;
                case 2:
                    textTag3.setText("#" + tags);
                    viewTag3.setLayoutManager(new LinearLayoutManager(getContext(),
                            LinearLayoutManager.HORIZONTAL, false));
                    viewTag3.setAdapter(new PopularTagAdapter(tagMemList));
                    break;
                case 3:
                    textTag4.setText("#" + tags);
                    viewTag4.setLayoutManager(new LinearLayoutManager(getContext(),
                            LinearLayoutManager.HORIZONTAL, false));
                    viewTag4.setAdapter(new PopularTagAdapter(tagMemList));
                    break;
                case 4:
                    textTag5.setText("#" + tags);
                    viewTag5.setLayoutManager(new LinearLayoutManager(getContext(),
                            LinearLayoutManager.HORIZONTAL, false));
                    viewTag5.setAdapter(new PopularTagAdapter(tagMemList));
                    break;
            }
        }
    }


}

