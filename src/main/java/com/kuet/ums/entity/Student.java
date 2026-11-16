package com.kuet.ums.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "students")
public class Student extends BaseUser {
    @Column(nullable = false, unique = true)
    private String roll;

    private String department;
    private Integer batch;

    @Override
    public Role getRole() { return Role.STUDENT; }

    public String getRoll() { return roll; }
    public void setRoll(String roll) { this.roll = roll; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Integer getBatch() { return batch; }
    public void setBatch(Integer batch) { this.batch = batch; }
}
