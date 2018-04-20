package com.example.heavymentaldelection.Info;

import java.util.Date;

public class HeavyMentalDataInfo {
    private Integer id;//信息ID
    private String city;//检测所在的城市
    private String address;//详细地址
    private String pollution;//污染情况
    private String sensor;//传感器类型
    private Double longitude;//经度
    private Double latitude;//纬度
    private Date createtime;//检测时间
    private String detail;//详细信息

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city == null ? null : city.trim();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address == null ? null : address.trim();
    }

    public String getPollution() {
        return pollution;
    }

    public void setPollution(String pollution) {
        this.pollution = pollution == null ? null : pollution.trim();
    }

    public String getSensor() {
        return sensor;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor == null ? null : sensor.trim();
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail == null ? null : detail.trim();
    }

    @Override
    public String toString() {
        return "HeavyMentalDataInfo{" +
                "id=" + id +
                ", city='" + city + '\'' +
                ", address='" + address + '\'' +
                ", pollution='" + pollution + '\'' +
                ", sensor='" + sensor + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", createtime=" + createtime +
                ", detail='" + detail + '\'' +
                '}';
    }
}