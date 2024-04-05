package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class OrderTrackDataSet {

    @SerializedName("order_id")
    @Expose
    public String order_id;

    @SerializedName("delivery_time")
    @Expose
    public String delivery_time;

    @SerializedName("zone_name")
    @Expose
    public String zone_name;

    @SerializedName("zone_id")
    @Expose
    public String zone_id;

    @SerializedName("vendor_name")
    @Expose
    public String vendor_name;

    @SerializedName("vendor_latitude")
    @Expose
    public String vendor_latitude;

    @SerializedName("vendor_longitude")
    @Expose
    public String vendor_longitude;

    @SerializedName("customer_first_name")
    @Expose
    public String customer_first_name;

    @SerializedName("customer_last_name")
    @Expose
    public String customer_last_name;

    @SerializedName("customer_mobile")
    @Expose
    public String customer_mobile;

    @SerializedName("customer_country_code")
    @Expose
    public String customer_country_code;


    @SerializedName("customer_latitude")
    @Expose
    public String customer_latitude;

    @SerializedName("customer_longitude")
    @Expose
    public String customer_longitude;

    @SerializedName("delivery_address")
    @Expose
    public String delivery_address;

    @SerializedName("driver_id")
    @Expose
    public String driver_id;

    @SerializedName("driver_mobile")
    @Expose
    public String driver_mobile;

    @SerializedName("driver_name")
    @Expose
    public String driver_name;

    @SerializedName("driver_profile")
    @Expose
    public String driver_profile;

    @SerializedName("ordered_date")
    @Expose
    public String ordered_date;

    @SerializedName("ordered_time")
    @Expose
    public String ordered_time;

    @SerializedName("payment_method")
    @Expose
    public String payment_method;

    @SerializedName("order_status_id")
    @Expose
    public String order_status_id;

    @SerializedName("order_status")
    @Expose
    public String order_status;

    @SerializedName("note")
    @Expose
    public String note;

    @SerializedName("schedule_time")
    @Expose
    public String schedule_time;

    @SerializedName("schedule_date")
    @Expose
    public String schedule_date;

    @SerializedName("schedule_status")
    @Expose
    public String schedule_status;

    @SerializedName("product")
    @Expose
    public ArrayList<OrderTrackProductDataSet> orderTrackProduct;

    @SerializedName("total")
    @Expose
    public String total;


}
