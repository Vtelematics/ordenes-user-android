package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MyOrderProductOptionDataSet {

   @SerializedName("option_name")
   @Expose
   private String option_name;

   @SerializedName("option_value")
   @Expose
   private String option_value;

   public String getOption_name() {
      return option_name;
   }

   public void setOption_name(String option_name) {
      this.option_name = option_name;
   }

   public String getOption_value() {
      return option_value;
   }

   public void setOption_value(String option_value) {
      this.option_value = option_value;
   }
}
