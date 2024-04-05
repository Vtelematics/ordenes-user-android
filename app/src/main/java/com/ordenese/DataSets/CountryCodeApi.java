package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class CountryCodeApi {

   @SerializedName("success")
   @Expose
   public SuccessDataSet success;

   @SerializedName("error")
   @Expose
   public ErrorDataSet error;

   @SerializedName("countries")
   @Expose
   public ArrayList<CountryCodeDataSet> countryList;


}
