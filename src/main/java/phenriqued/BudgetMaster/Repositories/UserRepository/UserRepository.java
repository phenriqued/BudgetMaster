package phenriqued.BudgetMaster.Repositories.UserRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import phenriqued.BudgetMaster.Models.UserEntity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

}
