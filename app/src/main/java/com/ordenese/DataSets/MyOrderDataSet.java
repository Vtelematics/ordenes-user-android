package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MyOrderDataSet {

    @SerializedName("order_id")
    @Expose
    private String order_id;

    @SerializedName("status")
    @Expose
    private String status;

    @SerializedName("ordered_date")
    @Expose
    private String ordered_date;

    @SerializedName("ordered_time")
    @Expose
    private String ordered_time;

    @SerializedName("vendor_id")
    @Expose
    private String vendor_id;

    @SerializedName("vendor_name")
    @Expose
    private String vendor_name;

    @SerializedName("logo")
    @Expose
    private String logo;

    @SerializedName("order_type")
    @Expose
    private String order_type;

    public String getOrder_type() {
        return order_type;
    }

    public void setOrder_type(String order_type) {
        this.order_type = order_type;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrdered_date() {
        return ordered_date;
    }

    public void setOrdered_date(String ordered_date) {
        this.ordered_date = ordered_date;
    }

    public String getOrdered_time() {
        return ordered_time;
    }

    public void setOrdered_time(String ordered_time) {
        this.ordered_time = ordered_time;
    }

    public String getVendor_id() {
        return vendor_id;
    }

    public void setVendor_id(String vendor_id) {
        this.vendor_id = vendor_id;
    }

    public String getVendor_name() {
        return vendor_name;
    }

    public void setVendor_name(String vendor_name) {
        this.vendor_name = vendor_name;
    }
}
