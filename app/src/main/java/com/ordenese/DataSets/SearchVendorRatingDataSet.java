package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SearchVendorRatingDataSet {

   @SerializedName("rating")
   @Expose
   public String rating;
   @SerializedName("vendor_rating_image")
   @Expose
   public String vendor_rating_image;
   @SerializedName("vendor_rating_name")
   @Expose
   public String vendor_rating_name;
   public String getRating() {
      return rating;
   }

   public void setRating(String rating) {
      this.rating = rating;
   }
}
