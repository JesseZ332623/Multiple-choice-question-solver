
package com.jesse.examination.email.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "email_auth_table")
public class EmailAuthTableEntity
{
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(
            name = "email",
            columnDefinition = "VARCHAR(64)"
    )
    private String  email;

    @Column(
            name = "email_auth_code",
            columnDefinition = "VARCHAR(64)"
    )
    private String  emailAuthCode;
}
