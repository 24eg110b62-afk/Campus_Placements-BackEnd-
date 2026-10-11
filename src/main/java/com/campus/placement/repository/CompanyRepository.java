package com.campus.placement.repository;

import com.campus.placement.entity.Company;
import com.campus.placement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByUser(User user);
}
