package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ScheduleDeliveryDataSet {

    @SerializedName("day")
    @Expose
    public ArrayList<String> day = new ArrayList<>();
    @SerializedName("date")
    @Expose
    public ArrayList<String> date = new ArrayList<>();

}
