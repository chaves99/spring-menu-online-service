package com.menuonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.menuonline.entity.TokenAccess;

@Repository
public interface TokenAccessRepository extends JpaRepository<TokenAccess, String> {
}
