package com.hean.consigueventas.oonabe.community.entity;

import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.common.entity.AuditableEntity;
import com.hean.consigueventas.oonabe.community.enums.MatchAvailableDay;
import com.hean.consigueventas.oonabe.community.enums.MatchDescriptor;
import com.hean.consigueventas.oonabe.community.enums.MatchGender;
import com.hean.consigueventas.oonabe.community.enums.MatchLanguage;
import com.hean.consigueventas.oonabe.user.entity.User;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "community_match_requests")
@Getter
@Setter
public class MatchRequest extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "whatsapp", nullable = false, length = 25)
    private String whatsapp;

    @Column(name = "exact_zone", nullable = false, length = 255)
    private String exactZone;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 30)
    private MatchGender gender;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "community_match_request_languages", joinColumns = @JoinColumn(name = "match_request_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false, length = 20)
    private Set<MatchLanguage> languages = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "community_match_request_categories",
            joinColumns = @JoinColumn(name = "match_request_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "community_match_request_available_days", joinColumns = @JoinColumn(name = "match_request_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "available_day", nullable = false, length = 20)
    private Set<MatchAvailableDay> availableDays = new HashSet<>();

    @Column(name = "expectations", nullable = false, columnDefinition = "TEXT")
    private String expectations;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "community_match_request_descriptors", joinColumns = @JoinColumn(name = "match_request_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "descriptor", nullable = false, length = 40)
    private Set<MatchDescriptor> descriptors = new HashSet<>();
}
