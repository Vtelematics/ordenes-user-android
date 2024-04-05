package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CouponApplyApi {


   @SerializedName("success")
   @Expose
   public SuccessDataSet success;

   @SerializedName("error")
   @Expose
   public ErrorDataSet error;


   @SerializedName("coupon_id")
   @Expose
   public String coupon_id;

   public String getCoupon_id() {
      return coupon_id;
   }

   public void setCoupon_id(String coupon_id) {
      this.coupon_id = coupon_id;
   }
}
