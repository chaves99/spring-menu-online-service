package com.menuonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.menuonline.entity.Price;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {

    void deleteByProductId(Long id);

}
