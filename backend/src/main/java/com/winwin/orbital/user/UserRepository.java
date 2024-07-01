package com.winwin.orbital.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<com.winwin.orbital.user.User, Long> {

    Optional<com.winwin.orbital.user.User> findByUsername(String username);

    Optional<User> findByEmail(String email);

}
