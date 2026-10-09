package com.kruzetech.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String id;

    @Column(nullable = false, unique = true)
    private String email;

    /** null với user đăng nhập bằng Firebase. */
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    private String avatar;

    @Column(name = "firebase_uid", unique = true)
    private String firebaseUid;

    @Column(name = "last_login")
    private Instant lastLogin;

    // ---- Gamification / Leaderboard ----

    @Column(name = "total_exp", nullable = false)
    private int totalExp = 0;

    @Column(name = "current_exp", nullable = false)
    private int currentExp = 0;

    @Column(name = "level", nullable = false)
    private int level = 1;

    @Column(name = "streak", nullable = false)
    private int streak = 0;

    @Column(name = "words_mastered", nullable = false)
    private int wordsMastered = 0;

    @Column(name = "total_reviews", nullable = false)
    private int totalReviews = 0;

    @Column(name = "last_study_date")
    private Instant lastStudyDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Danh hiệu hiển thị, đồng bộ công thức với app Flutter (UserProgression.rankTitle). */
    public String rankTitle() {
        if (level <= 1) return "Tập Sự (Novice)";
        if (level == 2) return "Khám Phá (Explorer)";
        if (level == 3) return "Tinh Anh (Apprentice)";
        if (level == 4) return "Chuyên Cần (Scholar)";
        if (level == 5) return "Học Giả (Expert)";
        return "Bậc Thầy (Master)";
    }

    public String displayName() {
        String full = ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
        if (!full.isEmpty()) return full;
        return email != null ? email : "Học viên";
    }
}

