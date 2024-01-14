package com.andmark.quotegen.repository;

import com.andmark.quotegen.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByUsertgId(Long usertgId);
    boolean existsByUsertgId(Long usertgId);
    boolean existsByUsername(String username);

    @Modifying
    @Query("UPDATE User u SET u.nickname = :newNickname WHERE u.username = :username")
    void updateNickname(@Param("username") String username, @Param("newNickname") String newNickname);

    void deleteByUsertgId(Long usertgId);
}
