package phenriqued.BudgetMaster.Models.UserEntity;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import phenriqued.BudgetMaster.DTOs.Login.RegisterUserDTO;
import phenriqued.BudgetMaster.Models.FamilyEntity.UserFamily;
import phenriqued.BudgetMaster.Models.UserEntity.Role.Role;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_user")

@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Setter
    private String email;
    private String password;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    @Setter
    private Boolean isActive;
    @OneToMany(mappedBy = "user")
    private List<UserFamily> family = new ArrayList<>();
    private LocalDate createdAt;

    public User(RegisterUserDTO userDTO, String password, Role role){
        this.name = userDTO.name();
        this.email = userDTO.email();
        this.password = password;
        this.role = role;
        this.isActive = true;
        this.createdAt = LocalDate.now();
    }

}
