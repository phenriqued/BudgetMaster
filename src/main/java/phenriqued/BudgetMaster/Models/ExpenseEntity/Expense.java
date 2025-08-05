package phenriqued.BudgetMaster.Models.ExpenseEntity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import phenriqued.BudgetMaster.DTOs.Expense.RequestCreateExpenseDTO;
import phenriqued.BudgetMaster.Models.ExpenseEntity.Category.ExpenseCategory;
import phenriqued.BudgetMaster.Models.UserEntity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_expense")

@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Setter
    @Column(columnDefinition = "VARCHAR (255)", nullable = false)
    private String description;
    @Column(precision = 15, scale =2, nullable = false)
    private BigDecimal amount;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "expense_category_id", nullable = false)
    private ExpenseCategory expenseCategory;
    private LocalDateTime entryDate;

    public Expense(RequestCreateExpenseDTO expenseDTO, ExpenseCategory expenseCategory, User user) {
        this.user = user;
        this.description = expenseDTO.description();
        this.amount = new BigDecimal(expenseDTO.amount());
        this.expenseCategory = expenseCategory;
        this.entryDate = LocalDateTime.now();
    }
}
