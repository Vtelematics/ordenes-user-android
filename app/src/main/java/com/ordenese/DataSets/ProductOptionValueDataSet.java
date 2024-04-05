package com.ordenese.DataSets;

public class ProductOptionValueDataSet {

    private String productOptionValueId;
    private String optionValueId;
    private String name;
    private String price;
    private String sortOrder;

    public String getProductOptionValueId() {
        return productOptionValueId;
    }

    public void setProductOptionValueId(String productOptionValueId) {
        this.productOptionValueId = productOptionValueId;
    }

    public String getOptionValueId() {
        return optionValueId;
    }

    public void setOptionValueId(String optionValueId) {
        this.optionValueId = optionValueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }
}
