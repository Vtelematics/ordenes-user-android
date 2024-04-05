package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class CheckoutCartListApi {

    @SerializedName("success")
    @Expose
    public SuccessDataSet success;

    @SerializedName("error")
    @Expose
    public ErrorDataSet error;

    @SerializedName("totals")
    @Expose
    public ArrayList<CartTotalsDataSet> totalsList;

    @SerializedName("error_warning")
    @Expose
    public String error_warning;

    @SerializedName("vendor_id")
    @Expose
    public String vendor_id;

    @SerializedName("vendor_name")
    @Expose
    public String vendor_name;

    @SerializedName("latitude")
    @Expose
    public String latitude;

    @SerializedName("longitude")
    @Expose
    public String longitude;

    @SerializedName("restaurant_latitude")
    @Expose
    public String restaurant_latitude;

    @SerializedName("restaurant_longitude")
    @Expose
    public String restaurant_longitude;

    @SerializedName("vendor_address")
    @Expose
    public String vendor_address;

    @SerializedName("vendor_mobile")
    @Expose
    public String vendor_mobile;

    @SerializedName("offer_status")
    @Expose
    public String offer_status;

    @SerializedName("coupon_status")
    @Expose
    public String coupon_status;


}
