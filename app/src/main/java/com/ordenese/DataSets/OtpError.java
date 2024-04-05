package com.ordenese.DataSets;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
public class OtpError {
   @SerializedName("message")
   @Expose
   public String message;
   @SerializedName("status")
   @Expose
   public String status;

}
