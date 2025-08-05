package phenriqued.BudgetMaster.Controllers.ExpenseControllers;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import phenriqued.BudgetMaster.DTOs.Expense.RequestCreateExpenseDTO;
import phenriqued.BudgetMaster.DTOs.Expense.ResponseAllExpenseDTO;
import phenriqued.BudgetMaster.DTOs.Expense.ResponseExpenseDTO;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Services.ExpenseService.ExpenseService;

import java.net.URI;

@RestController
@RequestMapping("/expense")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public ResponseEntity<ResponseAllExpenseDTO> listAllExpense(@PageableDefault(size = 5) Pageable pageable, @AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseEntity.ok(expenseService.getAllExpense(pageable, userDetails));
    }
    @PostMapping
    public ResponseEntity<ResponseExpenseDTO> createExpense(@RequestBody RequestCreateExpenseDTO createExpenseDTO, @AuthenticationPrincipal UserDetailsImpl userDetails){
        var data = expenseService.createExpense(createExpenseDTO, userDetails);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("{id}").buildAndExpand(data.getId()).toUri();
        return ResponseEntity.created(uri).body(new ResponseExpenseDTO(data));
    }


}
