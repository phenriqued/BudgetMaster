package phenriqued.BudgetMaster.Controllers.FamilyControllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import phenriqued.BudgetMaster.DTOs.Family.FinancialMovement.ResponseFamilyTotalDTO;
import phenriqued.BudgetMaster.Infra.Security.Config.SecurityConfiguration;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Services.FamilyService.FamilyIncomeService;

@Tag(name = "Renda da Familia Controller", description = "Este controlador é responsável por listar a renda individual de cada membro e o total da familia")
@SecurityRequirement(name = SecurityConfiguration.SECURITY)

@RestController
@RequestMapping("/families/{id}/income")
public class FamilyIncomeController {

    private final FamilyIncomeService familyIncomeService;

    public FamilyIncomeController(FamilyIncomeService familyIncomeService) {
        this.familyIncomeService = familyIncomeService;
    }
    @Operation(summary = "Lista a renda de todos os membros da família",
            description = "Lista a renda total da família, agregando a renda de todos os membros, e também a renda individual de cada membro. Requer que o usuário autenticado seja membro da família.")
    @SecurityRequirement(name = SecurityConfiguration.SECURITY)
    @ApiResponse(responseCode = "200", description = "Renda da família listada com sucesso!",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseFamilyTotalDTO.class),
                    examples = @ExampleObject(
                            summary = "Exemplo de resposta de sucesso",
                            value = """
                    {
                      "memberList": [
                        {
                          "name": "João Silva",
                          "total": 1500.00
                        },
                        {
                          "name": "Maria Souza",
                          "total": 2500.00
                        }
                      ],
                      "total": 4000.00
                    }
                    """
                    )
            )
    )
    @ApiResponse(responseCode = "401", description = "Não autenticado.")
    @ApiResponse(responseCode = "403", description = "Proibido. O usuário autenticado não é membro da família e não tem permissão para visualizar a renda.")
    @ApiResponse(responseCode = "404", description = "Não encontrado. A família com o ID especificado não existe.")

    @GetMapping
    public ResponseEntity<ResponseFamilyTotalDTO> listAllIncomeFamily(@PathVariable(value = "id") Long id,
                                                                      @AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseEntity.ok(familyIncomeService.getAllIncomeFamily(id, userDetails));
    }

}
