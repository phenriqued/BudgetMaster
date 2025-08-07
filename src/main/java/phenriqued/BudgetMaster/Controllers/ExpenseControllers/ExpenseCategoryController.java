package phenriqued.BudgetMaster.Controllers.ExpenseControllers;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import phenriqued.BudgetMaster.DTOs.Expense.RequestCreateExpenseCategoryDTO;
import phenriqued.BudgetMaster.DTOs.Expense.RequestUpdateExpenseCategoryDTO;
import phenriqued.BudgetMaster.DTOs.Expense.ResponseCategoryDTO;
import phenriqued.BudgetMaster.DTOs.Expense.ResponseSpendingPriorityDTO;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Services.ExpenseService.ExpenseCategoryService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/expense/category")
public class ExpenseCategoryController {

    private final ExpenseCategoryService categoryService;

    public ExpenseCategoryController(ExpenseCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<ResponseCategoryDTO>> listAllCategory(@AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseEntity.ok(categoryService.listAllCategoryByUser(userDetails));
    }
    @GetMapping("/spendingPriority")
    public ResponseEntity<List<ResponseSpendingPriorityDTO>> listSpendingPriority(){
        return ResponseEntity.ok(categoryService.listSpendingPriority());
    }

    @PostMapping
    public ResponseEntity<ResponseCategoryDTO> createExpenseCategory(@RequestBody RequestCreateExpenseCategoryDTO createCategoryDTO,
                                                                     @AuthenticationPrincipal UserDetailsImpl userDetails){
        var data = categoryService.createExpenseCategory(createCategoryDTO, userDetails);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("{id}").buildAndExpand(data.getId()).toUri();
        return ResponseEntity.created(uri).body(new ResponseCategoryDTO(data));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateExpenseCategory(@PathVariable("id") Long id, @RequestBody RequestUpdateExpenseCategoryDTO updateCategoryDTO,
                                                      @AuthenticationPrincipal UserDetailsImpl userDetails){
        categoryService.updateCategory(id, updateCategoryDTO, userDetails);
        return ResponseEntity.noContent().build();
    }




}
