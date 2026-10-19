package com.menuonline.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.menuonline.entity.Tags;
import com.menuonline.entity.UserEntity;

@Repository
public interface TagsRepository extends JpaRepository<Tags, Long> {

    public List<Tags> findByUser(UserEntity user);

    public void deleteByIdAndUser(Long id, UserEntity user);

    public List<Tags> findByProducts_Id(Long id);
}
