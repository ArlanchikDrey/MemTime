package com.arlhar_membots.Basic.LentaCycle;

public class ListItem {
    private String tag;
    public String uid; //Идентификатор мема
    public Integer likes; //Кол-во лайков
    public Integer dislikes; //Кол-во дизлайков
    public Integer rate; //Текущая оценка
    //0 - нет оценки
    //1 - лайк
    //2 - дизлайк
    public   Boolean inFavourite; //Находится ли в избранном?


    public ListItem(String tag){
        this.tag = tag;
    }

    /*public ListItem(String uid,Integer likes, Integer dislikes,Integer rate, Boolean inFavourite) {
        this.uid = uid;
        this.likes = likes;
        this.dislikes = dislikes;
        this.rate = rate;
        this.inFavourite = inFavourite;
    }*/

    public String getTag(){
        return tag;
    }

}
