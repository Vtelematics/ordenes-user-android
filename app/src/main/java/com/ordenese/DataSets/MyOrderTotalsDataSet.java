package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MyOrderTotalsDataSet {

   @SerializedName("title")
   @Expose
   private String title;

   @SerializedName("text")
   @Expose
   private String text;

   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public String getText() {
      return text;
   }

   public void setText(String text) {
      this.text = text;
   }
}
