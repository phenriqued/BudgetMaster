package phenriqued.BudgetMaster.Infra.Security.Token;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterSecurityException;
import phenriqued.BudgetMaster.Models.UserEntity.User;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "tb_security_user_token")

@NoArgsConstructor
@Getter
public class SecurityUserToken {

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
    private TokenType tokenType;
    @Column(nullable = false, unique = true)
    private String identifier;
    @Setter
    private LocalDateTime expirationToken;
    private LocalDateTime createdAt = LocalDateTime.now();

    public SecurityUserToken(User user, TokenType tokenType, String identifier, LocalDateTime expirationToken) {
        this.user = user;
        this.token = generatedToken(tokenType);
        this.tokenType = tokenType;
        this.identifier = identifier;
        this.expirationToken = expirationToken;
    }

    public void attToken(LocalDateTime expirationToken){
        if(Objects.isNull(tokenType) || Objects.isNull(expirationToken))
            throw new BudgetMasterSecurityException("Cannot create a token");
        this.token = generatedToken(tokenType);
        this.expirationToken = expirationToken;
        this.createdAt = LocalDateTime.now();
    }

    private String generatedToken(TokenType tokenType){
        return tokenType.equals(TokenType.USER_ACTIVATION) ? generatedSecureRandomToken(32) :
                UUID.randomUUID().toString();
    }

    private String generatedSecureRandomToken(int byteLength) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] token = new byte[byteLength];
        secureRandom.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }

}
