package phenriqued.BudgetMaster.Models.UserEntity;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import phenriqued.BudgetMaster.DTOs.Login.RegisterUserDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterSecurityException;
import phenriqued.BudgetMaster.Infra.Security.Token.DeviceType;
import phenriqued.BudgetMaster.Infra.Security.Token.RefreshToken;
import phenriqued.BudgetMaster.Models.FamilyEntity.UserFamily;
import phenriqued.BudgetMaster.Models.UserEntity.Role.Role;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    public User(RegisterUserDTO userDTO, String password, Role role){
        this.name = userDTO.name();
        this.email = userDTO.email();
        this.password = password;
        this.role = role;
        this.isActive = false;
        this.createdAt = LocalDate.now();
    }

    public String getRefreshToken(DeviceType deviceType){
        return refreshTokens.stream().filter(refresh -> refresh.getDeviceType().equals(deviceType)).findFirst()
                .orElseThrow(() -> new BudgetMasterSecurityException("Token for device does not exist")).getToken();
    }

    public void setIsActive(){
        this.isActive =! isActive;
    }

}
