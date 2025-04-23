package com.jesse.examination.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 用户角色实体类。
 */
@Data
@Entity
@Table(name = "user_roles")
public class RoleEntity
{
    @Id
    @Column(name = "id", nullable = false)
    Long roleId;        // 角色 ID

    @Column(name = "role_name", nullable = false)
    String roleName;    // 角色名
}
