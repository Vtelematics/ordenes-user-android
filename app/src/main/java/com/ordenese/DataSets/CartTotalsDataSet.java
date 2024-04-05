package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CartTotalsDataSet {

    @SerializedName("title")
    @Expose
    public String title;

    @SerializedName("text")
    @Expose
    public String text;
    @SerializedName("currency")
    @Expose
    public String currency;
    @SerializedName("amount")
    @Expose
    public String amount;
    @SerializedName("title_key")
    @Expose
    public String title_key;

    @SerializedName("text_amount")
    @Expose
    public String text_amount;

    public String getCurrency() {
        return currency;
    }

    public String getAmount() {
        return amount;
    }

    public String getText_amount() {
        return text_amount;
    }

    public void setText_amount(String text_amount) {
        this.text_amount = text_amount;
    }

    public String getTitle_key() {
        return title_key;
    }

    public void setTitle_key(String title_key) {
        this.title_key = title_key;
    }

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
