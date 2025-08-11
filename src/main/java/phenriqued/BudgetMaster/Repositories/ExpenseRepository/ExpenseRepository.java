package phenriqued.BudgetMaster.Repositories.ExpenseRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import phenriqued.BudgetMaster.Models.ExpenseEntity.Category.ExpenseCategory;
import phenriqued.BudgetMaster.Models.ExpenseEntity.Expense;
import phenriqued.BudgetMaster.Models.UserEntity.User;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Page<Expense> findAllByUser(Pageable pageable, User user);
    List<Expense> findAllByUser(User user);
    Page<Expense> findAllByUserAndExpenseCategory(Pageable pageable, User user, ExpenseCategory expenseCategory);
    Boolean existsByDescriptionIgnoreCaseAndUser(String description, User user);

}
