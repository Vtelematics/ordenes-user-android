package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class GroceryCategoryDataSet implements Serializable {

   @SerializedName("category_id")
   @Expose
   public String category_id;

   @SerializedName("name")
   @Expose
   public String name;

   @SerializedName("picture")
   @Expose
   public String picture;



}
