package phenriqued.BudgetMaster.Repositories.SecurityData;

import org.springframework.data.jpa.repository.JpaRepository;
import phenriqued.BudgetMaster.Infra.Security.Token.SecurityUserToken;
import phenriqued.BudgetMaster.Infra.Security.Token.TokenType;
import phenriqued.BudgetMaster.Models.UserEntity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SecurityUserTokenRepository extends JpaRepository<SecurityUserToken, UUID> {

    Optional<SecurityUserToken> findByIdentifierAndUser(String deviceIdentifier, User user);

    Optional<SecurityUserToken> findByToken(String token);
    Optional<List<SecurityUserToken>> findAllByUser(User user);

    void deleteAllByUser(User user);
    void deleteAllByUserAndTokenType(User user, TokenType tokenType);
}
