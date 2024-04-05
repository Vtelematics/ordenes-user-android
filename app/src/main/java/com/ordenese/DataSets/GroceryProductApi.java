package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class GroceryProductApi {

   @SerializedName("success")
   @Expose
   public SuccessDataSet success;

   @SerializedName("error")
   @Expose
   public ErrorDataSet error;

   @SerializedName("total")
   @Expose
   public String total;

   @SerializedName("product")
   @Expose
   public ArrayList<GroceryProductDataSet> productList;

   @SerializedName("vendor")
   @Expose
   public VendorDataDataSet vendorData;


}
