package com.ordenese.DataSets;


import java.io.Serializable;
import java.util.ArrayList;

public class ProductOptionDataSet implements Serializable {

    private String productOptionId;
    private String optionId;
    private String name;
    private String type;
    private String value;
    private String required;
    private String minimumLimit;
    private String maximumLimit;
    private String sortOrder;
    private ArrayList<ProductOptionValueDataSet> productOptionValuesList;
    private Boolean optionValueListEmpty;

    public Boolean getOptionValueListEmpty() {
        return optionValueListEmpty;
    }

    public void setOptionValueListEmpty(Boolean optionValueListEmpty) {
        this.optionValueListEmpty = optionValueListEmpty;
    }

    public String getProductOptionId() {
        return productOptionId;
    }

    public void setProductOptionId(String productOptionId) {
        this.productOptionId = productOptionId;
    }

    public String getOptionId() {
        return optionId;
    }

    public void setOptionId(String optionId) {
        this.optionId = optionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getRequired() {
        return required;
    }

    public void setRequired(String required) {
        this.required = required;
    }

    public String getMinimumLimit() {
        return minimumLimit;
    }

    public void setMinimumLimit(String minimumLimit) {
        this.minimumLimit = minimumLimit;
    }

    public String getMaximumLimit() {
        return maximumLimit;
    }

    public void setMaximumLimit(String maximumLimit) {
        this.maximumLimit = maximumLimit;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public ArrayList<ProductOptionValueDataSet> getProductOptionValuesList() {
        return productOptionValuesList;
    }

    public void setProductOptionValuesList(ArrayList<ProductOptionValueDataSet> productOptionValuesList) {
        this.productOptionValuesList = productOptionValuesList;
    }
}
