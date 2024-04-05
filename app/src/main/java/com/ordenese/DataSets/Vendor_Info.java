package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Vendor_Info implements Serializable {


    @SerializedName("vendor")
    @Expose
    private Vendor vendor;
    @SerializedName("success")
    @Expose
    private Success success;

    @SerializedName("error")
    @Expose
    public ErrorDataSet error;

    public ErrorDataSet getError() {
        return error;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public Success getSuccess() {
        return success;
    }

    public void setSuccess(Success success) {
        this.success = success;
    }
}
