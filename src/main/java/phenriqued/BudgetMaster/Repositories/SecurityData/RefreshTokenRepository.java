package phenriqued.BudgetMaster.Repositories.SecurityData;

import org.springframework.data.jpa.repository.JpaRepository;
import phenriqued.BudgetMaster.Infra.Security.Token.RefreshToken;
import phenriqued.BudgetMaster.Models.UserEntity.User;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByDeviceIdentifierAndUser(String deviceIdentifier, User user);

    Optional<RefreshToken> findByToken(String code);
}
