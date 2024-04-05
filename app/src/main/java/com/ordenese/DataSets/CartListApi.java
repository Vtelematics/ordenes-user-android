package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class CartListApi {

   @SerializedName("success")
   @Expose
   public SuccessDataSet success;

   @SerializedName("error")
   @Expose
   public ErrorDataSet error;

   @SerializedName("products")
   @Expose
   public ArrayList<CartProductsDataSet> productsList;

   @SerializedName("totals")
   @Expose
   public ArrayList<CartTotalsDataSet> totalsList;

   @SerializedName("error_warning")
   @Expose
   public String error_warning;

   @SerializedName("vendor_delivery_time")
   @Expose
   public String vendor_delivery_time;

   @SerializedName("vendor_id")
   @Expose
   public String vendor_id;

   @SerializedName("vendor_name")
   @Expose
   public String vendor_name;

   @SerializedName("vendor_type_id")
   @Expose
   public String vendor_type_id;


   @SerializedName("latitude")
   @Expose
   public String latitude;

   @SerializedName("longitude")
   @Expose
   public String longitude;

   @SerializedName("restaurant_latitude")
   @Expose
   public String restaurant_latitude;

   @SerializedName("restaurant_longitude")
   @Expose
   public String restaurant_longitude;

   @SerializedName("order_type")
   @Expose
   public String order_type;

   @SerializedName("vendor_address")
   @Expose
   public String vendor_address;

   @SerializedName("product_count")
   @Expose
   public String product_count;

   @SerializedName("total")
   @Expose
   public String total;





}
