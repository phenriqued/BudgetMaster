package phenriqued.BudgetMaster.Models.ExpenseEntity.Category;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import phenriqued.BudgetMaster.Models.UserEntity.User;

@Entity
@Table(name = "tb_expense_category")

@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class ExpenseCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Enumerated(EnumType.STRING)
    private SpendingPriority spendingPriority;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public ExpenseCategory(String name, SpendingPriority spendingPriority, User user) {
        this.name = name;
        this.spendingPriority = spendingPriority;
        this.user = user;
    }
}
