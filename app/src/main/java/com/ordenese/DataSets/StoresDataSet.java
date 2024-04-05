package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StoresDataSet {

   @SerializedName("vendor_id")
   @Expose
   public String vendor_id;

   @SerializedName("vendor_type_id")
   @Expose
   public String vendor_type_id;

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

   @SerializedName("store_types")
   @Expose
   public String store_types;

 @SerializedName("free_delivery")
   @Expose
   public String free_delivery;




}
