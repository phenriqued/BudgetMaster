package phenriqued.BudgetMaster.Controllers.UserControllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import phenriqued.BudgetMaster.DTOs.Security.PasswordDTOs.RequestConfirmationPasswordUser;
import phenriqued.BudgetMaster.DTOs.Security.PasswordDTOs.RequestOnlyNewPasswordChangeDTO;
import phenriqued.BudgetMaster.DTOs.Security.PasswordDTOs.RequestPasswordChangeUserDTO;
import phenriqued.BudgetMaster.Infra.Security.Config.SecurityConfiguration;
import phenriqued.BudgetMaster.Services.Security.TwoFactorAuthServices.TwoFactorAuthService;
import phenriqued.BudgetMaster.Services.UserServices.UserService;

import java.net.URI;

@Tag(name = "Gerenciamento de Conta de Usuário", description = "Gerencia operações como desativação de conta, alteração de senha e ativação/recuperação de conta.")
@SecurityRequirement(name = SecurityConfiguration.SECURITY)
@RestController
@RequestMapping("account/manager")
public class UserAccountController {

    private final UserService userService;
    private final TwoFactorAuthService twoFactorAuthService;

    public UserAccountController(UserService userService, TwoFactorAuthService twoFactorAuthService) {
        this.userService = userService;
        this.twoFactorAuthService = twoFactorAuthService;
    }

    @Operation(summary = "Desativar conta de usuário", description = "Desativa a conta do usuário autenticado após a confirmação da senha.")
    @ApiResponse(responseCode = "204", description = "Conta desativada com sucesso.")
    @ApiResponse(responseCode = "400", description = "Senha de confirmação inválida ou requisição malformada.")
    @ApiResponse(responseCode = "401", description = "Não autenticado ou senha de confirmação incorreta.")
    @ApiResponse(responseCode = "403", description = "Acesso negado (ex: usuário não tem permissão para desativar).")
    @PutMapping("disable")
    public ResponseEntity<Void> disableUser(@RequestBody @Valid RequestConfirmationPasswordUser confirmationPasswordUser, Authentication authentication){
        var user = userService.findUserByEmail(authentication.getName());
        userService.disableUser(confirmationPasswordUser, user);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Alterar senha do usuário", description = "Permite que o usuário autenticado altere sua senha existente.")
    @ApiResponse(responseCode = "204", description = "Senha alterada com sucesso.")
    @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos (ex: senhas não correspondem, validação falha).")
    @ApiResponse(responseCode = "401", description = "Não autenticado ou senha antiga incorreta.")
    @PutMapping("edit-password")
    public ResponseEntity<Void> editPassword(@RequestBody @Valid RequestPasswordChangeUserDTO requestPasswordChangeUserDTO,
                                             Authentication authentication){
        userService.changePassword(requestPasswordChangeUserDTO, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Alterar senha para ativar uma conta que foi desativada - recuperação",
            description = "Define uma nova senha para uma conta que está em estado de recuperação, usando um código de ativação/reset. Enviado pelo email")
    @ApiResponse(responseCode = "302", description = "Senha alterada e conta ativada/recuperada. Redireciona para a URL de login.")
    @ApiResponse(responseCode = "400", description = "Código de ativação/reset inválido ou expirado, ou nova senha inválida.")
    @PutMapping("change-password-to-activate")
    public ResponseEntity<Void> changePasswordToActivatedAccount(@RequestParam(value = "code") String code,
                                                                 @RequestBody @Valid RequestOnlyNewPasswordChangeDTO newPasswordChangeDTO){
        HttpHeaders httpHeaders = new HttpHeaders();
        String url = userService.changePasswordToActivateAccount(code, newPasswordChangeDTO);
        httpHeaders.setLocation(URI.create(url));

        SecurityContextHolder.clearContext();
        return new ResponseEntity<>(httpHeaders, HttpStatus.FOUND);
    }

}
