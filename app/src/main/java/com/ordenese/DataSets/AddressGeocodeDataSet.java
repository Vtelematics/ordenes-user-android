package com.ordenese.DataSets;


public class AddressGeocodeDataSet {

    private String restaurantId;
    private String restaurantName;
    private String latitude;
    private String longitude;
    private String address;
    private String geocode;
    private Boolean responseEmpty;
    private String preparingTime;
    private String deliveryTime;
    private String checkOutNote;

    public String getCheckOutNote() {
        return checkOutNote;
    }

    public void setCheckOutNote(String checkOutNote) {
        this.checkOutNote = checkOutNote;
    }

    public String getPreparingTime() {
        return preparingTime;
    }

    public void setPreparingTime(String preparingTime) {
        this.preparingTime = preparingTime;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public String getGeocode() {
        return geocode;
    }

    public void setGeocode(String geocode) {
        this.geocode = geocode;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public Boolean getResponseEmpty() {
        return responseEmpty;
    }

    public void setResponseEmpty(Boolean responseEmpty) {
        this.responseEmpty = responseEmpty;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
