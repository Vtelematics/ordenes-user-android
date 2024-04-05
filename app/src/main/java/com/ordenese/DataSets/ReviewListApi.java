package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.ordenese.DataSets.ErrorDataSet;
import com.ordenese.DataSets.ReviewListDataSet;
import com.ordenese.DataSets.SuccessDataSet;
import com.ordenese.DataSets.VendorFilterDataSet;

import java.util.ArrayList;

public class ReviewListApi {

   @SerializedName("success")
   @Expose
   public SuccessDataSet success;

   @SerializedName("error")
   @Expose
   public ErrorDataSet error;

   @SerializedName("avg_vendor_rating")
   @Expose
   public String avg_vendor_rating;

   @SerializedName("avg_delivery_time")
   @Expose
   public String avg_delivery_time;

   @SerializedName("avg_quality")
   @Expose
   public String avg_quality;

   @SerializedName("avg_value_for_money")
   @Expose
   public String avg_value_for_money;

   @SerializedName("avg_order_packing")
   @Expose
   public String avg_order_packing;

   @SerializedName("total")
   @Expose
   public String total;

   @SerializedName("review_list")
   @Expose
   public ArrayList<ReviewListDataSet> reviewList;

   public String getTotal() {
      return total;
   }

   public void setTotal(String total) {
      this.total = total;
   }

   public String getAvg_vendor_rating() {
      return avg_vendor_rating;
   }

   public void setAvg_vendor_rating(String avg_vendor_rating) {
      this.avg_vendor_rating = avg_vendor_rating;
   }

   public String getAvg_delivery_time() {
      return avg_delivery_time;
   }

   public void setAvg_delivery_time(String avg_delivery_time) {
      this.avg_delivery_time = avg_delivery_time;
   }

   public String getAvg_quality() {
      return avg_quality;
   }

   public void setAvg_quality(String avg_quality) {
      this.avg_quality = avg_quality;
   }

   public String getAvg_value_for_money() {
      return avg_value_for_money;
   }

   public void setAvg_value_for_money(String avg_value_for_money) {
      this.avg_value_for_money = avg_value_for_money;
   }

   public String getAvg_order_packing() {
      return avg_order_packing;
   }

   public void setAvg_order_packing(String avg_order_packing) {
      this.avg_order_packing = avg_order_packing;
   }

   public ArrayList<ReviewListDataSet> getReviewList() {
      return reviewList;
   }

   public void setReviewList(ArrayList<ReviewListDataSet> reviewList) {
      this.reviewList = reviewList;
   }
}
