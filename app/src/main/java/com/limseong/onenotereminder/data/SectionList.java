package com.limseong.onenotereminder.data;

import com.google.gson.annotations.SerializedName;
import com.microsoft.graph.models.extensions.OnenoteSection;

import java.util.Date;
import java.util.List;

public class SectionList {
    @SerializedName("update_date")
    private Date updateDate;

    @SerializedName("sections")
    private List<OnenoteSection> sections;

    public SectionList(Date updateDate, List<OnenoteSection> sections) {
        this.updateDate = updateDate;
        this.sections = sections;
    }
}
