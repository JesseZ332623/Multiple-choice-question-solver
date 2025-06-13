package com.jesse.examination.user.repository;

import com.jesse.examination.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserEntityRepository extends JpaRepository<UserEntity, Long>
{
    /**
     * 通过用户名查找用户实体。（JPA 自动实现）
     */
    Optional<UserEntity> findUserByUsername(String userName);

    /**
     * 通过用户名删除用户实体实体。（JPA 自动实现）
     */
    void deleteUserByUsername(String userName);

    /**
     * 检查指定的用户名字段十否存在。（JPA 自动实现）
     */
    boolean existsByUsername(String userName);

    /**
     * 检查指定的用户全名字段十否存在。（JPA 自动实现）
     */
    boolean existsByFullName(String fullName);
}
