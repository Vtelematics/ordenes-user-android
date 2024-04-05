package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Product {
    @SerializedName("product_item_id")
    @Expose
    private String productItemId;
    @SerializedName("item_name")
    @Expose
    private String itemName;
    @SerializedName("description")
    @Expose
    private String description;

    @SerializedName("image")
    @Expose
    private String image;

    @SerializedName("logo")
    @Expose
    private String logo;


    @SerializedName("price")
    @Expose
    private String price;
    @SerializedName("discount")
    @Expose
    private String discount;
    @SerializedName("stock")
    @Expose
    private String stock;
    @SerializedName("has_options")
    @Expose
    private String hasOptions;
    @SerializedName("price_status")
    @Expose
    private String price_status;
    @SerializedName("options")
    @Expose
    private ArrayList<ProductOptionsData> options = null;

    public String getPrice_status() {
        return price_status;
    }

    public void setPrice_status(String price_status) {
        this.price_status = price_status;
    }

    public String getProductItemId() {
        return productItemId;
    }

    public void setProductItemId(String productItemId) {
        this.productItemId = productItemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getStock() {
        return stock;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }

    public String getHasOptions() {
        return hasOptions;
    }

    public void setHasOptions(String hasOptions) {
        this.hasOptions = hasOptions;
    }

    public ArrayList<ProductOptionsData> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<ProductOptionsData> options) {
        this.options = options;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }
}
