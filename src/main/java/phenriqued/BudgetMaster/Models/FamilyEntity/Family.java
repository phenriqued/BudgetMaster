package phenriqued.BudgetMaster.Models.FamilyEntity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_family")

@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class Family {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @OneToMany(mappedBy = "family")
    private List<UserFamily> userFamilies = new ArrayList<>();

}
