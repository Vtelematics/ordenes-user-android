package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class OrderDataSet {

    @SerializedName("order_id")
    @Expose
    public String order_id;

    @SerializedName("ordered_date")
    @Expose
    public String ordered_date;

    @SerializedName("ordered_time")
    @Expose
    public String ordered_time;

    @SerializedName("delivery_address")
    @Expose
    public String delivery_address;

    @SerializedName("payment_method")
    @Expose
    public String payment_method;

    @SerializedName("order_status_id")
    @Expose
    public String order_status_id;

    @SerializedName("cancel_status")
    @Expose
    public String cancel_status;

    @SerializedName("order_status")
    @Expose
    public String order_status;

    @SerializedName("note")
    @Expose
    public String note;

    @SerializedName("vendor_name")
    @Expose
    public String vendor_name;

    @SerializedName("order_type")
    @Expose
    public String order_type;

    @SerializedName("vendor_latitude")
    @Expose
    public String vendor_latitude;

    @SerializedName("vendor_longitude")
    @Expose
    public String vendor_longitude;

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
    public ArrayList<MyOrderProductDataSet> myOrderProductList;

    @SerializedName("total")
    @Expose
    public ArrayList<MyOrderTotalsDataSet> myOrderTotalsList;

    @SerializedName("review_status")
    @Expose
    public String review_status;

    public String getOrder_type() {
        return order_type;
    }

    public String getVendor_latitude() {
        return vendor_latitude;
    }

    public String getVendor_longitude() {
        return vendor_longitude;
    }

    public String getReview_status() {
        return review_status;
    }

    public void setReview_status(String review_status) {
        this.review_status = review_status;
    }

    public ArrayList<MyOrderProductDataSet> getMyOrderProductList() {
        return myOrderProductList;
    }

    public void setMyOrderProductList(ArrayList<MyOrderProductDataSet> myOrderProductList) {
        this.myOrderProductList = myOrderProductList;
    }


    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
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

    public String getDelivery_address() {
        return delivery_address;
    }

    public void setDelivery_address(String delivery_address) {
        this.delivery_address = delivery_address;
    }

    public String getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(String payment_method) {
        this.payment_method = payment_method;
    }

    public String getOrder_status_id() {
        return order_status_id;
    }

    public void setOrder_status_id(String order_status_id) {
        this.order_status_id = order_status_id;
    }

    public String getOrder_status() {
        return order_status;
    }

    public void setOrder_status(String order_status) {
        this.order_status = order_status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getVendor_name() {
        return vendor_name;
    }

    public void setVendor_name(String vendor_name) {
        this.vendor_name = vendor_name;
    }


    public ArrayList<MyOrderTotalsDataSet> getMyOrderTotalsList() {
        return myOrderTotalsList;
    }

    public void setMyOrderTotalsList(ArrayList<MyOrderTotalsDataSet> myOrderTotalsList) {
        this.myOrderTotalsList = myOrderTotalsList;
    }
}
