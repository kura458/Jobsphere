package com.jobsphere.jobsite.model.shared;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "addresses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String country;
    @Column(columnDefinition = "TEXT")
    private String region;
    @Column(columnDefinition = "TEXT")
    private String city;
    @Column(name = "sub_city", columnDefinition = "TEXT")
    private String subCity;
    @Column(columnDefinition = "TEXT")
    private String street;
    @Column(name = "created_at")
    private Instant createdAt;
}