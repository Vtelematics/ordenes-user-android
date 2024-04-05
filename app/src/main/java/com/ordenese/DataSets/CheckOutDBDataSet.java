package com.ordenese.DataSets;

public class CheckOutDBDataSet {

   private String couponId;
   private String couponCode;

   private String addressId;
   private String paymentListId;
   private String paymentCode;

   private String contactLessDeliveryChecked;

   public String getPaymentCode() {
      return paymentCode;
   }

   public void setPaymentCode(String paymentCode) {
      this.paymentCode = paymentCode;
   }

   public String getContactLessDeliveryChecked() {
      return contactLessDeliveryChecked;
   }

   public void setContactLessDeliveryChecked(String contactLessDeliveryChecked) {
      this.contactLessDeliveryChecked = contactLessDeliveryChecked;
   }

   public String getAddressId() {
      return addressId;
   }

   public void setAddressId(String addressId) {
      this.addressId = addressId;
   }

   public String getPaymentListId() {
      return paymentListId;
   }

   public void setPaymentListId(String paymentListId) {
      this.paymentListId = paymentListId;
   }

   public String getCouponId() {
      return couponId;
   }

   public void setCouponId(String couponId) {
      this.couponId = couponId;
   }

   public String getCouponCode() {
      return couponCode;
   }

   public void setCouponCode(String couponCode) {
      this.couponCode = couponCode;
   }
}
