package com.arlhar_membots.Basic;

import com.google.firebase.database.IgnoreExtraProperties;

// [START comment_class]
@IgnoreExtraProperties
public class GetMem {

    public String id;
    public String url;

    public GetMem() {
        // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
    }

    public GetMem(String id, String url) {
        this.id = id;
        this.url = url;
    }

}
