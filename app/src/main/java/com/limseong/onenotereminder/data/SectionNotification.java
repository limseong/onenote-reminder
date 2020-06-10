package com.limseong.onenotereminder.data;

import com.google.gson.annotations.SerializedName;

import java.util.LinkedList;
import java.util.List;

public class SectionNotification {
    @SerializedName("id_list")
    private List<String> sectionIdList;

    @SerializedName("option")
    private int option;

    public SectionNotification() {
        sectionIdList = new LinkedList<>();
    }

    public SectionNotification(List<String> sectionIdList) {
        this.sectionIdList = sectionIdList;
    }

    public List<String> getSectionIdList() {
        return sectionIdList;
    }
}
