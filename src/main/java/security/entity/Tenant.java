package security.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="tenants")
@Data
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(length = 15)
    private String phone;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Relationships
    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> users = new ArrayList<>();

    // Constructors
    public Tenant() {
        this.createdAt = LocalDateTime.now();  // Auto-set creation date
    }

    public Tenant(String name, String phone, String email) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.createdAt = LocalDateTime.now();
    }




}
