package com.jesse.examination.email.repo;

import com.jesse.examination.email.entity.EmailAuthTableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailAuthTableRepo
        extends JpaRepository<EmailAuthTableEntity, Integer> {}
