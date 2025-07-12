package com.example.MessageService.security.entity;
import java.util.HashSet;
import java.util.Set;
import com.example.MessageService.segment.entity.Segment;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@AllArgsConstructor
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(length = 11)
    private String phone;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 50)
    private String city;

    @Column
    private int age;

    @Column(length = 50)
    private String gender;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType type;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt=LocalDateTime.now();


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;


    public User(Long id) {
        this.id = id;
    }

//    To Stop unidirectional ManyToMany relationship with Segment

//    @ManyToMany(mappedBy = "users", fetch = FetchType.LAZY)
//    @JsonManagedReference("user-segments")
//    private Set<Segment> segments = new HashSet<>();
}
