package phenriqued.BudgetMaster.Controllers.ExpenseControllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import phenriqued.BudgetMaster.DTOs.Expense.RequestCreateExpenseCategoryDTO;
import phenriqued.BudgetMaster.DTOs.Expense.RequestUpdateExpenseCategoryDTO;
import phenriqued.BudgetMaster.DTOs.Expense.ResponseCategoryDTO;
import phenriqued.BudgetMaster.DTOs.Expense.ResponseSpendingPriorityDTO;
import phenriqued.BudgetMaster.Infra.Security.Config.SecurityConfiguration;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Services.ExpenseService.ExpenseCategoryService;

import java.net.URI;
import java.util.List;

@Tag(name = "Categoria de Despesas Controller", description = "Este controlador é responsável por criar, listar todos, listar por ID e por prioridade de gastos, " +
        "atualizar e deletar uma categoria de despesa do usuário")
@SecurityRequirement(name = SecurityConfiguration.SECURITY)

@RestController
@RequestMapping("/expense/category")
public class ExpenseCategoryController {

    private final ExpenseCategoryService categoryService;

    public ExpenseCategoryController(ExpenseCategoryService categoryService) {
        this.categoryService = categoryService;
    }
    @Operation(summary = "Lista todas as categorias de despesas", description = "responsável por listar todas as categorias de depesas")
    @ApiResponse(responseCode = "200", description = "Lista com sucesso todos as categorias de despesas")
    @ApiResponse(responseCode = "401", description = "Não autenticado.")
    @GetMapping
    public ResponseEntity<List<ResponseCategoryDTO>> listAllCategory(@AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseEntity.ok(categoryService.listAllCategoryByUser(userDetails));
    }
    @Operation(summary = "Lista as prioridades de gastos", description = "responsável por listar todas as as prioridades de gastos")
    @ApiResponse(responseCode = "200", description = "Lista com sucesso todos as as prioridades de gastos")
    @ApiResponse(responseCode = "401", description = "Não autenticado.")
    @GetMapping("/spendingPriority")
    public ResponseEntity<List<ResponseSpendingPriorityDTO>> listSpendingPriority(){
        return ResponseEntity.ok(categoryService.listSpendingPriority());
    }
    @Operation(summary = "Cria uma categoria de despesas", description = "responsável por criar um entidade de categoria de despesa.")
    @ApiResponse(responseCode = "201", description = "renda criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Caso já exista alguma categoria de despesa com nome repetido pertencente aos usuários.")
    @ApiResponse(responseCode = "401", description = "Não autenticado.")
    @PostMapping
    public ResponseEntity<ResponseCategoryDTO> createExpenseCategory(@RequestBody RequestCreateExpenseCategoryDTO createCategoryDTO,
                                                                     @AuthenticationPrincipal UserDetailsImpl userDetails){
        var data = categoryService.createExpenseCategory(createCategoryDTO, userDetails);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("{id}").buildAndExpand(data.getId()).toUri();
        return ResponseEntity.created(uri).body(new ResponseCategoryDTO(data));
    }
    @Operation(summary = "Atualiza uma Categoria de despesas", description = "responsável por atualizar uma categoria de despesa, " +
            "seja somente o nome quanto somente a prioridade de gasto")
    @ApiResponse(responseCode = "204", description = "atualização feita com sucesso.")
    @ApiResponse(responseCode = "404", description = "Categoria não encontrada ou não pertence ao usuário.")
    @ApiResponse(responseCode = "400", description = "Dados para atualização incorretos")
    @ApiResponse(responseCode = "401", description = "Não autenticado.")
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateExpenseCategory(@PathVariable("id") Long id, @RequestBody RequestUpdateExpenseCategoryDTO updateCategoryDTO,
                                                      @AuthenticationPrincipal UserDetailsImpl userDetails){
        categoryService.updateCategory(id, updateCategoryDTO, userDetails);
        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "Deleta uma categoria", description = "responsável por deletar uma Categoria")
    @ApiResponse(responseCode = "204", description = "deleção feita com sucesso.")
    @ApiResponse(responseCode = "404", description = "Categoria não encontradas ou não pertence ao usuário.")
    @ApiResponse(responseCode = "401", description = "Não autenticado.")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteExpenseCategory(@PathVariable("id") Long id, @AuthenticationPrincipal UserDetailsImpl userDetails){
        categoryService.deleteCategory(id, userDetails);
        return ResponseEntity.noContent().build();
    }

}
