package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BrandsDataSet {

   @SerializedName("vendor_id")
   @Expose
   public String vendor_id;

   @SerializedName("vendor_name")
   @Expose
   public String vendor_name;

   @SerializedName("delivery_time")
   @Expose
   public String delivery_time;

   @SerializedName("logo")
   @Expose
   public String logo;

   @SerializedName("vendor_status")
   @Expose
   public String vendor_status;

   @SerializedName("vendor_type_id")
   @Expose
   public String vendor_type_id;

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

   public String getVendor_id() {
      return vendor_id;
   }

   public void setVendor_id(String vendor_id) {
      this.vendor_id = vendor_id;
   }

   public String getVendor_name() {
      return vendor_name;
   }

   public void setVendor_name(String vendor_name) {
      this.vendor_name = vendor_name;
   }

   public String getDelivery_time() {
      return delivery_time;
   }

   public void setDelivery_time(String delivery_time) {
      this.delivery_time = delivery_time;
   }

   public String getLogo() {
      return logo;
   }

   public void setLogo(String logo) {
      this.logo = logo;
   }
}
