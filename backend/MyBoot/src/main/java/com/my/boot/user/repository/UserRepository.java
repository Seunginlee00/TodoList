package com.my.boot.user.repository;

import com.my.boot.user.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLoginId(String loginId);
    Boolean existsByLoginId(String loginId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roleList WHERE u.loginId = :loginId")
    User getWithRoles(@Param("loginId") String loginId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roleList WHERE u.loginId = :loginId")
    Optional<User> selectUserWithRoles(@Param("userId") String loginId);
}
