package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class WishListDataSet {

    @SerializedName("vendor_list")
    @Expose
    private ArrayList<WishlistVendor> vendorList = null;
    @SerializedName("success")
    @Expose
    private Success success;

    public ArrayList<WishlistVendor> getVendorList() {
        return vendorList;
    }

    public void setVendorList(ArrayList<WishlistVendor> vendorList) {
        this.vendorList = vendorList;
    }

    public Success getSuccess() {
        return success;
    }

    public void setSuccess(Success success) {
        this.success = success;
    }

}
