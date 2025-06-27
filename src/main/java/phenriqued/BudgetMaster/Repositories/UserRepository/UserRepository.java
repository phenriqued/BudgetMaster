package phenriqued.BudgetMaster.Repositories.UserRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import phenriqued.BudgetMaster.Models.UserEntity.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
