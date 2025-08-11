package phenriqued.BudgetMaster.Controllers.EmergencyReserveControllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import phenriqued.BudgetMaster.DTOs.EmergencyReserve.ResponseEmergencyReserveDTO;
import phenriqued.BudgetMaster.DTOs.EmergencyReserve.ResponseEmergencyReserveProgressDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Infra.Security.Config.SecurityConfiguration;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Services.EmergencyReserveService.ReserveService;

import java.math.BigDecimal;

@Tag(name = "Reserva de Emergência", description = "Endpoints para gerenciar e calcular a reserva de emergência do usuário.")
@SecurityRequirement(name = SecurityConfiguration.SECURITY)

@RestController
@RequestMapping("/emergency-reserve")
public class EmergencyReserveController {

    private final ReserveService service;

    public EmergencyReserveController(ReserveService service) {
        this.service = service;
    }
    @Operation(summary = "Obter o valor total da reserva de emergência",
            description = "Retorna o valor total da reserva de emergência, calculada com base nos gastos essenciais do usuário nos últimos 6 meses.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação bem-sucedida",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseEmergencyReserveDTO.class))),
            @ApiResponse(responseCode = "204", description = "Usuário não possui dados suficientes para o cálculo."),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    @GetMapping
    public ResponseEntity<ResponseEmergencyReserveDTO> getEmergencyReserve(@AuthenticationPrincipal UserDetailsImpl userDetails){
        try{
            return ResponseEntity.ok(service.getTotalEmergencyReserve(userDetails));
        }catch (BusinessRuleException e){
            return ResponseEntity.noContent().build();
        }
    }

    @Operation(summary = "Obter o progresso da reserva de emergência",
            description = "Calcula e retorna a porcentagem do valor da reserva de emergência atingida com base no saldo atual do usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Progresso calculado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseEmergencyReserveProgressDTO.class))),
            @ApiResponse(responseCode = "400", description = "Não foi possível calcular o progresso.",
                    content = @Content(examples = @ExampleObject(value = "No expenses and/or income registered"))),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    @GetMapping("/progress")
    public ResponseEntity<?> getEmergencyReserveProgress(@AuthenticationPrincipal UserDetailsImpl userDetails){
        try {
            return ResponseEntity.ok(service.getEmergencyReserveProgress(userDetails));
        }catch (BusinessRuleException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Simular o progresso da reserva de emergência",
            description = "Simula em meses quanto tempo o usuário levaria para atingir a reserva de emergência com base em uma economia mensal.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Simulação calculada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseEmergencyReserveProgressDTO.class))),
            @ApiResponse(responseCode = "400", description = "Não foi possível realizar a simulação.",
                    content = @Content(examples = @ExampleObject(value = "No expenses and/or income registered"))),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    @GetMapping("/simulation")
    public ResponseEntity<?> getSimulationEmergencyReserveProgress(@Parameter(description = "Valor que o usuário economiza por mês.") @RequestParam(value = "monthlySaving") Integer monthlySaving,
                                                                   @AuthenticationPrincipal UserDetailsImpl userDetails){
        try {
            return ResponseEntity.ok(service.getSimulationEmergencyReserveProgress(monthlySaving, userDetails));
        }catch (BusinessRuleException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
