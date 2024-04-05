package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TopPickDataSet {

   @SerializedName("id")
   @Expose
   public String id;

   @SerializedName("name")
   @Expose
   public String name;

   @SerializedName("logo")
   @Expose
   public String logo;

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
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
