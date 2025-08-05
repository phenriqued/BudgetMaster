package phenriqued.BudgetMaster.Repositories.ExpenseRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import phenriqued.BudgetMaster.Models.ExpenseEntity.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

}
