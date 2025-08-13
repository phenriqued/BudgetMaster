package phenriqued.BudgetMaster.Models.FamilyEntity;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import phenriqued.BudgetMaster.Models.UserEntity.User;

import java.time.LocalDate;

@Entity
@Table(name = "tb_user_family")

@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class UserFamily {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "family_id")
    private Family family;
    @Enumerated(EnumType.STRING)
    private RoleFamily roleFamily;
    private LocalDate joinedAt;

    public UserFamily(User user, Family family, RoleFamily roleFamily) {
        this.user = user;
        this.family = family;
        this.roleFamily = roleFamily;
        this.joinedAt = LocalDate.now();
    }
}
