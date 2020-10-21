package com.limseong.onenotereminder.data;

import com.google.gson.annotations.SerializedName;

public class NotificationSetting {
    @SerializedName("hour")
    private int hour;

    @SerializedName("minute")
    private int minute;

    @SerializedName("enabled")
    private boolean enabled;

    public NotificationSetting(int hour, int minute, boolean enabled) {
        this.hour = hour;
        this.minute = minute;
        this.enabled = enabled;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("%02d", hour));
        stringBuilder.append(":");
        stringBuilder.append(String.format("%02d", minute));
        return stringBuilder.toString();
    }
}
