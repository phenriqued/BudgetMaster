package phenriqued.BudgetMaster.Models.Security.TwoFactorAuthentication;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import phenriqued.BudgetMaster.Models.UserEntity.User;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "tb_two_factor_auth")

@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class TwoFactorAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Enumerated(EnumType.STRING)
    @Column(name = "type_2FA", nullable = false)
    private Type2FA type2FA;
    @Column(columnDefinition = "VARCHAR(255)", nullable = false, unique = true)
    private String secret;
    private LocalDateTime createdAt;
    @Setter
    private LocalDateTime expirationAt;
    private Boolean isActive;

    public TwoFactorAuth(User user, Type2FA type2FA, String secret, LocalDateTime expirationAt) {
        this.user = user;
        this.type2FA = type2FA;
        this.createdAt = LocalDateTime.now();
        this.secret = secret;
        this.isActive = false;
        this.expirationAt = expirationAt;
    }

    public void setActive() {
        this.isActive = !this.isActive;
    }

    public void setSecret(String secret){
        if (Objects.isNull(secret) || secret.isBlank()) throw new NullPointerException("[INTERNAL ERROR] Secret cannot be null!");
        this.secret = secret;
    }

}
