package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StoreInfoDataSet {

   @SerializedName("vendor_id")
   @Expose
   public String vendor_id;

   @SerializedName("vendor_name")
   @Expose
   public String vendor_name;

   @SerializedName("vendor_type_id")
   @Expose
   public String vendor_type_id;

   @SerializedName("delivery_charge")
   @Expose
   public String delivery_charge;

   @SerializedName("minimum_amount")
   @Expose
   public String minimum_amount;

   @SerializedName("vendor_status")
   @Expose
   public String vendor_status;

   @SerializedName("delivery_time")
   @Expose
   public String delivery_time;

   @SerializedName("delivery")
   @Expose
   public String delivery;

   @SerializedName("pick_up")
   @Expose
   public String pick_up;

   @SerializedName("preparing_time")
   @Expose
   public String preparing_time;
   @SerializedName("vendor_distance")
   @Expose
   public String vendor_distance;

}
