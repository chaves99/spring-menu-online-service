package com.menuonline.entity;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "subscription")
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Subscription {

    @Id
    private String id;

    private String customerId;

    private String description;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Boolean freeTier;

    @Enumerated(EnumType.STRING)
    private EndReason endReason;

    private LocalDateTime endAt;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",", "[", "]");
        sj.add("id:" + id);
        sj.add("customerId:" + customerId);
        sj.add("description:" + description);
        sj.add("status:" + status);
        sj.add("freeTier:" + freeTier);
        sj.add("endAt:" + endAt);
        sj.add("createdAt:" + createdAt);
        sj.add("updatedAt:" + updatedAt);
        sj.add("user:" + user.getEmail());
        return sj.toString();
    }

    public enum Status {
        ACTIVE,
        UNPAID,
        CANCELED;
    }

    public enum EndReason {
        UNPAID,
        USER_CANCEL;
    }

    /**
     * the method always will return a subs
     * because all user MUST have a subs even
     * that it is a free tier subs
     */
    public static Subscription findCurrent(List<Subscription> subs) {
        if (subs.size() == 1)
            return subs.get(0);

        Optional<Subscription> active = subs.stream()
                .filter(Subscription::isActive)
                .findFirst();
        if (active.isPresent()) {
            return active.get();
        }

        return subs.stream().max(Comparator.comparing(Subscription::getCreatedAt)).get();
    }

    public static boolean isActive(Subscription subscription) {
        return subscription.getStatus().equals(Status.ACTIVE)
                || subscription.getStatus().equals(Status.UNPAID);
    }

}
