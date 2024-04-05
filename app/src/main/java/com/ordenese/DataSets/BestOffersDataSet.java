package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BestOffersDataSet {

   @SerializedName("vendor_id")
   @Expose
   public String vendor_id;

   @SerializedName("offer_id")
   @Expose
   public String offer_id;

   @SerializedName("vendor_name")
   @Expose
   public String vendor_name;

   @SerializedName("rating")
   @Expose
   public RatingDataSet ratingDataSet;

   @SerializedName("offer_value")
   @Expose
   public String offer_value;

   @SerializedName("delivery_time")
   @Expose
   public String delivery_time;

   @SerializedName("delivery_fee")
   @Expose
   public String delivery_fee;

   @SerializedName("banner")
   @Expose
   public String banner;

   @SerializedName("vendor_status")
   @Expose
   public String vendor_status;

   @SerializedName("cuisines")
   @Expose
   public String cuisines;

   @SerializedName("offer")
   @Expose
   public String offer;

   @SerializedName("vendor_type_id")
   @Expose
   public String vendor_type_id;
   @SerializedName("minimum_amount")
   @Expose
   public String minimum_amount;

   public String getVendor_type_id() {
      return vendor_type_id;
   }

   public void setVendor_type_id(String vendor_type_id) {
      this.vendor_type_id = vendor_type_id;
   }

   public String getVendor_status() {
      return vendor_status;
   }

   public void setVendor_status(String vendor_status) {
      this.vendor_status = vendor_status;
   }

   public String getCuisines() {
      return cuisines;
   }

   public void setCuisines(String cuisines) {
      this.cuisines = cuisines;
   }

   public String getOffer() {
      return offer;
   }

   public void setOffer(String offer) {
      this.offer = offer;
   }

   public RatingDataSet getRatingDataSet() {
      return ratingDataSet;
   }

   public void setRatingDataSet(RatingDataSet ratingDataSet) {
      this.ratingDataSet = ratingDataSet;
   }

   public String getVendor_id() {
      return vendor_id;
   }

   public void setVendor_id(String vendor_id) {
      this.vendor_id = vendor_id;
   }

   public String getOffer_id() {
      return offer_id;
   }

   public void setOffer_id(String offer_id) {
      this.offer_id = offer_id;
   }

   public String getVendor_name() {
      return vendor_name;
   }

   public void setVendor_name(String vendor_name) {
      this.vendor_name = vendor_name;
   }



   public String getOffer_value() {
      return offer_value;
   }

   public void setOffer_value(String offer_value) {
      this.offer_value = offer_value;
   }

   public String getDelivery_time() {
      return delivery_time;
   }

   public void setDelivery_time(String delivery_time) {
      this.delivery_time = delivery_time;
   }

   public String getDelivery_fee() {
      return delivery_fee;
   }

   public void setDelivery_fee(String delivery_fee) {
      this.delivery_fee = delivery_fee;
   }

   public String getBanner() {
      return banner;
   }

   public void setBanner(String banner) {
      this.banner = banner;
   }
}
