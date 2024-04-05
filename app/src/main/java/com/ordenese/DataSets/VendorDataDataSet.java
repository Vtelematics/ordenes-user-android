package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VendorDataDataSet {

   @SerializedName("vendor_id")
   @Expose
   public String vendor_id;

   @SerializedName("vendor_name")
   @Expose
   public String vendor_name;

   @SerializedName("vendor_status")
   @Expose
   public String vendor_status;

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

   public String getVendor_status() {
      return vendor_status;
   }

   public void setVendor_status(String vendor_status) {
      this.vendor_status = vendor_status;
   }
}
