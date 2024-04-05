package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ReviewListDataSet {

    @SerializedName("vendor_rating")
    @Expose
    public String vendor_rating;

    @SerializedName("comment")
    @Expose
    public String comment;

    @SerializedName("customer_name")
    @Expose
    public String customer_name;

    @SerializedName("date")
    @Expose
    public String date;

    public String getVendor_rating() {
        return vendor_rating;
    }

    public void setVendor_rating(String vendor_rating) {
        this.vendor_rating = vendor_rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCustomer_name() {
        return customer_name;
    }

    public void setCustomer_name(String customer_name) {
        this.customer_name = customer_name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
