package com.jesse.examination.email.service.impl;

import com.jesse.examination.email.entity.EmailAuthTableEntity;
import com.jesse.examination.email.repo.EmailAuthTableRepo;
import com.jesse.examination.email.service.EmailAuthServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailAuthService implements EmailAuthServiceInterface
{
    private final EmailAuthTableRepo emailAuthTableRepo;

    @Autowired
    public EmailAuthService(EmailAuthTableRepo emailAuthTableRepo) {
        this.emailAuthTableRepo = emailAuthTableRepo;
    }

    @Override
    public EmailAuthTableEntity getAuthInfoById(Integer id) {
        return this.emailAuthTableRepo.findById(id).orElseThrow();
    }
}
