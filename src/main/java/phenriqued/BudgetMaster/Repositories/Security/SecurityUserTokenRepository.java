package phenriqued.BudgetMaster.Repositories.Security;

import org.springframework.data.jpa.repository.JpaRepository;
import phenriqued.BudgetMaster.Models.Security.Token.SecurityUserToken;
import phenriqued.BudgetMaster.Models.UserEntity.User;

import java.util.Optional;
import java.util.UUID;

public interface SecurityUserTokenRepository extends JpaRepository<SecurityUserToken, UUID> {

    Optional<SecurityUserToken> findByIdentifierAndUser(String deviceIdentifier, User user);

    Optional<SecurityUserToken> findByToken(String token);

}
