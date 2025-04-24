package com.jesse.examination.user.repository;

import com.jesse.examination.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserEntityRepository extends JpaRepository<UserEntity, Long>
{
    /**
     * 通过用户名查找用户实体。（JPA 自动实现）
     */
    Optional<UserEntity> findUserByUserName(String userName);

    void deleteUserByUserName(String userName);

    boolean existsByFullName(String fullName);
    boolean existsByUserName(String userName);
}
