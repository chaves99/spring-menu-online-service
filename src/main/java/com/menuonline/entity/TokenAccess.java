package com.menuonline.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "token_access")
@Getter
@Setter
@EqualsAndHashCode
@ToString
@EntityListeners(AuditingEntityListener.class)
public class TokenAccess {

    public static final int TOKEN_DURATION_HOURS = 24;

    public static final int TOKEN_DURATION_RECOVERY_PASSWORD_MIN = 5;

    @Id
    private String token;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime expirationDate;

    public static TokenAccess create(UserEntity user, LocalDateTime expiration) {
        TokenAccess token = new TokenAccess();
        token.setUser(user);
        String uuidToken = UUID.randomUUID().toString();
        token.setToken(uuidToken);
        token.setExpirationDate(expiration);
        return token;
    }
}
