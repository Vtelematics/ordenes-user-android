package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ConfirmOrderApi {

   @SerializedName("success")
   @Expose
   public SuccessDataSet success;

   @SerializedName("error")
   @Expose
   public ErrorDataSet error;

   @SerializedName("order_id")
   @Expose
   public String order_id;



   public String getOrder_id() {
      return order_id;
   }

   public void setOrder_id(String order_id) {
      this.order_id = order_id;
   }
}
