package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoginCustomerInfoDataSet {

   @SerializedName("secret_key")
   @Expose
   public String secret_key;

   @SerializedName("customer_id")
   @Expose
   public String customer_id;

   @SerializedName("firstname")
   @Expose
   public String firstname;

   @SerializedName("lastname")
   @Expose
   public String lastname;

   @SerializedName("email")
   @Expose
   public String email;

   @SerializedName("telephone")
   @Expose
   public String telephone;

   @SerializedName("image")
   @Expose
   public String image;

   public String getSecret_key() {
      return secret_key;
   }

   public void setSecret_key(String secret_key) {
      this.secret_key = secret_key;
   }

   public String getCustomer_id() {
      return customer_id;
   }

   public void setCustomer_id(String customer_id) {
      this.customer_id = customer_id;
   }

   public String getFirstname() {
      return firstname;
   }

   public void setFirstname(String firstname) {
      this.firstname = firstname;
   }

   public String getLastname() {
      return lastname;
   }

   public void setLastname(String lastname) {
      this.lastname = lastname;
   }

   public String getEmail() {
      return email;
   }

   public void setEmail(String email) {
      this.email = email;
   }

   public String getTelephone() {
      return telephone;
   }

   public void setTelephone(String telephone) {
      this.telephone = telephone;
   }

   public String getImage() {
      return image;
   }

   public void setImage(String image) {
      this.image = image;
   }
}
