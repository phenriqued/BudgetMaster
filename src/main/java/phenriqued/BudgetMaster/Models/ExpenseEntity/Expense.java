package phenriqued.BudgetMaster.Models.ExpenseEntity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import phenriqued.BudgetMaster.DTOs.Expense.RequestCreateExpenseDTO;
import phenriqued.BudgetMaster.DTOs.Expense.RequestUpdateExpenseDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Models.ExpenseEntity.Category.ExpenseCategory;
import phenriqued.BudgetMaster.Models.UserEntity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.Consumer;

@Entity
@Table(name = "tb_expense")

@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
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

    public void setAmount(String amount) {
        if(amount != null && !amount.trim().isBlank()){
            if(!amount.matches("^\\d+(\\.\\d{1,2})?$")){
                throw new BusinessRuleException("the value must correspond to one or more positive numeric digits followed by a period and finally up to two numeric digits");
            }
        }
        this.amount = new BigDecimal(amount);
    }
    public void setExpenseCategory(ExpenseCategory expenseCategory){
        if(expenseCategory == null) throw new NullPointerException("Expensive Category cannot be null");
        this.expenseCategory = expenseCategory;
    }
    public void update(RequestUpdateExpenseDTO updateExpenseDTO) {
        if (updateExpenseDTO != null){
            setIfNotEmpty(updateExpenseDTO.description(), this::setDescription);
            setIfNotEmpty(updateExpenseDTO.amount(), this::setAmount);
        }
    }
    private void setIfNotEmpty(String value, Consumer<String> setter){
        if(value != null && !value.isEmpty())
            setter.accept(value);
    }
}
