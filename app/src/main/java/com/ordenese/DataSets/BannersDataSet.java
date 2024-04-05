package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BannersDataSet {

   @SerializedName("banner_id")
   @Expose
   public String banner_id;

   @SerializedName("vendor_id")
   @Expose
   public String vendor_id;

   @SerializedName("vendor_type_id")
   @Expose
   public String vendor_type_id;

   @SerializedName("banner")
   @Expose
   public String banner;

   public String getVendor_type_id() {
      return vendor_type_id;
   }

   public void setVendor_type_id(String vendor_type_id) {
      this.vendor_type_id = vendor_type_id;
   }

   public String getBanner_id() {
      return banner_id;
   }

   public void setBanner_id(String banner_id) {
      this.banner_id = banner_id;
   }

   public String getVendor_id() {
      return vendor_id;
   }

   public void setVendor_id(String vendor_id) {
      this.vendor_id = vendor_id;
   }

   public String getBanner() {
      return banner;
   }

   public void setBanner(String banner) {
      this.banner = banner;
   }
}
