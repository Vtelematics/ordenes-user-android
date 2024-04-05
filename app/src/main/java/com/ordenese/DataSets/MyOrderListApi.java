package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class MyOrderListApi {

   @SerializedName("success")
   @Expose
   public SuccessDataSet success;

   @SerializedName("error")
   @Expose
   public ErrorDataSet error;

   @SerializedName("orders")
   @Expose
   public ArrayList<MyOrderDataSet> myOrderList;

   @SerializedName("total")
   @Expose
   public String total;




}
