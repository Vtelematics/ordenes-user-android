package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VendorZoneDetailsDataSet {

   @SerializedName("zone_id")
   @Expose
   public String zone_id;

   @SerializedName("zone_name")
   @Expose
   public String zone_name;

   public String getZone_id() {
      return zone_id;
   }

   public void setZone_id(String zone_id) {
      this.zone_id = zone_id;
   }

   public String getZone_name() {
      return zone_name;
   }

   public void setZone_name(String zone_name) {
      this.zone_name = zone_name;
   }


}
