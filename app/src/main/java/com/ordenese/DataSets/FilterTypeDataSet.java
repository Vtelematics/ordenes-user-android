package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FilterTypeDataSet {

   @SerializedName("filter_type_id")
   @Expose
   public String filter_type_id;

   @SerializedName("name")
   @Expose
   public String name;

   @SerializedName("status")
   @Expose
   public String status;

   public String getFilter_type_id() {
      return filter_type_id;
   }

   public void setFilter_type_id(String filter_type_id) {
      this.filter_type_id = filter_type_id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getStatus() {
      return status;
   }

   public void setStatus(String status) {
      this.status = status;
   }
}
