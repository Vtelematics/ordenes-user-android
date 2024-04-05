package com.ordenese.DataSets;

import java.io.Serializable;
import java.util.ArrayList;

public class MenuAndItemsDataSet implements Serializable {

    private Boolean isParentMenu;
    private String parentSectionId;
    private String parentName;
    private String parentChildItemsCount;
    private String childProductMinQty;
    private String childProductId;
    private String childName,price_status;
    private String childProductDescription;
    private String childImage;
    private String childLogo;
    private String childImageThumb;
    private String childPrice;
    private String childOfferPrice;
    private String childIsOffer;
    private String childPriceOnSelection;
    private String childSortOrder;
    private String childProductToStore;
    private String childHasOption;

    private ArrayList<ProductOptionsData> productOptionList;
    private String item_type ;

    public String getPrice_status() {
        return price_status;
    }

    public void setPrice_status(String price_status) {
        this.price_status = price_status;
    }

    public String getChildImageThumb() {
        return childImageThumb;
    }

    public void setChildImageThumb(String childImageThumb) {
        this.childImageThumb = childImageThumb;
    }

    public String getChildProductMinQty() {
        return childProductMinQty;
    }

    public void setChildProductMinQty(String childProductMinQty) {
        this.childProductMinQty = childProductMinQty;
    }

    public String getItem_type() {
        return item_type;
    }

    public void setItem_type(String item_type) {
        this.item_type = item_type;
    }

    public ArrayList<ProductOptionsData> getProductOptionList() {
        return productOptionList;
    }

    public void setProductOptionList(ArrayList<ProductOptionsData> productOptionList) {
        this.productOptionList = productOptionList;
    }

    public String getChildHasOption() {
        return childHasOption;
    }

    public void setChildHasOption(String childHasOption) {
        this.childHasOption = childHasOption;
    }

    public String getChildImage() {
        return childImage;
    }

    public void setChildImage(String childImage) {
        this.childImage = childImage;
    }

    public String getChildIsOffer() {
        return childIsOffer;
    }

    public void setChildIsOffer(String childIsOffer) {
        this.childIsOffer = childIsOffer;
    }

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    public String getChildOfferPrice() {
        return childOfferPrice;
    }

    public void setChildOfferPrice(String childOfferPrice) {
        this.childOfferPrice = childOfferPrice;
    }

    public String getChildPrice() {
        return childPrice;
    }

    public void setChildPrice(String childPrice) {
        this.childPrice = childPrice;
    }

    public String getChildPriceOnSelection() {
        return childPriceOnSelection;
    }

    public void setChildPriceOnSelection(String childPriceOnSelection) {
        this.childPriceOnSelection = childPriceOnSelection;
    }

    public String getChildProductDescription() {
        return childProductDescription;
    }

    public void setChildProductDescription(String childProductDescription) {
        this.childProductDescription = childProductDescription;
    }

    public String getChildProductId() {
        return childProductId;
    }

    public void setChildProductId(String childProductId) {
        this.childProductId = childProductId;
    }

    public String getChildProductToStore() {
        return childProductToStore;
    }

    public void setChildProductToStore(String childProductToStore) {
        this.childProductToStore = childProductToStore;
    }

    public String getChildSortOrder() {
        return childSortOrder;
    }

    public void setChildSortOrder(String childSortOrder) {
        this.childSortOrder = childSortOrder;
    }

    public Boolean getParentMenu() {
        return isParentMenu;
    }

    public void setParentMenu(Boolean parentMenu) {
        isParentMenu = parentMenu;
    }

    public String getParentChildItemsCount() {
        return parentChildItemsCount;
    }

    public void setParentChildItemsCount(String parentChildItemsCount) {
        this.parentChildItemsCount = parentChildItemsCount;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getParentSectionId() {
        return parentSectionId;
    }

    public void setParentSectionId(String parentSectionId) {
        this.parentSectionId = parentSectionId;
    }

    public String getChildLogo() {
        return childLogo;
    }

    public void setChildLogo(String childLogo) {
        this.childLogo = childLogo;
    }
}
