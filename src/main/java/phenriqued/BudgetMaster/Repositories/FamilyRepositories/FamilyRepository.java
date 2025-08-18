package phenriqued.BudgetMaster.Repositories.FamilyRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import phenriqued.BudgetMaster.Models.FamilyEntity.Family;

public interface FamilyRepository extends JpaRepository<Family, Long> {
}
