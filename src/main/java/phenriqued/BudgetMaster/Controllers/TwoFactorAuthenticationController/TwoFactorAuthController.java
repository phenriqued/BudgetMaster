package phenriqued.BudgetMaster.Controllers.TwoFactorAuthenticationController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import phenriqued.BudgetMaster.DTOs.Security.TwoFactorAuth.Request2faActiveDTO;
import phenriqued.BudgetMaster.DTOs.Security.TwoFactorAuth.RequestValid2faDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterSecurityException;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Infra.Security.Config.SecurityConfiguration;
import phenriqued.BudgetMaster.Services.Security.TwoFactorAuthServices.TwoFactorAuthService;

import java.net.URI;
import java.net.URISyntaxException;

@Tag(name = "Autenticação de dois fatores", description = "Este controlador é responsável por ativar, verificar e enviar um novo código de autenticação" +
        "podendo ser EMAIL e AUTENTICADOR.")
@SecurityRequirement(name = SecurityConfiguration.SECURITY)

@RestController
@RequestMapping("/account/two-factor-auth")
public class TwoFactorAuthController {

    private final TwoFactorAuthService twoFactorAuthService;

    public TwoFactorAuthController(TwoFactorAuthService twoFactorAuthService) {
        this.twoFactorAuthService = twoFactorAuthService;
    }

    @Operation(summary = "Iniciar configuração da A2F", description = "Inicia o processo de configuração da Autenticação de Dois Fatores para o usuário autenticado.")
    @ApiResponse(responseCode = "200", description = "Configuração iniciada com sucesso. Retorna informações para o setup (ex: QR code/chave).")
    @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos.")
    @ApiResponse(responseCode = "403", description = "Não autenticado.")
    @PostMapping("initiate")
    public ResponseEntity<String> activeTwoFactorAuthentication(@RequestBody @Valid Request2faActiveDTO request2faActiveDTO,
                                                                Authentication authentication){
        String result = twoFactorAuthService.createTwoFactorAuth(request2faActiveDTO, authentication.getName());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Validar código da A2F", description = "Valida o código de Autenticação de Dois Fatores fornecido pelo usuário para ativar a A2F ou completar o login.")
    @ApiResponse(responseCode = "204", description = "Código validado e A2F ativada com sucesso.")
    @ApiResponse(responseCode = "400", description = "Código A2F inválido ou tipo incorreto.")
    @ApiResponse(responseCode = "401", description = "Não autenticado.")
    @ApiResponse(responseCode = "302", description = "Código A2F expirado. Redireciona para o endpoint de reenvio de código.")
    @PutMapping("validate")
    public ResponseEntity<?> verifyTwoFactorAuthentication(@RequestBody @Valid RequestValid2faDTO requestValid2faDTO, Authentication authentication) throws URISyntaxException {
        try{
            twoFactorAuthService.validationAndActivationTwoFactorAuth(authentication.getName(), requestValid2faDTO.code(), requestValid2faDTO.type2fa());
            return ResponseEntity.noContent().build();
        }catch (BusinessRuleException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (BudgetMasterSecurityException e){
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setLocation(new URI("http://localhost:8080/account/two-factor-auth/resend-code?code="+requestValid2faDTO.code()));
            return new ResponseEntity<>(httpHeaders, HttpStatus.FOUND);
        }
    }
    @Operation(summary = "Obter status/informações de reenvio de código A2F (via GET)",
            description = "Endpoint para obter informações ou iniciar um reenvio de código A2F via método GET. ")
    @GetMapping("resend-code")
    public ResponseEntity<Void> getResendCodeTwoFactorAuthentication(@RequestParam(value = "code") String code){
        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "Reenviar código da A2F", description = "Solicita o reenvio de um novo código de Autenticação de Dois Fatores para o usuário.")
    @ApiResponse(responseCode = "204", description = "Reenvio de código A2F solicitado com sucesso.")
    @ApiResponse(responseCode = "400", description = "Parâmetro 'code' ausente ou inválido.")
    @ApiResponse(responseCode = "401", description = "Não autenticado.")
    @PostMapping("resend-code")
    public ResponseEntity<Void> resendCodeTwoFactorAuthentication(@RequestParam(value = "code") String code){
        twoFactorAuthService.resendActivatedTwoFactorAuth(code);
        return ResponseEntity.noContent().build();
    }

}
