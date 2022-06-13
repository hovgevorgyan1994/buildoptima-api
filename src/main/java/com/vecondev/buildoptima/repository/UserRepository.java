package com.vecondev.buildoptima.repository;

import com.vecondev.buildoptima.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository
    extends JpaRepository<User, UUID>, PagingAndSortingRepository<User, UUID> {

  boolean existsByEmailIgnoreCase(String email);

  boolean existsByPhone(String phone);

  Optional<User> findByEmail(String email);
}
