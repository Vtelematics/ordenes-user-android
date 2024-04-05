package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HomeModuleContent {
    @SerializedName("login_title")
    @Expose
    public String loginTitle;
    @SerializedName("login_description")
    @Expose
    public String loginDescription;
    @SerializedName("brand")
    @Expose
    public String brand;
    @SerializedName("drinks")
    @Expose
    public String drinks;
}
