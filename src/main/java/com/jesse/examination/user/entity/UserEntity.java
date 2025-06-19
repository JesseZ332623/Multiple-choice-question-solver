package com.jesse.examination.user.entity;

import com.jesse.examination.scorerecord.entity.ScoreRecordEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户数据表实体类。
 */
@Data
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PUBLIC, force = true)
public class UserEntity implements UserDetails
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", unique = true, nullable = false)
    private Long id;                    // 用户 ID（自动生成）

    @Column(name = "user_name", unique = true, nullable = false)
    private String username;            // 用户名

    @Column(name = "password", nullable = false)
    private String password;            // 用户密码

    @Column(name = "full_name", unique = true, nullable = false)
    private String fullName;            // 用户全名

    @Column(name = "telephone_number", nullable = false)
    private String telephoneNumber;     // 手机号

    @Column(name = "email", nullable = false)
    private String email;               // 邮箱

    @Column(name = "register_datetime", nullable = false)
    private LocalDateTime registerDateTime;  // 注册日期

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name               = "user_role_relation",
            joinColumns        = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")

    )
    private Set<RoleEntity> roles;      // 用户的角色（多对多）

    @OneToMany(mappedBy = "userEntity", fetch = FetchType.LAZY)
    private List<ScoreRecordEntity> scoreRecords; // 用户的成绩（一对多）

    @Override
    public String toString()
    {
        return String.format(
                "[ID = %s, User Name: %s, Password: %s, Full Name: %s, " +
                "Telephone Number: %s, Email: %s, Register Time: %s, Roles: %s]",
                id, username, password, fullName, telephoneNumber,
                email, registerDateTime.toString(), roles.toString()
        );
    }

    /**
     * Return the authorities granted to the user. Cannot return <code>null</code>.
     *
     * @return the authorities, sorted by natural key (never <code>null</code>)
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.getRoles()
                   .stream()
                   .map((role) -> new SimpleGrantedAuthority(role.getRoleName()))
                   .collect(Collectors.toList());
    }

    /**
     * Returns the username used to authenticate the user. Cannot return
     * <code>null</code>.
     *
     * @return the username (never <code>null</code>)
     */
    @Override
    public String getUsername() { return this.username; }

    /**
     * Returns the password used to authenticate the user.
     * @return the password
     */
    @Override
    public String getPassword() { return this.password; }

    /**
     * Indicates whether the user's account has expired. An expired account cannot be
     * authenticated.
     *
     * @return <code>true</code> if the user's account is valid (i.e., non-expired),
     * <code>false</code> if no longer valid (i.e., expired)
     */
    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    /**
     * Indicates whether the user is locked or unlocked. A locked user cannot be
     * authenticated.
     *
     * @return <code>true</code> if the user is not locked, <code>false</code> otherwise
     */
    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    /**
     * Indicates whether the user's credentials (password) have expired. Expired
     * credentials prevent authentication.
     *
     * @return <code>true</code> if the user's credentials are valid (i.e., non-expired),
     * <code>false</code> if no longer valid (i.e., expired)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    /**
     * Indicates whether the user is enabled or disabled. A disabled user cannot be
     * authenticated.
     *
     * @return <code>true</code> if the user is enabled, <code>false</code> otherwise
     */
    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
