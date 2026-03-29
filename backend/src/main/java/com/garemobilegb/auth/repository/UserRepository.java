package com.garemobilegb.auth.repository;

import com.garemobilegb.auth.domain.Role;
import com.garemobilegb.auth.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByPhoneNumber(String phoneNumber);

  boolean existsByPhoneNumber(String phoneNumber);

  List<User> findByRole(Role role);
}
