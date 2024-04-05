package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class PaymentMethodsApi {

   @SerializedName("success")
   @Expose
   public SuccessDataSet success;

   @SerializedName("error")
   @Expose
   public ErrorDataSet error;

   @SerializedName("address")
   @Expose
   public CheckOutAddsDataSet checkOutAdds;

   @SerializedName("payment")
   @Expose
   public ArrayList<PaymentDataSet> paymentMethodsList;

   @SerializedName("schedule")
   @Expose
   public ScheduleDeliveryDataSet schedule;

   @SerializedName("delivery_time")
   @Expose
   public String delivery_time;

   @SerializedName("schedule_status")
   @Expose
   public String schedule_status;

   @SerializedName("order_type")
   @Expose
   public String order_type;

   @SerializedName("error_warning")
   @Expose
   public String error_warning;

}
