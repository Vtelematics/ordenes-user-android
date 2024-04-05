package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class CartProductsDataSet {

   @SerializedName("cart_id")
   @Expose
   public String cart_id;

   @SerializedName("product_id")
   @Expose
   public String product_id;

   @SerializedName("name")
   @Expose
   public String name;

   @SerializedName("image")
   @Expose
   public String image;

   @SerializedName("quantity")
   @Expose
   public String quantity;

   @SerializedName("stock_status")
   @Expose
   public String stock_status;

   @SerializedName("price")
   @Expose
   public String price;

   @SerializedName("total")
   @Expose
   public String total;

   @SerializedName("discount_price")
   @Expose
   public String discount_price;

   @SerializedName("actual_total")
   @Expose
   public String actual_total;

   @SerializedName("option")
   @Expose
   public ArrayList<CartProductOptionsDataSet> optionsList;

   public String getStock_status() {
      return stock_status;
   }

   public void setStock_status(String stock_status) {
      this.stock_status = stock_status;
   }

   public String getCart_id() {
      return cart_id;
   }

   public void setCart_id(String cart_id) {
      this.cart_id = cart_id;
   }

   public String getProduct_id() {
      return product_id;
   }

   public void setProduct_id(String product_id) {
      this.product_id = product_id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getImage() {
      return image;
   }

   public void setImage(String image) {
      this.image = image;
   }

   public String getQuantity() {
      return quantity;
   }

   public void setQuantity(String quantity) {
      this.quantity = quantity;
   }


   public String getPrice() {
      return price;
   }

   public void setPrice(String price) {
      this.price = price;
   }

   public String getTotal() {
      return total;
   }

   public void setTotal(String total) {
      this.total = total;
   }

   public ArrayList<CartProductOptionsDataSet> getOptionsList() {
      return optionsList;
   }

   public void setOptionsList(ArrayList<CartProductOptionsDataSet> optionsList) {
      this.optionsList = optionsList;
   }
}
