package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CountryCodeDataSet {

   @SerializedName("id")
   @Expose
   public String id;

   @SerializedName("name")
   @Expose
   public String name;

   @SerializedName("code")
   @Expose
   public String code;

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

   public String getCode() {
      return code;
   }

   public void setCode(String code) {
      this.code = code;
   }
}
