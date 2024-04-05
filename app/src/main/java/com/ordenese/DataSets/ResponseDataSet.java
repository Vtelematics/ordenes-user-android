package com.ordenese.DataSets;

public class ResponseDataSet {

    private String status;
    private String message;
    private Boolean isSuccess;
    private Boolean isResponseEmpty;
    private Boolean ifThereIsNoSuccessError;

    public Boolean getSuccess() {
        return isSuccess;
    }

    public void setSuccess(Boolean success) {
        isSuccess = success;
    }

    public Boolean getIfThereIsNoSuccessError() {
        return ifThereIsNoSuccessError;
    }

    public void setIfThereIsNoSuccessError(Boolean ifThereIsNoSuccessError) {
        this.ifThereIsNoSuccessError = ifThereIsNoSuccessError;
    }

    public Boolean getResponseEmpty() {
        return isResponseEmpty;
    }

    public void setResponseEmpty(Boolean responseEmpty) {
        isResponseEmpty = responseEmpty;
    }

    public Boolean getIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(Boolean isSuccess) {
        this.isSuccess = isSuccess;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
