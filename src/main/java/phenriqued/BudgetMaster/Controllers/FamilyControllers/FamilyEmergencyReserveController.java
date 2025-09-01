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
import phenriqued.BudgetMaster.DTOs.EmergencyReserve.ResponseEmergencyReserveDTO;
import phenriqued.BudgetMaster.DTOs.Family.FinancialMovement.ResponseFamilyTotalDTO;
import phenriqued.BudgetMaster.Infra.Security.Config.SecurityConfiguration;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Services.FamilyService.FamilyEmergencyReserveService;

@Tag(name = "Reserva de Emergencia da Familia Controller", description = "Este controlador é responsável por gerar a reserva de emergencia da familia")
@SecurityRequirement(name = SecurityConfiguration.SECURITY)

@RestController
@RequestMapping("/families/{id}/emergency-reserve")
public class FamilyEmergencyReserveController {

    private final FamilyEmergencyReserveService service;

    public FamilyEmergencyReserveController(FamilyEmergencyReserveService service) {
        this.service = service;
    }
    @Operation(summary = "Retornar a reserva de emergência da familia",
            description = "Lista a reserva de emergência da familia para 6 meses")
    @SecurityRequirement(name = SecurityConfiguration.SECURITY)
    @ApiResponse(responseCode = "200", description = "Reserva de emergencia da família listada com sucesso!",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseFamilyTotalDTO.class),
                    examples = @ExampleObject(
                            summary = "Exemplo de resposta de sucesso",
                            value = """
                    {
                        "idealReserve": 26262.00,
                        "currency": "BRL",
                        "calculateAt": "2025-09-01"
                    }
                    """
                    )
            )
    )
    @ApiResponse(responseCode = "401", description = "Não autenticado.")
    @ApiResponse(responseCode = "403", description = "Proibido. O usuário autenticado não é membro da família e não tem permissão para visualizar a despesa.")
    @ApiResponse(responseCode = "404", description = "Não encontrado. A família com o ID especificado não existe.")
    @GetMapping
    public ResponseEntity<ResponseEmergencyReserveDTO> getFamilyEmergencyReserve(@PathVariable(value = "id") Long id,
                                                                                 @AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseEntity.ok(service.getTotalReserveFamily(id, userDetails));
    }

}
