package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CouponDataSet {

   @SerializedName("coupon_id")
   @Expose
   public String coupon_id;

   @SerializedName("code")
   @Expose
   public String code;

   @SerializedName("name")
   @Expose
   public String name;

   public String getCoupon_id() {
      return coupon_id;
   }

   public void setCoupon_id(String coupon_id) {
      this.coupon_id = coupon_id;
   }

   public String getCode() {
      return code;
   }

   public void setCode(String code) {
      this.code = code;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }
}
