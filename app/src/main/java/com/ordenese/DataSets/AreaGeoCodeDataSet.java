package com.ordenese.DataSets;

public class AreaGeoCodeDataSet {

    private String mLatitude;
    private String mLongitude;
    private String mAddress;
    private String mIndex;
    private String mNewAdds;

    public String getmAddsNameOnly() {
        return mNewAdds;
    }

    public void setmAddsNameOnly(String mNewAdds) {
        this.mNewAdds = mNewAdds;
    }

    public String getmIndex() {
        return mIndex;
    }

    public void setmIndex(String mIndex) {
        this.mIndex = mIndex;
    }

    public String getmLatitude() {
        return mLatitude;
    }

    public void setmLatitude(String mLatitude) {
        this.mLatitude = mLatitude;
    }

    public String getmLongitude() {
        return mLongitude;
    }

    public void setmLongitude(String mLongitude) {
        this.mLongitude = mLongitude;
    }

    public String getmAddress() {
        return mAddress;
    }

    public void setmAddress(String mAddress) {
        this.mAddress = mAddress;
    }
}
