package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GroceryProductDataSet {

    @SerializedName("product_item_id")
    @Expose
    public String product_item_id;

    @SerializedName("item_name")
    @Expose
    public String item_name;

    @SerializedName("description")
    @Expose
    public String description;

    @SerializedName("price")
    @Expose
    public String price;

    @SerializedName("qty")
    @Expose
    public String qty;

    @SerializedName("cart_qty")
    @Expose
    public String cart_qty;

    @SerializedName("cart_id")
    @Expose
    public String cart_id;

    @SerializedName("discount")
    @Expose
    public String discount;

    @SerializedName("picture")
    @Expose
    public String picture;

    @SerializedName("logo")
    @Expose
    public String logo;

    @SerializedName("vendorData")
    @Expose
    public VendorDataDataSet vendorData;

    public String getProduct_item_id() {
        return product_item_id;
    }

    public void setProduct_item_id(String product_item_id) {
        this.product_item_id = product_item_id;
    }

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getCart_qty() {
        return cart_qty;
    }

    public void setCart_qty(String cart_qty) {
        this.cart_qty = cart_qty;
    }

    public String getCart_id() {
        return cart_id;
    }

    public void setCart_id(String cart_id) {
        this.cart_id = cart_id;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public VendorDataDataSet getVendorData() {
        return vendorData;
    }

    public void setVendorData(VendorDataDataSet vendorData) {
        this.vendorData = vendorData;
    }
}
