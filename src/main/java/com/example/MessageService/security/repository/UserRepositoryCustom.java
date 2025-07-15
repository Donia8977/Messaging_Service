package com.example.MessageService.security.repository;

import com.example.MessageService.security.entity.User;

import java.util.List;
import java.util.Map;

public interface UserRepositoryCustom {

    List<User> findUsersByCriteria(Map<String, Object> criteria, Long tenantId);
    List<User> findUsersByAnyCriteria(Map<String, Object> criteria, Long tenantId);
}
