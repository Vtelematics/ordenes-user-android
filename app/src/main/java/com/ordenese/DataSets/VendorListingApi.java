package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class VendorListingApi {

    @SerializedName("success")
    @Expose
    public SuccessDataSet success;

    @SerializedName("error")
    @Expose
    public ErrorDataSet error;

    @SerializedName("filter")
    @Expose
    public ArrayList<FilterDataSet> filterList;

    @SerializedName("banner")
    @Expose
    public ArrayList<VendorBannerDataSet> bannerList;

    @SerializedName("vendor")
    @Expose
    public ArrayList<VendorDataSet> vendorList;


}
