package com.menuonline.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.menuonline.entity.Customization;

@Repository
public interface CustomizationRepository extends JpaRepository<Customization, Long> {

    List<Customization> findByUserId(Long userId);

    // It should always return something
    // TODO for it work it needs to save the default themes
    @Query(nativeQuery = true, value = "select * from customization where active = true and user_id = :userId")
    Customization findActive(Long userId);

    List<Customization> findAllByUserId(Long userId);
}
