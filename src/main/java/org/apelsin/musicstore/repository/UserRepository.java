package org.apelsin.musicstore.repository;

import org.apelsin.musicstore.model.User;
import org.apelsin.musicstore.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserUsername(String username);

    boolean existsByUserUsername(String username);

    List<User> findByUserRole(Role role);
}