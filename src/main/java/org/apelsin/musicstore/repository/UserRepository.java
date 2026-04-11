package org.apelsin.musicstore.repository;

import org.apelsin.musicstore.model.User;
import org.apelsin.musicstore.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = "SELECT * FROM users WHERE user_username = :username", nativeQuery = true)
    Optional<User> findByUserUsername(@Param("username") String username);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM users WHERE user_username = :username)", nativeQuery = true)
    boolean existsByUserUsername(@Param("username") String username);

    @Query(value = "SELECT * FROM users WHERE user_role = :role", nativeQuery = true)
    List<User> findByUserRole(@Param("role") Role role);

    @Query(value = "SELECT * FROM users", nativeQuery = true)
    List<User> findAllUsers();

    @Query(value = "SELECT * FROM users WHERE user_id = :userId", nativeQuery = true)
    Optional<User> findByUserId(@Param("userId") Long userId);
}
