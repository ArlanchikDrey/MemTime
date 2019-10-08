package com.arlhar_membots.Basic.LentaCycle;

/**
 * Created by admin on 03.09.2018.
 */

public class CommentList {
    public String text_comments;
    public String author;
    public String time;
    public String comment_key;
    public Integer likes_com;
    public Integer rate;//оценил коммент?да=1,нет=0

    public CommentList(){

    }


    public CommentList(String time,String author,String text_comments,String comment_key ,Integer likes_com,Integer rate) {
        this.time=time;
        this.text_comments = text_comments;
        this.author=author;
        this.comment_key=comment_key;
        this.likes_com=likes_com;
        this.rate=rate;



        }
}
