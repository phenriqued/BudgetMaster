package phenriqued.BudgetMaster.Controllers.IncomeControllers;

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
import phenriqued.BudgetMaster.DTOs.Income.RequestNewIncome;
import phenriqued.BudgetMaster.DTOs.Income.RequestUpdateIncome;
import phenriqued.BudgetMaster.DTOs.Income.ResponseAllIncomesDTO;
import phenriqued.BudgetMaster.DTOs.Income.ResponseIncomesDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Infra.Security.Config.SecurityConfiguration;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Services.IncomeService.IncomeService;

import java.net.URI;

@Tag(name = "Renda Controller", description = "Este controlador é responsável por criar, listar todos, listar por ID e DESCRIÇÃO, atualizar e deletar uma renda do usuário")
@SecurityRequirement(name = SecurityConfiguration.SECURITY)

@RestController
@RequestMapping("/income")
public class IncomeController {

    private final IncomeService incomeService;
    public IncomeController(IncomeService incomeService) {this.incomeService = incomeService;}

    @Operation(summary = "Lista todas as rendas", description = "responsável por listar todas as rendas e o total de montante que usuário tem.")
    @ApiResponse(responseCode = "200", description = "Lista com sucesso todos as rendas")
    @ApiResponse(responseCode = "401", description = "Não autenticado.")
    @GetMapping
    public ResponseEntity<ResponseAllIncomesDTO> listAllIncome(@PageableDefault(size = 5 ) Pageable pageable,
                                                                     @AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseEntity.ok(incomeService.listAllIncomes(pageable, userDetails));
    }

    @Operation(summary = "Cria uma nova renda", description = "responsável por criar um entidade renda")
    @ApiResponse(responseCode = "201", description = "renda criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Caso já exista alguma renda com descrição repetida ou montante não está correto, ou seja o o valor " +
            "deve corresponder a um ou mais dígitos numéricos positivos seguidos de um ponto e, finalmente, até dois dígitos numéricos ")
    @ApiResponse(responseCode = "401", description = "Não autenticado.")
    @PostMapping
    public ResponseEntity<RequestNewIncome> createIncome(@RequestBody @Valid RequestNewIncome requestIncomeDTO, @AuthenticationPrincipal UserDetailsImpl userDetails){
        var data = incomeService.createIncome(requestIncomeDTO, userDetails);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("{id}").buildAndExpand(data.getId()).toUri();
        return ResponseEntity.created(uri).body(new RequestNewIncome(data));
    }

    @Operation(summary = "Lista uma renda buscada pelo ID", description = "responsável por listar uma renda buscada pelo ID")
    @ApiResponse(responseCode = "200", description = "renda encontrada com sucesso")
    @ApiResponse(responseCode = "404", description = "Buscas não encontradas ou buscas para ID que não pertence ao usuário.")
    @ApiResponse(responseCode = "401", description = "Não autenticado.")
    @GetMapping("{id}")
    public ResponseEntity<ResponseIncomesDTO> getIncomeById(@PathVariable("id") Long id, @AuthenticationPrincipal UserDetailsImpl userDetails){
        try {
            return ResponseEntity.ok(incomeService.getIncomeById(id, userDetails));
        }catch (BusinessRuleException e){
            return ResponseEntity.notFound().build();
        }

    }
    @Operation(summary = "Lista uma renda buscada pela descrição", description = "responsável por listar uma renda buscada pela descrição")
    @ApiResponse(responseCode = "200", description = "renda encontrada com sucesso")
    @ApiResponse(responseCode = "404", description = "Buscas não encontradas ou buscas para descrições que não pertence ao usuário.")
    @ApiResponse(responseCode = "401", description = "Não autenticado.")
    @GetMapping("/search")
    public ResponseEntity<ResponseIncomesDTO> getIncomeByDescription(@RequestParam(value = "description") String description, @AuthenticationPrincipal UserDetailsImpl userDetails){
        try {
            return ResponseEntity.ok(incomeService.getIncomeByDescription(description, userDetails));
        }catch (BusinessRuleException e){
            return ResponseEntity.notFound().build();
        }
    }
    @Operation(summary = "Atualiza uma renda", description = "responsável por atualizar um renda, seja somente a descrição quanto somente o montante")
    @ApiResponse(responseCode = "204", description = "atualização feita com sucesso.")
    @ApiResponse(responseCode = "404", description = "Renda não encontradas ou atualização que não pertence ao usuário.")
    @ApiResponse(responseCode = "400", description = "Dados para atualização incorretos")
    @ApiResponse(responseCode = "401", description = "Não autenticado.")
    @PatchMapping
    public ResponseEntity<Void> updateIncome(@RequestParam(value = "id") Long id, @RequestBody @Valid RequestUpdateIncome requestIncomeDTO,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails){
        incomeService.updateIncome(id, requestIncomeDTO, userDetails);
        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "Deleta uma renda", description = "responsável por deletar uma renda do usuário")
    @ApiResponse(responseCode = "204", description = "deleção feita com sucesso.")
    @ApiResponse(responseCode = "404", description = "Renda não encontrada.")
    @ApiResponse(responseCode = "401", description = "Não autenticado.")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteIncome(@PathVariable("id") Long id,  @AuthenticationPrincipal UserDetailsImpl userDetails){
        incomeService.deleteIncome(id, userDetails);
        return ResponseEntity.noContent().build();
    }
}
