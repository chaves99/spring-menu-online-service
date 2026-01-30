package com.menuonline.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.menuonline.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    public List<Category> findByUserId(Long id);

    @Query(value = "update categories set enabled = 'false' where user_id = :userId and id = :id",
        nativeQuery = true)
    public void disable(Long userId, Long id);

    public Optional<Category> findByUserIdAndId(long userId, long id);
}
