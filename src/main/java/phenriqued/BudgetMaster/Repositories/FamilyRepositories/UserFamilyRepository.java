package phenriqued.BudgetMaster.Repositories.FamilyRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import phenriqued.BudgetMaster.Models.FamilyEntity.Family;
import phenriqued.BudgetMaster.Models.FamilyEntity.UserFamily;
import phenriqued.BudgetMaster.Models.UserEntity.User;

public interface UserFamilyRepository extends JpaRepository<UserFamily, Long> {

    Boolean existsByUserAndFamily(User user, Family family);

}
