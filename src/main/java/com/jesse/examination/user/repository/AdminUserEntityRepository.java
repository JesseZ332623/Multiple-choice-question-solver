package com.jesse.examination.user.repository;

import com.jesse.examination.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AdminUserEntityRepository extends JpaRepository<UserEntity, Long>
{
    /**
     * 通过用户名查找用户实体。（JPA 自动实现）
     */
    Optional<UserEntity>
    findUserByUserName(String userName);

    /**
     * 删除通过用户名删除用户实体。（JPA 自动实现）
     */
    void deleteUserByUserName(String userName);

    /**
     * 通过 full name 验证所对应的数据行是否存在。
     */
    boolean existsByFullName(String fullName);

    /**
     * 通过 user name 验证所对应的数据行是否存在。
     */
    boolean existsByUserName(String userName);
}