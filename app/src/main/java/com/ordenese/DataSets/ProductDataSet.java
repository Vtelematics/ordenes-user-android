package com.ordenese.DataSets;


import java.util.ArrayList;

public class ProductDataSet {

    private String productId,vendor_id;
    private String name;
    private String productDescription;
    private String image;
    private String imageThumb;



    private String price;
    private String offerPrice;
    private String isOffer;
    private String priceOnselection;
    private String sortOrder;
    private String productToStore;
    private int hasOption;
    private String deliveryNote;
    private String ItemNote;
    private String productToMenuId;
    private String newStatus;
    private String stock;
    private String isFavorite;
    private ArrayList<ProductOptionDataSet> productOptionList;
    private Boolean optionListEmpty;
    private String botanicalName;
    private String droughtTolerance;
    private String bloom;
    private String deciduous;
    private String containerSizes;
    private String soilType;
    private String plantHeight;
    private String growthRate;
    private String zoneList;
    private String matureHeight;
    private String matureWidth;
    private String sunlight;
    private String spacing;
    private String item_type;


    public String getImageThumb() {
        return imageThumb;
    }

    public void setImageThumb(String imageThumb) {
        this.imageThumb = imageThumb;
    }

    public String getItem_type() {
        return item_type;
    }

    public void setItem_type(String item_type) {
        this.item_type = item_type;
    }

    public String getVendor_id() {
        return vendor_id;
    }

    public void setVendor_id(String vendor_id) {
        this.vendor_id = vendor_id;
    }

    public String getBotanicalName() {
        return botanicalName;
    }

    public void setBotanicalName(String botanicalName) {
        this.botanicalName = botanicalName;
    }

    public String getDroughtTolerance() {
        return droughtTolerance;
    }

    public void setDroughtTolerance(String droughtTolerance) {
        this.droughtTolerance = droughtTolerance;
    }

    public String getBloom() {
        return bloom;
    }

    public void setBloom(String bloom) {
        this.bloom = bloom;
    }

    public String getDeciduous() {
        return deciduous;
    }

    public void setDeciduous(String deciduous) {
        this.deciduous = deciduous;
    }

    public String getContainerSizes() {
        return containerSizes;
    }

    public void setContainerSizes(String containerSizes) {
        this.containerSizes = containerSizes;
    }

    public String getSoilType() {
        return soilType;
    }

    public void setSoilType(String soilType) {
        this.soilType = soilType;
    }

    public String getPlantHeight() {
        return plantHeight;
    }

    public void setPlantHeight(String plantHeight) {
        this.plantHeight = plantHeight;
    }

    public String getGrowthRate() {
        return growthRate;
    }

    public void setGrowthRate(String growthRate) {
        this.growthRate = growthRate;
    }

    public String getZoneList() {
        return zoneList;
    }

    public void setZoneList(String zoneList) {
        this.zoneList = zoneList;
    }

    public String getMatureHeight() {
        return matureHeight;
    }

    public void setMatureHeight(String matureHeight) {
        this.matureHeight = matureHeight;
    }

    public String getMatureWidth() {
        return matureWidth;
    }

    public void setMatureWidth(String matureWidth) {
        this.matureWidth = matureWidth;
    }

    public String getSunlight() {
        return sunlight;
    }

    public void setSunlight(String sunlight) {
        this.sunlight = sunlight;
    }

    public String getSpacing() {
        return spacing;
    }

    public void setSpacing(String spacing) {
        this.spacing = spacing;
    }

    public Boolean getOptionListEmpty() {
        return optionListEmpty;
    }

    public void setOptionListEmpty(Boolean optionListEmpty) {
        this.optionListEmpty = optionListEmpty;
    }

    public ArrayList<ProductOptionDataSet> getProductOptionList() {
        return productOptionList;
    }

    public void setProductOptionList(ArrayList<ProductOptionDataSet> productOptionList) {
        this.productOptionList = productOptionList;
    }

    public String getProductToMenuId() {
        return productToMenuId;
    }

    public void setProductToMenuId(String productToMenuId) {
        this.productToMenuId = productToMenuId;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getStock() {
        return stock;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }

    public String getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(String isFavorite) {
        this.isFavorite = isFavorite;
    }

    public String getMinimum() {
        return minimum;
    }

    public void setMinimum(String minimum) {
        this.minimum = minimum;
    }

    private String minimum;

    public String getDeliveryNote() {
        return deliveryNote;
    }

    public void setDeliveryNote(String deliveryNote) {
        this.deliveryNote = deliveryNote;
    }

    public String getItemNote() {
        return ItemNote;
    }

    public void setItemNote(String itemNote) {
        ItemNote = itemNote;
    }

    public int getHasOption() {
        return hasOption;
    }

    public void setHasOption(int hasOption) {
        this.hasOption = hasOption;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getIsOffer() {
        return isOffer;
    }

    public void setIsOffer(String isOffer) {
        this.isOffer = isOffer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOfferPrice() {
        return offerPrice;
    }

    public void setOfferPrice(String offerPrice) {
        this.offerPrice = offerPrice;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPriceOnselection() {
        return priceOnselection;
    }

    public void setPriceOnselection(String priceOnselection) {
        this.priceOnselection = priceOnselection;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductToStore() {
        return productToStore;
    }

    public void setProductToStore(String productToStore) {
        this.productToStore = productToStore;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }
}
