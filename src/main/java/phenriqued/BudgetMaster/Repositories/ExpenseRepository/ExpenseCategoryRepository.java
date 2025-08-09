package phenriqued.BudgetMaster.Repositories.ExpenseRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import phenriqued.BudgetMaster.Models.ExpenseEntity.Category.ExpenseCategory;
import phenriqued.BudgetMaster.Models.UserEntity.User;

import java.util.List;
import java.util.Optional;

public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {

    @Query("SELECT c FROM ExpenseCategory c WHERE c.user IS NULL OR c.user = ?1")
    List<ExpenseCategory> findAllByUser(User user);

    @Query("SELECT c FROM ExpenseCategory c WHERE c.name = ?1 AND (c.user IS NULL OR c.user = ?2)")
    Optional<ExpenseCategory> findByNameAndUserOrPublic(String name, User user);

    @Query("SELECT c FROM ExpenseCategory c WHERE c.id = ?1 AND c.user IS NOT NULL ")
    Optional<ExpenseCategory> findByIdAndUserNotNull(Long id);

    Boolean existsByNameAndUser(String name, User user);

}
