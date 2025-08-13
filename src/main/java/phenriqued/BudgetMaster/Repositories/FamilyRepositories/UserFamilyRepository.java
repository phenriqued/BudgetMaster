package phenriqued.BudgetMaster.Repositories.FamilyRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import phenriqued.BudgetMaster.Models.FamilyEntity.UserFamily;

public interface UserFamilyRepository extends JpaRepository<UserFamily, Long> {

}
