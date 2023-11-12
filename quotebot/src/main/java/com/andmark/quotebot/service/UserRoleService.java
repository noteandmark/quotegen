package com.andmark.quotebot.service;

import com.andmark.quotebot.domain.enums.UserRole;

public interface UserRoleService {
    boolean hasRequiredRole(Long userId, UserRole requiredRole);
}
