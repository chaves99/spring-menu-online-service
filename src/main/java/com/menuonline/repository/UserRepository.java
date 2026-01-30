package com.menuonline.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.menuonline.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByEstablishmentName(String establishmentName);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        update users set
        code = :code,
        address_line = :line,
        city = :city
        where id = :id;
    """)
    void updateAddress(Long id, String code, String line, String city);
}
