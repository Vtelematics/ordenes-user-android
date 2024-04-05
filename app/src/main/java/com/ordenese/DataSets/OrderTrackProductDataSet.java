package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class OrderTrackProductDataSet {

   @SerializedName("name")
   @Expose
   public String name;

   @SerializedName("quantity")
   @Expose
   public String quantity;

   @SerializedName("price")
   @Expose
   public String price;

   @SerializedName("total")
   @Expose
   public String total;

   @SerializedName("option")
   @Expose
   public ArrayList<OrderTrackProductOptionDataSet> orderTrackProductOption;


}
