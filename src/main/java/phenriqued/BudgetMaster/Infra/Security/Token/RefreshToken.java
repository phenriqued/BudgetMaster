package phenriqued.BudgetMaster.Infra.Security.Token;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterSecurityException;
import phenriqued.BudgetMaster.Models.UserEntity.User;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "tb_refresh_token")

@NoArgsConstructor
@Getter
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false, unique = true)
    private String token;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceType deviceType;
    @Column(nullable = false, unique = true)
    private String deviceIdentifier;
    @Setter
    private LocalDateTime expirationToken;
    private LocalDateTime createdAt = LocalDateTime.now();

    public RefreshToken(User user, String token, DeviceType deviceType, String deviceIdentifier, LocalDateTime expirationToken) {
        this.user = user;
        this.token = token;
        this.deviceType = deviceType;
        this.deviceIdentifier = deviceIdentifier;
        this.expirationToken = expirationToken;
    }

    public void attToken(String token, LocalDateTime expirationToken){
        if(Objects.isNull(token) || Objects.isNull(expirationToken))
            throw new BudgetMasterSecurityException("Cannot create a null token");
        this.token = token;
        this.expirationToken = expirationToken;
        this.createdAt = LocalDateTime.now();
    }

}
