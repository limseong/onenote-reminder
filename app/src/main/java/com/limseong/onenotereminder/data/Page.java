package com.limseong.onenotereminder.data;

import com.google.gson.annotations.SerializedName;

import androidx.annotation.NonNull;

public class Page {
    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("section_title")
    private String sectionTitle;

    @SerializedName("parent_section_id")
    private String sectionId;

    @SerializedName("onenote_client_url")
    private String clientUrl;

    //private String webUrl;
    public Page(@NonNull String title, @NonNull String sectionTitle, @NonNull String clientUrl) {
        this.title = title;
        this.sectionTitle = sectionTitle;
        this.clientUrl = clientUrl;
    }

    public Page(@NonNull String id, @NonNull String title, @NonNull String sectionId,
                @NonNull String sectionTitle, @NonNull String clientUrl) {
        this.id = id;
        this.title = title;
        this.sectionId = sectionId;
        this.sectionTitle = sectionTitle;
        this.clientUrl = clientUrl;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSectionTitle() {
        return sectionTitle;
    }

    public String getSectionId() {
        return sectionId;
    }

    public String getClientUrl() {
        return clientUrl;
    }


}
