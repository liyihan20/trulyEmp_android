package cn.com.truly.ic.trulyemp.models;

/**
 * Created by 110428101 on 2017-08-15.
 */

public class SimpleSearchUserModel {
    private int userId ;
    private String userName ;
    private String cardNumber ;
    private String shortDepName ;
    private String userStatus ;
    private String sex;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getShortDepName() {
        return shortDepName;
    }

    public void setShortDepName(String shortDepName) {
        this.shortDepName = shortDepName;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }
}
