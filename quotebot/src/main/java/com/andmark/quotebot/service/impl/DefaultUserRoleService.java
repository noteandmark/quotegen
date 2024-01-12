package com.andmark.quotebot.service.impl;

import com.andmark.quotebot.domain.enums.UserRole;
import com.andmark.quotebot.service.ApiService;
import com.andmark.quotebot.service.UserRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DefaultUserRoleService implements UserRoleService {

    private final ApiService apiService;

    public DefaultUserRoleService(ApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public boolean hasRequiredRole(Long userId, UserRole neededRole) {

        UserRole userRole = apiService.getUserRole(userId);
        log.debug("user role = {} for user.getId() = {}", userRole, userId);

        boolean isUser = userRole.equals(UserRole.ROLE_USER);
        boolean isAdmin = userRole.equals(UserRole.ROLE_ADMIN);

        return (neededRole.equals(UserRole.ROLE_ADMIN) && isAdmin) ||
                (neededRole.equals(UserRole.ROLE_USER) && (isUser || isAdmin));
    }

}
