package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BusinessTypesDataSet {

   @SerializedName("type_id")
   @Expose
   public String type_id;

   @SerializedName("name")
   @Expose
   public String name;

   @SerializedName("logo")
   @Expose
   public String logo;

   public String getType_id() {
      return type_id;
   }

   public void setType_id(String type_id) {
      this.type_id = type_id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getLogo() {
      return logo;
   }

   public void setLogo(String logo) {
      this.logo = logo;
   }
}
