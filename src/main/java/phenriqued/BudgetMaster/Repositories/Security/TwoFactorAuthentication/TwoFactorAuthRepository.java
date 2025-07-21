package phenriqued.BudgetMaster.Repositories.Security.TwoFactorAuthentication;

import org.springframework.data.jpa.repository.JpaRepository;
import phenriqued.BudgetMaster.Models.Security.TwoFactorAuthentication.TwoFactorAuth;

import java.util.UUID;

public interface TwoFactorAuthRepository extends JpaRepository<TwoFactorAuth, UUID> {

}
