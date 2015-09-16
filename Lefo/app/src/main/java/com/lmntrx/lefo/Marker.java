package com.lmntrx.lefo;

/**
 * Created by livin on 16/9/15.
 */
public class Marker {
    private String mLabel;
    private String mIcon;
    private Double mLatitude;
    private Double mLongitude;

    public Marker(String label, String icon, Double latitude, Double longitude)
    {
        this.mLabel = label;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mIcon = icon;
    }
    public Marker(String icon, Double latitude, Double longitude)
    {
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mIcon = icon;
    }
    public Marker(Double latitude, Double longitude)
    {
        this.mLatitude = latitude;
        this.mLongitude = longitude;
    }

    public String getmLabel()
    {
        return mLabel;
    }

    public void setmLabel(String mLabel)
    {
        this.mLabel = mLabel;
    }

    public String getmIcon()
    {
        return mIcon;
    }

    public void setmIcon(String icon)
    {
        this.mIcon = icon;
    }

    public Double getmLatitude()
    {
        return mLatitude;
    }

    public void setLatitude(Double mLatitude)
    {
        this.mLatitude = mLatitude;
    }

    public Double getmLongitude()
    {
        return mLongitude;
    }

    public void setLongitude(Double mLongitude)
    {
        this.mLongitude = mLongitude;
    }
}
