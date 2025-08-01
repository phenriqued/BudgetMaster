package phenriqued.BudgetMaster.Models.IncomeEntity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import phenriqued.BudgetMaster.DTOs.Income.RequestNewIncome;
import phenriqued.BudgetMaster.DTOs.Income.RequestUpdateIncome;
import phenriqued.BudgetMaster.Models.UserEntity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.Consumer;

@Entity
@Table(name = "tb_income")

@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class Income {

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
    private LocalDateTime entryDate;

    public Income(@NotNull RequestNewIncome newIncomeDTO, User user){
        this.user = user;
        this.description = newIncomeDTO.description();
        this.amount = new BigDecimal(newIncomeDTO.amount());
        this.entryDate = LocalDateTime.now();
    }

    public void setAmount(String amount){
        this.amount = new BigDecimal(amount);
    }

    public void updateIncome(RequestUpdateIncome updateIncome){
        if (updateIncome != null){
            setIfNotEmpty(updateIncome.description(), this::setDescription);
            setIfNotEmpty(updateIncome.amount(), this::setAmount);
        }
    }

    private void setIfNotEmpty(String value, Consumer<String> setter){
        if(value != null && !value.isEmpty())
            setter.accept(value);
    }
}
