package phenriqued.BudgetMaster.Controllers.IncomeControllers;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import phenriqued.BudgetMaster.DTOs.Income.RequestNewIncome;
import phenriqued.BudgetMaster.DTOs.Income.RequestUpdateIncome;
import phenriqued.BudgetMaster.DTOs.Income.ResponseAllIncomesDTO;
import phenriqued.BudgetMaster.DTOs.Income.ResponseIncomesDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Services.IncomeService.IncomeService;

import java.net.URI;

@RestController
@RequestMapping("/income")
public class IncomeController {

    private final IncomeService incomeService;

    public IncomeController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    @GetMapping
    public ResponseEntity<ResponseAllIncomesDTO> listAllIncome(@PageableDefault(size = 5 ) Pageable pageable,
                                                                     @AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseEntity.ok(incomeService.listAllIncomes(pageable, userDetails));
    }

    @PostMapping
    public ResponseEntity<RequestNewIncome> createIncome(@RequestBody @Valid RequestNewIncome requestIncomeDTO, @AuthenticationPrincipal UserDetailsImpl userDetails){
        var data = incomeService.createIncome(requestIncomeDTO, userDetails);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("{id}").buildAndExpand(data.getId()).toUri();
        return ResponseEntity.created(uri).body(new RequestNewIncome(data));
    }

    @GetMapping("{id}")
    public ResponseEntity<ResponseIncomesDTO> getIncomeById(@PathVariable("id") Long id, @AuthenticationPrincipal UserDetailsImpl userDetails){
        try {
            return ResponseEntity.ok(incomeService.getIncomeById(id, userDetails));
        }catch (BusinessRuleException e){
            return ResponseEntity.notFound().build();
        }

    }
    @GetMapping("/search")
    public ResponseEntity<ResponseIncomesDTO> getIncomeByDescription(@RequestParam(value = "description") String description, @AuthenticationPrincipal UserDetailsImpl userDetails){
        try {
            return ResponseEntity.ok(incomeService.getIncomeByDescription(description, userDetails));
        }catch (BusinessRuleException e){
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping
    public ResponseEntity<Void> updateIncome(@RequestParam(value = "id") Long id, @RequestBody @Valid RequestUpdateIncome requestIncomeDTO,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails){
        incomeService.updateIncome(id, requestIncomeDTO, userDetails);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteIncome(@PathVariable("id") Long id,  @AuthenticationPrincipal UserDetailsImpl userDetails){
        incomeService.deleteIncome(id, userDetails);
        return ResponseEntity.noContent().build();
    }
}
