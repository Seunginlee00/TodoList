package com.my.boot.user.repository;

import com.my.boot.user.entity.UserRole;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    @Query("select ur.roleList from UserRole ur where ur.user.loginId = :loginId")
    List<String> findRoleCsvByUserId(@Param("loginId") String loginId);
}
