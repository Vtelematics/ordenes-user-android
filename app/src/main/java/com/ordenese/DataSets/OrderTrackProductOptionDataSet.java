package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OrderTrackProductOptionDataSet {

   @SerializedName("option_name")
   @Expose
   public String option_name;

   @SerializedName("option_value")
   @Expose
   public String option_value;


}
