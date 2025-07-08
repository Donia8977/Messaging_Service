package com.example.MessageService.segment.entity;

import jakarta.persistence.*;
import lombok.*;
import com.example.MessageService.security.entity.Tenant;
import com.example.MessageService.security.entity.User;
import java.util.HashSet;
import java.util.Set;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "segments",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "tenant_id"})
        }
)
@ToString(exclude = {"tenant"})
@EqualsAndHashCode(of = {"id", "tenant"}) //equality based on id and tenant

public class Segment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Lob
    @Column(name = "rules_json", nullable = false, columnDefinition = "TEXT")
    private String rulesJson;


    // Many-to-Many relationship with User
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "segment_users",
            joinColumns = @JoinColumn(name = "segment_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> users = new HashSet<>();


    //Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;
}