package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PaymentDataSet {

   @SerializedName("payment_list_id")
   @Expose
   public String payment_list_id;

   @SerializedName("payment_name")
   @Expose
   public String payment_name;

   @SerializedName("payment_code")
   @Expose
   public String payment_code;

   public String getPayment_list_id() {
      return payment_list_id;
   }

   public void setPayment_list_id(String payment_list_id) {
      this.payment_list_id = payment_list_id;
   }

   public String getPayment_name() {
      return payment_name;
   }

   public void setPayment_name(String payment_name) {
      this.payment_name = payment_name;
   }

   public String getPayment_code() {
      return payment_code;
   }

   public void setPayment_code(String payment_code) {
      this.payment_code = payment_code;
   }
}
