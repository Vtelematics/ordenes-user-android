package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FilterDataSet {

   @SerializedName("filter_id")
   @Expose
   public String filter_id;

   @SerializedName("key")
   @Expose
   public String key;

   @SerializedName("name")
   @Expose
   public String name;

   @SerializedName("logo")
   @Expose
   public String logo;


   public String getKey() {
      return key;
   }

   public void setKey(String key) {
      this.key = key;
   }

   public String getFilter_id() {
      return filter_id;
   }

   public void setFilter_id(String filter_id) {
      this.filter_id = filter_id;
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
