package com.menuonline.repository;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.menuonline.entity.Subscription;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, String> {

    /**
     * The method do NOT return a optional because 
     * all the users SHOULD HAVE a subscription linked 
     * with it.
     * @param userId
     * @return the latest subscription created
     */
    @Query(value = "select * from subscription where user_id = :userId order by created_at desc limit 1", nativeQuery = true)
    Subscription findCurrent(Long userId);

    Optional<Subscription> findByIdAndCustomerId(String id, String customerId);

    Optional<Subscription> findByIdAndUserId(Long id, Long userId);
}
