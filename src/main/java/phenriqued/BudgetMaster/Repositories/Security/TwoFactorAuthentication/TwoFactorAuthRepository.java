package phenriqued.BudgetMaster.Repositories.Security.TwoFactorAuthentication;

import org.springframework.data.jpa.repository.JpaRepository;
import phenriqued.BudgetMaster.Models.Security.TwoFactorAuthentication.TwoFactorAuth;
import phenriqued.BudgetMaster.Models.Security.TwoFactorAuthentication.Type2FA;
import phenriqued.BudgetMaster.Models.UserEntity.User;

import java.util.Optional;
import java.util.UUID;

public interface TwoFactorAuthRepository extends JpaRepository<TwoFactorAuth, UUID> {

    Boolean existsByUserAndType2FA(User user, Type2FA type2FA);

    Optional<TwoFactorAuth> findBySecret(String secret);
}
