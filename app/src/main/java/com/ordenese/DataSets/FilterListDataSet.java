package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class FilterListDataSet {

   @SerializedName("filter_id")
   @Expose
   public String filter_id;

   @SerializedName("name")
   @Expose
   public String name;

   @SerializedName("type")
   @Expose
   public String type;

   @SerializedName("status")
   @Expose
   public String status;

   @SerializedName("filter_type")
   @Expose
   public ArrayList<FilterTypeDataSet> filterTypeList;

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

   public String getType() {
      return type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public String getStatus() {
      return status;
   }

   public void setStatus(String status) {
      this.status = status;
   }

   public ArrayList<FilterTypeDataSet> getFilterTypeList() {
      return filterTypeList;
   }

   public void setFilterTypeList(ArrayList<FilterTypeDataSet> filterTypeList) {
      this.filterTypeList = filterTypeList;
   }
}
