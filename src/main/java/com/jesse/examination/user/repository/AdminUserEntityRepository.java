package com.jesse.examination.user.repository;

import com.jesse.examination.user.entity.UserEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AdminUserEntityRepository extends JpaRepository<UserEntity, Long>
{
    /**
     * 通过用户名查找用户实体。（JPA 自动实现）
     */
    Optional<UserEntity> findUserByUsername(String userName);

    /**
     * 通过实体图 (Entity Graph) 用一句 SQL 就能查询出所有的用户信息，
     * 避免频繁的数据库操作（N + 1 问题）。
     * 这里还要使用 DISTINCT 确保在用户过多的情况下避免笛卡尔积爆炸，
     * 未来随着用户数量的增涨还会添加分页查询策略。
     */
    @NotNull
    @Query(value = "SELECT DISTINCT u FROM UserEntity u")
    @EntityGraph(attributePaths = "roles")
    List<UserEntity> findAll();

    /**
     * 查询在 [startId, endId] 范围内，
     * 实际存在多少个数据，将数据行 id 返回并存入一个列表中。
     */
    @Query(value = """
                    SELECT users.user_id FROM users
                    WHERE users.user_id
                    BETWEEN :startId AND :endId
                    """,
            nativeQuery = true
    )
    List<Long> findIdByIdBetween(
            @Param(value = "startId") Long startId,
            @Param(value = "endId")   Long endId
    );

    /**
     * 查询当前表中的所有用户 id。
     */
    @Query(
            value = "SELECT users.user_id FROM users",
            nativeQuery = true
    )
    List<Long> findAllUserId();

    /**
     * 查询当前表中的所有用户名。
     */
    @Query(
            value = "SELECT users.user_name FROM users",
            nativeQuery = true
    )
    List<String> findAllUserName();

    /**
     * 删除通过用户名删除用户实体。（JPA 自动实现）
     */
    void deleteUserByUsername(String userName);

    /**
     * 按 ids 列表的指示删除 user_role_relation 从表对应的数据行。
     */
    @Modifying
    @Query(
            value = """
                    DELETE FROM user_role_relation
                    WHERE user_role_relation.user_id IN :ids
                    """,
            nativeQuery = true
    )
    void deleteUserRoleRelationsByIds(
            @Param(value = "ids")
            List<Long> ids
    );

    /**
     * 按 ids 列表的指示删除 users 主表对应的数据行。
     */
    @Modifying
    @Query(
            value = "DELETE FROM users WHERE users.user_id IN :ids",
            nativeQuery = true
    )
    void deleteUserByIds(
            @Param(value = "ids")
            List<Long> ids
    );

    /**
     * 将 users 表的 id 自增重置为 1。
     */
    @Modifying
    @Query(
            value = "ALTER TABLE users AUTO_INCREMENT = 1",
            nativeQuery = true
    )
    void alterAutoIncrementToOne();

    /**
     * 通过 full name 验证所对应的数据行是否存在（JPA 自动实现）。
     */
    boolean existsByFullName(String fullName);

    /**
     * 通过 user name 验证所对应的数据行是否存在（JPA 自动实现）。
     */
    boolean existsByUsername(String userName);
}