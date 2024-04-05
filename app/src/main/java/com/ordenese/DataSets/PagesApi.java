package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PagesApi {

   @SerializedName("success")
   @Expose
   public SuccessDataSet success;

   @SerializedName("error")
   @Expose
   public ErrorDataSet error;

   @SerializedName("info")
   @Expose
   public PagesDataset pagesData;



}
