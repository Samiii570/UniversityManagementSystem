package com.kuet.ums.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "admins")
public class Admin extends BaseUser {
    @Override
    public Role getRole() { return Role.ADMIN; }
}
