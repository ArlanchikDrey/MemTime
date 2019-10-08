package com.arlhar_membots.Basic.LentaCycle;

public class MemModel {

    public String uid; //Идентификатор мема
    public String url; //URL изображения
    public String tags; //Строка со списком тэгов
    public Integer likes; //Кол-во лайков
    public Integer dislikes; //Кол-во дизлайков
    public Integer rate; //Текущая оценка
    public Integer size_comment;
    public Long TIMESTAMP;



    //0 - нет оценки
    //1 - лайк
    //2 - дизлайк
    public Boolean inFavourite; //Находится ли в избранном?


    public MemModel(){

    }


    public MemModel(String uid, String url, String tags, Integer likes, Integer dislikes,
                    Integer rate, Boolean inFavourite,Integer size_comment, Long TIMESTAMP) {
        this.uid = uid;
        this.url = url;
        this.tags = tags;
        this.likes = likes;
        this.dislikes = dislikes;
        this.rate = rate;
        this.inFavourite = inFavourite;
        this.size_comment = size_comment;
        this.TIMESTAMP = TIMESTAMP;


    }

}
