package phenriqued.BudgetMaster.Models.UserEntity;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import phenriqued.BudgetMaster.DTOs.Login.RegisterUserDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Infra.Security.Token.SecurityUserToken;
import phenriqued.BudgetMaster.Models.FamilyEntity.UserFamily;
import phenriqued.BudgetMaster.Models.Security.TwoFactorAuthentication.TwoFactorAuth;
import phenriqued.BudgetMaster.Models.Security.TwoFactorAuthentication.Type2FA;
import phenriqued.BudgetMaster.Models.UserEntity.Role.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    @OneToMany(mappedBy = "user")
    private List<UserFamily> family = new ArrayList<>();
    private Boolean isActive;
    private LocalDate createdAt;
    private LocalDateTime deleteAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SecurityUserToken> securityUserTokens = new ArrayList<>();
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TwoFactorAuth> twoFactorAuths = new ArrayList<>();

    public User(RegisterUserDTO userDTO, String password, Role role){
        this.name = userDTO.name();
        this.email = userDTO.email();
        this.password = password;
        this.role = role;
        this.isActive = false;
        this.createdAt = LocalDate.now();
        this.deleteAt = null;
    }

    public void setIsActive(){
        this.isActive =! isActive;
    }

    public void setPassword(String password) {
        if(Objects.isNull(password) || password.trim().isEmpty()) throw new BusinessRuleException("Password cannot be null or empty");
        this.password = password;
    }

    public void setDeleteAt(){
        if (isActive){
            this.deleteAt = null;
        }else{
            this.deleteAt = LocalDateTime.now().plusMinutes(4320);
        }
    }

    public Boolean isTwoFactorAuthEnabled(){
        if(twoFactorAuths == null || twoFactorAuths.isEmpty()) return false;

        return twoFactorAuths.stream().anyMatch(TwoFactorAuth::getIsActive);
    }
    public TwoFactorAuth priorityOrderTwoFactorAuth(){
        if (!isTwoFactorAuthEnabled()) throw new BusinessRuleException("there is no active two-factor authentication");
        var twoFactorAuthIsActive = twoFactorAuths.stream().filter(TwoFactorAuth::getIsActive).toList();

        return twoFactorAuthIsActive.stream().filter(twoFactorAuth -> twoFactorAuth.getType2FA().equals(Type2FA.AUTHENTICATOR)).findFirst()
                .orElse(twoFactorAuthIsActive.stream().filter(twoFactorAuth -> twoFactorAuth.getType2FA().equals(Type2FA.EMAIL)).findFirst()
                        .orElseThrow(() -> new BusinessRuleException("[INTERNAL ERROR]: there is no active two-factor authentication")));

    }

}
