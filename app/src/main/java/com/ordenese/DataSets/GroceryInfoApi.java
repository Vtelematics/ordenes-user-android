package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class GroceryInfoApi {


   @SerializedName("success")
   @Expose
   public Success success;

   @SerializedName("error")
   @Expose
   public ErrorDataSet error;

   @SerializedName("vendor_info")
   @Expose
   public StoreInfoDataSet storeInfo;

   @SerializedName("category")
   @Expose
   public ArrayList<GroceryCategoryDataSet> categoryList;


}
