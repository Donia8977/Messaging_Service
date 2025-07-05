package security.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 11)
    private String phone;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 50)
    private String city;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    public User(String name, String phone, String email, String city, LocalDateTime createdAt, Tenant tenant) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.city = city;
        this.createdAt = createdAt;
        this.tenant = tenant;
    }

    public User() {

    }
}
