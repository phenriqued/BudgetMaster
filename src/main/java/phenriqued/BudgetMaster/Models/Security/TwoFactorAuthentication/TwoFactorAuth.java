package phenriqued.BudgetMaster.Models.Security.TwoFactorAuthentication;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import phenriqued.BudgetMaster.Models.UserEntity.User;

import java.time.LocalDateTime;
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
    private Type2FA type2FA;
    @Column(columnDefinition = "VARCHAR(255)", nullable = false, unique = true)
    private String secret;
    private LocalDateTime createdAt;


}
