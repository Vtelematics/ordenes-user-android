package com.ordenese.DataSets;


public class ListDataSet {


    private String restaurantId;
    private String name;
    private String image;
    private String logo;
    private String preparingTime;
    private String newStatus;
    private String workingStatus;
    private String rating;
    private String cuisine;
    private String restaurantStatus;
    private String sortStatus;
    private String deliveryCharge;
    private String minimumAmount;
    private String deliveryTime,offer;

    private String branchDescription;
    private String branchDescriptionArabic;
    private Boolean restaurantsListEmpty;

    //setRestaurantsListEmpty
    //getRestaurantId
    //getName
    //getImage
    //getLogo
    //getPreparingTime
    //setNewStatus
    //setWorkingStatus
    //getRating
    //setCuisine
    //setRestaurantStatus
    //setSortStatus
    //getDeliveryCharge
    //setMinimumAmount
    //getDeliveryTime



    public String getOffer() {
        return offer;
    }

    public void setOffer(String offer) {
        this.offer = offer;
    }

    public Boolean getRestaurantsListEmpty() {
        return restaurantsListEmpty;
    }

    public void setRestaurantsListEmpty(Boolean restaurantsListEmpty) {
        this.restaurantsListEmpty = restaurantsListEmpty;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
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

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getPreparingTime() {
        return preparingTime;
    }

    public void setPreparingTime(String preparingTime) {
        this.preparingTime = preparingTime;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getWorkingStatus() {
        return workingStatus;
    }

    public void setWorkingStatus(String workingStatus) {
        this.workingStatus = workingStatus;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getCuisine() {
        return cuisine;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public String getRestaurantStatus() {
        return restaurantStatus;
    }

    public void setRestaurantStatus(String restaurantStatus) {
        this.restaurantStatus = restaurantStatus;
    }

    public String getSortStatus() {
        return sortStatus;
    }

    public void setSortStatus(String sortStatus) {
        this.sortStatus = sortStatus;
    }

    public String getDeliveryCharge() {
        return deliveryCharge;
    }

    public void setDeliveryCharge(String deliveryCharge) {
        this.deliveryCharge = deliveryCharge;
    }

    public String getMinimumAmount() {
        return minimumAmount;
    }

    public void setMinimumAmount(String minimumAmount) {
        this.minimumAmount = minimumAmount;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public String getBranchDescription() {
        return branchDescription;
    }

    public void setBranchDescription(String branchDescription) {
        this.branchDescription = branchDescription;
    }

    public String getBranchDescriptionArabic() {
        return branchDescriptionArabic;
    }

    public void setBranchDescriptionArabic(String branchDescriptionArabic) {
        this.branchDescriptionArabic = branchDescriptionArabic;
    }
}
