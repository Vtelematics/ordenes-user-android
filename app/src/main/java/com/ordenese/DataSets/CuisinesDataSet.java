package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CuisinesDataSet {

   @SerializedName("cuisine_id")
   @Expose
   public String cuisine_id;

   @SerializedName("name")
   @Expose
   public String name;

   public String getCuisine_id() {
      return cuisine_id;
   }

   public void setCuisine_id(String cuisine_id) {
      this.cuisine_id = cuisine_id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }
}
