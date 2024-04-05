package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VendorDataSet {

    @SerializedName("vendor_id")
    @Expose
    public String vendor_id;

    @SerializedName("name")
    @Expose
    public String name;

    @SerializedName("banner")
    @Expose
    public String banner;

    @SerializedName("logo")
    @Expose
    public String logo;

    @SerializedName("vendor_status")
    @Expose
    public String vendor_status;

    @SerializedName("delivery_charge")
    @Expose
    public String delivery_charge;

    @SerializedName("minimum_amount")
    @Expose
    public String minimum_amount;

    @SerializedName("delivery_time")
    @Expose
    public String delivery_time;

    @SerializedName("rating")
    @Expose
    public VendorRatingDataSet vendorRatingDataSet;

    @SerializedName("offer")
    @Expose
    public String offer;

    @SerializedName("cuisines")
    @Expose
    public String cuisines;

    @SerializedName("vendor_type_id")
    @Expose
    public String vendor_type_id;

    @SerializedName("free_delivery")
    @Expose
    public String free_delivery;

    public String getVendor_type_id() {
        return vendor_type_id;
    }

    public void setVendor_type_id(String vendor_type_id) {
        this.vendor_type_id = vendor_type_id;
    }

    public String getCuisines() {
        return cuisines;
    }

    public void setCuisines(String cuisines) {
        this.cuisines = cuisines;
    }

    public String getVendor_id() {
        return vendor_id;
    }

    public void setVendor_id(String vendor_id) {
        this.vendor_id = vendor_id;
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

    public String getVendor_status() {
        return vendor_status;
    }

    public void setVendor_status(String vendor_status) {
        this.vendor_status = vendor_status;
    }

    public String getDelivery_charge() {
        return delivery_charge;
    }

    public void setDelivery_charge(String delivery_charge) {
        this.delivery_charge = delivery_charge;
    }

    public String getMinimum_amount() {
        return minimum_amount;
    }

    public void setMinimum_amount(String minimum_amount) {
        this.minimum_amount = minimum_amount;
    }

    public String getDelivery_time() {
        return delivery_time;
    }

    public void setDelivery_time(String delivery_time) {
        this.delivery_time = delivery_time;
    }

    public VendorRatingDataSet getVendorRatingDataSet() {
        return vendorRatingDataSet;
    }

    public void setVendorRatingDataSet(VendorRatingDataSet vendorRatingDataSet) {
        this.vendorRatingDataSet = vendorRatingDataSet;
    }

    public String getOffer() {
        return offer;
    }

    public void setOffer(String offer) {
        this.offer = offer;
    }
}
