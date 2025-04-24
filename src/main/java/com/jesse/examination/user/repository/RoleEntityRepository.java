package com.jesse.examination.user.repository;

import com.jesse.examination.user.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleEntityRepository extends JpaRepository<RoleEntity, Long>
{
    /**
     * 通过角色名查找角色实体。（JPA 自动实现）。
     */
    RoleEntity findRoleByRoleName(String roleName);
}
