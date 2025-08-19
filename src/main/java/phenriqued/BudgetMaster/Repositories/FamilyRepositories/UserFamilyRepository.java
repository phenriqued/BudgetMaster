package phenriqued.BudgetMaster.Repositories.FamilyRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import phenriqued.BudgetMaster.Models.FamilyEntity.Family;
import phenriqued.BudgetMaster.Models.FamilyEntity.RoleFamily;
import phenriqued.BudgetMaster.Models.FamilyEntity.UserFamily;
import phenriqued.BudgetMaster.Models.UserEntity.User;

import java.util.List;
import java.util.Optional;

public interface UserFamilyRepository extends JpaRepository<UserFamily, Long> {

    Boolean existsByUserAndFamily(User user, Family family);

    Boolean existsByFamilyAndUserAndRoleFamily(Family family, User user, RoleFamily roleFamily);

    List<UserFamily> findAllByFamily(Family family);

    @Query("SELECT families.family FROM UserFamily families WHERE families.user = ?1")
    List<Family> findAllFamilyByUser(User user);

}
