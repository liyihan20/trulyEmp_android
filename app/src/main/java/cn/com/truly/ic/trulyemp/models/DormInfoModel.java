package cn.com.truly.ic.trulyemp.models;

/**
 * Created by 110428101 on 2017-08-03.
 */

public class DormInfoModel {
    private String livingStatus;
    private String areaName;
    private String dormNumber;
    private String inDate;
    private String feeMonths;

    public String getLivingStatus() {
        return livingStatus;
    }

    public void setLivingStatus(String livingStatus) {
        this.livingStatus = livingStatus;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getDormNumber() {
        return dormNumber;
    }

    public void setDormNumber(String dormNumber) {
        this.dormNumber = dormNumber;
    }

    public String getInDate() {
        return inDate;
    }

    public void setInDate(String inDate) {
        this.inDate = inDate;
    }

    public String getFeeMonths() {
        return feeMonths;
    }

    public void setFeeMonths(String feeMonths) {
        this.feeMonths = feeMonths;
    }
}
