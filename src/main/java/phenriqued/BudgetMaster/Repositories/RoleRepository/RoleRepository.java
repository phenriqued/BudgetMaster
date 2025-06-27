package phenriqued.BudgetMaster.Repositories.RoleRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import phenriqued.BudgetMaster.Models.UserEntity.Role.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
