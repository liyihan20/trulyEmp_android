package cn.com.truly.ic.trulyemp.models;

/**
 * Created by 110428101 on 2017-07-11.
 */

public class SimpleResultModel {
    private boolean suc;
    private String msg;
    private String extra;

    public boolean isSuc() {
        return suc;
    }

    public void setSuc(boolean suc) {
        this.suc = suc;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
