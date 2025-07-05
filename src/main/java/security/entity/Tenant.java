package security.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name="tenants")
@Data
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 15)
    private String phone;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Tenant() { }

    public Tenant(String name, String phone, String email, LocalDateTime createdAt) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.createdAt = createdAt;
    }



}
