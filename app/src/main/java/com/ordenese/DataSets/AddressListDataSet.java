package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AddressListDataSet implements Serializable {

    @SerializedName("address_id")
    @Expose
    public String address_id;

    @SerializedName("customer_id")
    @Expose
    public String customer_id;

    @SerializedName("first_name")
    @Expose
    public String first_name;

    @SerializedName("last_name")
    @Expose
    public String last_name;

    @SerializedName("country_code")
    @Expose
    public String country_code;

    @SerializedName("mobile")
    @Expose
    public String mobile;

    @SerializedName("email")
    @Expose
    public String email;

    @SerializedName("landline")
    @Expose
    public String landline;

    @SerializedName("area")
    @Expose
    public String area;

    @SerializedName("zone_id")
    @Expose
    public String zone_id;

    @SerializedName("address_type")
    @Expose
    public String address_type;

    @SerializedName("block")
    @Expose
    public String block;

    @SerializedName("street")
    @Expose
    public String street;

    @SerializedName("way")
    @Expose
    public String way;

    @SerializedName("building_name")
    @Expose
    public String building_name;

    @SerializedName("floor")
    @Expose
    public String floor;

    @SerializedName("door_no")
    @Expose
    public String door_no;

    @SerializedName("additional_direction")
    @Expose
    public String additional_direction;

    @SerializedName("latitude")
    @Expose
    public String latitude;

    @SerializedName("longitude")
    @Expose
    public String longitude;

    @SerializedName("address")
    @Expose
    public String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getZone_id() {
        return zone_id;
    }

    public void setZone_id(String zone_id) {
        this.zone_id = zone_id;
    }

    public String getAddress_id() {
        return address_id;
    }

    public void setAddress_id(String address_id) {
        this.address_id = address_id;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(String customer_id) {
        this.customer_id = customer_id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getCountry_code() {
        return country_code;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getLandline() {
        return landline;
    }

    public void setLandline(String landline) {
        this.landline = landline;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getAddress_type() {
        return address_type;
    }

    public void setAddress_type(String address_type) {
        this.address_type = address_type;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getWay() {
        return way;
    }

    public void setWay(String way) {
        this.way = way;
    }

    public String getBuilding_name() {
        return building_name;
    }

    public void setBuilding_name(String building_name) {
        this.building_name = building_name;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getDoor_no() {
        return door_no;
    }

    public void setDoor_no(String door_no) {
        this.door_no = door_no;
    }

    public String getAdditional_direction() {
        return additional_direction;
    }

    public void setAdditional_direction(String additional_direction) {
        this.additional_direction = additional_direction;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
