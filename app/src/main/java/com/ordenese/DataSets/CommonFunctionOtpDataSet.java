package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CommonFunctionOtpDataSet {

   @SerializedName("encript")
   @Expose
   public EncriptDataSet encript;
   @SerializedName("otp_type")
   @Expose
   public String otpType;
   @SerializedName("success")
   @Expose
   public Success success;

}
