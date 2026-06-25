package com.menuonline.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "customization")
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Customization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String mainColor;

    private String secondaryColor;

    private String font;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private Theme themeType;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    private Boolean active;

    private Boolean builtin;

    public static enum Theme {
        DARK, LIGHT;

        public static Theme from(String name) {
            for (var t : values()) {
                if (t.name().equals(name)) {
                    return t;
                }
            }
            return null;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append("id=").append(id).append(", ");
        sb.append("name=").append(name).append(", ");
        sb.append("mainColor=").append(mainColor).append(", ");
        sb.append("secondaryColor=").append(secondaryColor).append(", ");
        sb.append("font=").append(font).append(", ");
        sb.append("themeType=").append(themeType).append(", ");
        sb.append("active=").append(active).append(", ");
        sb.append("builtin=").append(builtin).append(", ");
        sb.append("user=").append(user.getId());
        sb.append("]");
        return sb.toString();
    }
}
