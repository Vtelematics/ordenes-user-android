package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Vendor {

    @SerializedName("vendor_id")
    @Expose
    private String vendorId;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("image")
    @Expose
    private String image;
    @SerializedName("logo")
    @Expose
    private String logo;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("latitude")
    @Expose
    private String latitude;
    @SerializedName("longitude")
    @Expose
    private String longitude;
    @SerializedName("vendor_status")
    @Expose
    private String vendorStatus;
    @SerializedName("working_hours")
    @Expose
    private String working_hours;
    @SerializedName("business_type")
    @Expose
    private BusinessType businessType;

    @SerializedName("cuisine")
    @Expose
    private List<Cuisine> cuisine = null;

    @SerializedName("delivery_charge")
    @Expose
    private String deliveryCharge;
    @SerializedName("minimum_amount")
    @Expose
    private String minimumAmount;
    @SerializedName("delivery_time")
    @Expose
    private String deliveryTime;
    @SerializedName("payment_method")
    @Expose
    private ArrayList<PaymentMethod> paymentMethod = null;
    @SerializedName("category")
    @Expose
    private ArrayList<Category> category = null;
    @SerializedName("rating")
    @Expose
    public RatingDataSet rating;
    @SerializedName("delivery")
    @Expose
    public String delivery;

    @SerializedName("pick_up")
    @Expose
    public String pick_up;

    @SerializedName("preparing_time")
    @Expose
    public String preparing_time;
    @SerializedName("vendor_distance")
    @Expose
    public String vendor_distance;

    public String getDelivery() {
        return delivery;
    }



    public String getWorking_hours() {
        return working_hours;
    }

    public void setWorking_hours(String working_hours) {
        this.working_hours = working_hours;
    }

    public RatingDataSet getRating() {
        return rating;
    }

    public void setRating(RatingDataSet rating) {
        this.rating = rating;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getVendorStatus() {
        return vendorStatus;
    }

    public void setVendorStatus(String vendorStatus) {
        this.vendorStatus = vendorStatus;
    }

    public BusinessType getBusinessType() {
        return businessType;
    }

    public void setBusinessType(BusinessType businessType) {
        this.businessType = businessType;
    }

    public List<Cuisine> getCuisine() {
        return cuisine;
    }

    public void setCuisine(List<Cuisine> cuisine) {
        this.cuisine = cuisine;
    }

    public String getDeliveryCharge() {
        return deliveryCharge;
    }

    public void setDeliveryCharge(String deliveryCharge) {
        this.deliveryCharge = deliveryCharge;
    }

    public String getMinimumAmount() {
        return minimumAmount;
    }

    public void setMinimumAmount(String minimumAmount) {
        this.minimumAmount = minimumAmount;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public ArrayList<PaymentMethod> getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(ArrayList<PaymentMethod> paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public ArrayList<Category> getCategory() {
        return category;
    }

    public void setCategory(ArrayList<Category> category) {
        this.category = category;
    }

}
