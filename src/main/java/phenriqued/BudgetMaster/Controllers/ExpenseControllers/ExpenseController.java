package phenriqued.BudgetMaster.Controllers.ExpenseControllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import phenriqued.BudgetMaster.DTOs.Expense.RequestCreateExpenseDTO;
import phenriqued.BudgetMaster.DTOs.Expense.RequestUpdateExpenseDTO;
import phenriqued.BudgetMaster.DTOs.Expense.ResponseAllExpenseDTO;
import phenriqued.BudgetMaster.DTOs.Expense.ResponseExpenseDTO;
import phenriqued.BudgetMaster.Infra.Security.Config.SecurityConfiguration;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Services.ExpenseService.ExpenseService;

import java.net.URI;

@Tag(name = "Despesa Controller", description = "Este controlador é responsável por criar, listar todos, listar por ID e por categoria, atualizar e deletar uma despesa do usuário")
@SecurityRequirement(name = SecurityConfiguration.SECURITY)

@RestController
@RequestMapping("/expense")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }
    @Operation(summary = "Lista todas as depesas", description = "responsável por listar todas as depesas e o total de montante de despesas.")
    @ApiResponse(responseCode = "200", description = "Lista com sucesso todos as despesas")
    @ApiResponse(responseCode = "401", description = "Não autenticado.")
    @GetMapping
    public ResponseEntity<ResponseAllExpenseDTO> listAllExpense(@PageableDefault(size = 5) Pageable pageable, @AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseEntity.ok(expenseService.getAllExpense(pageable, userDetails));
    }
    @Operation(summary = "Cria uma despesa", description = "responsável por criar um entidade de despesa.")
    @ApiResponse(responseCode = "201", description = "despesa criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Caso já exista alguma despesa com descrição repetida ou montante não está correto, ou seja o o valor " +
            "deve corresponder a um ou mais dígitos numéricos positivos seguidos de um ponto e, finalmente, até dois dígitos numéricos ")
    @ApiResponse(responseCode = "401", description = "Não autenticado.")
    @PostMapping
    public ResponseEntity<ResponseExpenseDTO> createExpense(@RequestBody @Valid RequestCreateExpenseDTO createExpenseDTO, @AuthenticationPrincipal UserDetailsImpl userDetails){
        var data = expenseService.createExpense(createExpenseDTO, userDetails);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("{id}").buildAndExpand(data.getId()).toUri();
        return ResponseEntity.created(uri).body(new ResponseExpenseDTO(data));
    }
    @Operation(summary = "Retorna uma despesa", description = "responsável por retornar uma despesa procurada por ID da mesma")
    @ApiResponse(responseCode = "200", description = "retorna a despesa correspondente ao usuário e o ID")
    @ApiResponse(responseCode = "404", description = "Caso não exista uma despesa corresponde ao usuário ou o ID")
    @ApiResponse(responseCode = "401", description = "Não autenticado.")
    @GetMapping("/search")
    public ResponseEntity<ResponseExpenseDTO> listExpenseById(@RequestParam("id") Long id, @AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseEntity.ok(expenseService.getExpenseById(id, userDetails));
    }
    @Operation(summary = "Retorna uma lista de despesas por categoria", description = "responsável por retornar uma lista de despesas por categoria")
    @ApiResponse(responseCode = "200", description = "retorna uma lista de despesas correspondente ao usuário e a categoria")
    @ApiResponse(responseCode = "404", description = "Caso não exista uma despesa corresponde a categoria de busca ou pertencente ao usuário")
    @ApiResponse(responseCode = "401", description = "Não autenticado.")
    @GetMapping("/search/category")
    public ResponseEntity<ResponseAllExpenseDTO> listAllExpenseByCategory(@PageableDefault(size = 5) Pageable pageable, @RequestParam("category") String category,
                                                                          @AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseEntity.ok(expenseService.getAllExpenseByCategory(pageable, category, userDetails));
    }
    @Operation(summary = "Retorna uma lista de despesas por prioridade de gastos", description = "responsável por retornar uma lista de despesas por prioridade de gasto")
    @ApiResponse(responseCode = "200", description = "retorna uma lista de despesas correspondente ao usuário e a prioridade de gasto")
    @ApiResponse(responseCode = "404", description = "Caso não exista uma despesa corresponde a prioridade de gasto ou pertencente ao usuário")
    @ApiResponse(responseCode = "401", description = "Não autenticado.")
    @GetMapping("/search/spending-priority/{id}")
    public ResponseEntity<ResponseAllExpenseDTO> listAllExpenseBySpendingPriority(@PageableDefault(size = 10) Pageable pageable, @PathVariable("id") Long id,
                                                                          @AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseEntity.ok(expenseService.getAllExpenseBySpendingPriority(pageable, id, userDetails));
    }

    @Operation(summary = "Atualiza uma despesa", description = "responsável por atualizar uma despesa, seja somente a descrição quanto somente o montante")
    @ApiResponse(responseCode = "204", description = "atualização feita com sucesso.")
    @ApiResponse(responseCode = "404", description = "Despesa não encontradas ou não pertence ao usuário.")
    @ApiResponse(responseCode = "400", description = "Dados para atualização incorretos")
    @ApiResponse(responseCode = "401", description = "Não autenticado.")
    @PatchMapping("/update/{id}")
    public ResponseEntity<Void> updateExpense(@PathVariable("id") Long id, @RequestBody RequestUpdateExpenseDTO updateExpenseDTO,
                                              @AuthenticationPrincipal UserDetailsImpl userDetails){
        expenseService.updateExpense(id, updateExpenseDTO, userDetails);
        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "Deleta uma renda", description = "responsável por deletar uma despesa")
    @ApiResponse(responseCode = "204", description = "deleção feita com sucesso.")
    @ApiResponse(responseCode = "404", description = "Despesa não encontradas ou não pertence ao usuário.")
    @ApiResponse(responseCode = "401", description = "Não autenticado.")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable("id") Long id, @AuthenticationPrincipal UserDetailsImpl userDetails){
        expenseService.deleteExpense(id, userDetails);
        return ResponseEntity.noContent().build();
    }

}
