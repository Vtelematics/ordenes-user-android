package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EncriptDataSet {
   @SerializedName("now")
   @Expose
   public String now;
   @SerializedName("encript_code")
   @Expose
   public String encriptCode;

}
