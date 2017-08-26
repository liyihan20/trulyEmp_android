package cn.com.truly.ic.trulyemp.models;

import java.math.BigDecimal;

/**
 * Created by 110428101 on 2017-08-23.
 */

public class SalaryInfoModel {
    private double basicSalary;
    private String salaryType;
    private String lastSalaryDate;

    public double getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(double basicSalary) {
        this.basicSalary = basicSalary;
    }

    public String getSalaryType() {
        return salaryType;
    }

    public void setSalaryType(String salaryType) {
        this.salaryType = salaryType;
    }

    public String getLastSalaryDate() {
        return lastSalaryDate;
    }

    public void setLastSalaryDate(String lastSalaryDate) {
        this.lastSalaryDate = lastSalaryDate;
    }
}
