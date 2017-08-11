package cn.com.truly.ic.trulyemp.models;

/**
 * Created by 110428101 on 2017-08-10.
 */

public class RechargeRecordModel {
    private String rechargeSum;
    private String rechargeTime;
    private String place;
    private String beforeSum;
    private String afterSum;

    public String getRechargeSum() {
        return rechargeSum;
    }

    public void setRechargeSum(String rechargeSum) {
        this.rechargeSum = rechargeSum;
    }

    public String getRechargeTime() {
        return rechargeTime;
    }

    public void setRechargeTime(String rechargeTime) {
        this.rechargeTime = rechargeTime;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getBeforeSum() {
        return beforeSum;
    }

    public void setBeforeSum(String beforeSum) {
        this.beforeSum = beforeSum;
    }

    public String getAfterSum() {
        return afterSum;
    }

    public void setAfterSum(String afterSum) {
        this.afterSum = afterSum;
    }
}
