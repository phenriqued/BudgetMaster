package phenriqued.BudgetMaster.Repositories.IncomeRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import phenriqued.BudgetMaster.Models.IncomeEntity.Income;
import phenriqued.BudgetMaster.Models.UserEntity.User;

import java.util.Optional;

public interface IncomeRepository extends JpaRepository<Income, Long> {

    Boolean existsByDescriptionAndUser(String description, User user);

    Page<Income> findAllByUser(Pageable pageable, User user);

    @Query("SELECT income FROM Income income WHERE income.description = ?1 AND income.user = ?2")
    Optional<Income> findByDescriptionAndUser(String description, User user);
}
