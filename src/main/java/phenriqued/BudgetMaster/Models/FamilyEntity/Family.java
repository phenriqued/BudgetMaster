package phenriqued.BudgetMaster.Models.FamilyEntity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import phenriqued.BudgetMaster.DTOs.Family.RequestCreateFamilyDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "tb_family")

@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class Family {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Setter
    private String name;
    @OneToMany(mappedBy = "family", orphanRemoval = true, fetch = FetchType.EAGER)
    private List<UserFamily> userFamilies = new ArrayList<>();

    public Family(RequestCreateFamilyDTO familyDTO) {
        this.name = familyDTO.name();
    }

    public void addUserFamily(UserFamily userFamily){
        if(Objects.isNull(userFamily)) throw new NullPointerException("[INTERNAL ERROR] User cannot be null");
        userFamilies.add(userFamily);
    }

}
