package com.hean.consigueventas.oonabe.rewards.Entity;

import com.hean.consigueventas.oonabe.rewards.Enum.UserPointStatus;
import com.hean.consigueventas.oonabe.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_points")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserPoints {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "available_points", nullable = false)
    private Integer availablePoints;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserPointStatus status;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
