package com.menuonline.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "users")
@Getter
@Setter
@EqualsAndHashCode
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

    @Column(name = "establishment_name")
    private String establishmentName;

    private String instagram;

    private String facebook;

    private String website;

    private String phone;

    private String whatsapp;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private List<TokenAccess> tokenAccesses = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Schedule> schedules;

    private String image;

    private String code;

    private String city;

    private String addressLine;

    @Column(name = "created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Override
    public String toString() {
        return "UserEntity[" + id + " - " + email + "]";
    }
    
}
