package com.ordenese.DataSets;

import android.media.Rating;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WishlistVendor {

    @SerializedName("vendor_id")
    @Expose
    private String vendorId;
    @SerializedName("vendor_type_id")
    @Expose
    private String vendorTypeId;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("banner")
    @Expose
    private String banner;
    @SerializedName("logo")
    @Expose
    private String logo;
    @SerializedName("vendor_status")
    @Expose
    private String vendorStatus;
    @SerializedName("delivery_charge")
    @Expose
    private String deliveryCharge;
    @SerializedName("free_delivery")
    @Expose
    private String freeDelivery;
    @SerializedName("minimum_amount")
    @Expose
    private String minimumAmount;
    @SerializedName("delivery_time")
    @Expose
    private String deliveryTime;
    @SerializedName("rating")
    @Expose
    private RatingDataSet rating;
    @SerializedName("new")
    @Expose
    private String _new;
    @SerializedName("cuisines")
    @Expose
    private String cuisines;
    @SerializedName("store_types")
    @Expose
    private String storeTypes;
    @SerializedName("offer")
    @Expose
    private String offer;

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

    public String getVendorTypeId() {
        return vendorTypeId;
    }

    public void setVendorTypeId(String vendorTypeId) {
        this.vendorTypeId = vendorTypeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getVendorStatus() {
        return vendorStatus;
    }

    public void setVendorStatus(String vendorStatus) {
        this.vendorStatus = vendorStatus;
    }

    public String getDeliveryCharge() {
        return deliveryCharge;
    }

    public void setDeliveryCharge(String deliveryCharge) {
        this.deliveryCharge = deliveryCharge;
    }

    public String getFreeDelivery() {
        return freeDelivery;
    }

    public void setFreeDelivery(String freeDelivery) {
        this.freeDelivery = freeDelivery;
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

    public String get_new() {
        return _new;
    }

    public void set_new(String _new) {
        this._new = _new;
    }

    public String getCuisines() {
        return cuisines;
    }

    public void setCuisines(String cuisines) {
        this.cuisines = cuisines;
    }

    public String getStoreTypes() {
        return storeTypes;
    }

    public void setStoreTypes(String storeTypes) {
        this.storeTypes = storeTypes;
    }

    public String getOffer() {
        return offer;
    }

    public void setOffer(String offer) {
        this.offer = offer;
    }
}
