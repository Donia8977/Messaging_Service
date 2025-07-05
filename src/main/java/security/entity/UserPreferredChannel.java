package security.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_preferred_channels")
public class UserPreferredChannel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", nullable = false)
    private ChannelType channelType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public UserPreferredChannel() {}

    public UserPreferredChannel(User user, ChannelType channelType) {
        this.user = user;
        this.channelType = channelType;
    }
}
