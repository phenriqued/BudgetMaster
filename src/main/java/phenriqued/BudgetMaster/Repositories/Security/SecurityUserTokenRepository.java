package phenriqued.BudgetMaster.Repositories.Security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import phenriqued.BudgetMaster.Models.Security.Token.SecurityUserToken;
import phenriqued.BudgetMaster.Models.Security.Token.TokenType;
import phenriqued.BudgetMaster.Models.UserEntity.User;

import java.util.Optional;
import java.util.UUID;

public interface SecurityUserTokenRepository extends JpaRepository<SecurityUserToken, UUID> {

    Optional<SecurityUserToken> findByIdentifierAndUser(String deviceIdentifier, User user);

    Optional<SecurityUserToken> findByToken(String token);

    void deleteAllByUser(User user);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM SecurityUserToken userToken WHERE userToken.user = ?1 AND userToken.tokenType = ?2")
    void deleteAllByUserAndTokenType(User user, TokenType tokenType);
}
