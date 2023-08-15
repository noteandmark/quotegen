package com.andmark.quotegen.repository;

import com.andmark.quotegen.domain.User;
import com.andmark.quotegen.domain.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByUsertgId(Long usertgId);
    boolean existsByUsertgId(Long usertgId);
    boolean existsByUsername(String username);
}
