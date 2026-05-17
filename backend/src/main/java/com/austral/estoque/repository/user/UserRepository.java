package com.austral.estoque.repository.user;

import com.austral.estoque.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE (u.username = :login OR u.email = :email) AND u.deletedAt IS NULL")
    Optional<User> findByUsernameOrEmail(String login, String email);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
