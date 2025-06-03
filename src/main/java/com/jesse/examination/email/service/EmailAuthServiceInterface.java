package com.jesse.examination.email.service;

import com.jesse.examination.email.entity.EmailAuthTableEntity;

public interface EmailAuthServiceInterface
{
    EmailAuthTableEntity getAuthInfoById(Integer id);
}
