package com.ordenese.DataSets;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OtpSuccess {

   @SerializedName("success")
   @Expose
   public Success success;

   @SerializedName("error")
   @Expose
   public OtpError error;

}
