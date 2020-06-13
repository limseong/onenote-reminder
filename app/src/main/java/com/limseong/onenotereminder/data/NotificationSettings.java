package com.limseong.onenotereminder.data;

import com.google.gson.annotations.SerializedName;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class NotificationSettings {
    @SerializedName("section_id_list")
    private List<String> sectionIdList;

    @SerializedName("time_list")
    private List<Calendar> timeList;

    @SerializedName("option")
    private int option;

    public NotificationSettings() {
        sectionIdList = new LinkedList<>();
        timeList = new LinkedList<>();
    }

    public NotificationSettings(List<String> sectionIdList, List<Calendar> timeList) {
        this.sectionIdList = sectionIdList;
        this.timeList = timeList;
    }

    public List<String> getSectionIdList() {
        return sectionIdList;
    }

    public List<Calendar> getTimeList() {
        return timeList;
    }
}
