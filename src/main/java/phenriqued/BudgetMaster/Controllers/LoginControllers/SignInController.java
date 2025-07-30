package phenriqued.BudgetMaster.Controllers.LoginControllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import phenriqued.BudgetMaster.DTOs.Login.SignInDTO;
import phenriqued.BudgetMaster.DTOs.Security.Token.RequestRefreshTokenDTO;
import phenriqued.BudgetMaster.DTOs.Security.Token.TokenDTO;
import phenriqued.BudgetMaster.DTOs.Security.Token.TokenSignInDTO;
import phenriqued.BudgetMaster.DTOs.Security.TwoFactorAuth.RequestValidSignIn2faDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterSecurityException;
import phenriqued.BudgetMaster.Services.LoginService.SignInService;

import java.net.URI;

@Tag(name = "Sign In", description = "responsável por autenticar um usuário na aplicação")

@RestController
@RequestMapping("/login")
public class SignInController {

    private final SignInService service;

    public SignInController(SignInService service) {
        this.service = service;
    }

    @GetMapping("/signin")
    public ResponseEntity<Void> getSignIn(){
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Autentica um usuário e realiza o login",
            description = "Este endpoint permite que os usuários se autentiquem na aplicação. " +
                    "O fluxo de resposta varia conforme o status da autenticação de dois fatores (A2F) do usuário:\n\n" +
                    "- **Usuários sem A2F ativada:** Recebem diretamente um token de acesso JWT e um refresh token para continuar utilizando a API.\n" +
                    "- **Usuários com A2F ativada:** Recebem um token de segurança temporário e uma mensagem indicando a necessidade de validação do código A2F. Neste caso, os campos de JWT e refresh token estarão nulos, aguardando a etapa de validação da A2F.\n\n" +
                    "Em caso de credenciais inválidas ou conta desabilitada, um erro 403 será retornado."
    )
    @ApiResponse(responseCode = "200", description = "para usuário que tem A2F desativada retorna o token JWT e Refresh token")
    @ApiResponse(responseCode = "200", description = "para usuário que tem A2F ativada retorna Security token e A2F Message com JWT e Refresh Token null")
    @ApiResponse(responseCode = "403", description = "Acesso Negado: Credenciais inválidas (e-mail ou senha incorretos")
    @PostMapping("/signin")
    public ResponseEntity<TokenSignInDTO> signIn(@RequestBody @Valid SignInDTO signInData){
        return ResponseEntity.ok(service.logIntoAccount(signInData));
    }

    @Operation(summary = "Efetua a validação do código A2F", description = "Responsável pela validação do código de autenticação de dois fatores")
    @ApiResponse(responseCode = "200", description = "OK: O codigo está válido e retorna os tokens JWT e Refresh Token")
    @ApiResponse(responseCode = "302", description = "FOUND: O codigo está expirado e encaminha para reenviar o código para o usuário.")
    @ApiResponse(responseCode = "400", description = "BAD REQUEST: O codigo está inválido.")
    @PutMapping("/validation2fa")
    public ResponseEntity<?> validationTwoFactorAuthentication(@RequestBody @Valid RequestValidSignIn2faDTO code){
        try{
            return ResponseEntity.ok(service.validation2fa(code));
        }catch (BudgetMasterSecurityException e){
            HttpHeaders httpHeaders = new HttpHeaders();
            String url = "http://localhost:8080/account/two-factor-auth/resend-code?code="+code.code();
            httpHeaders.setLocation(URI.create(url));
            return new ResponseEntity<>(httpHeaders, HttpStatus.FOUND);
        }
    }

    @Operation(summary = "Atualiza tokens de acesso e refresh",
            description = "Este endpoint permite a renovação dos tokens de autenticação (JWT e Refresh Token) utilizando um Refresh Token válido. ")
    @ApiResponse(responseCode = "200", description = "Tokens atualizados com sucesso. Retorna um novo Access Token JWT e um novo Refresh Token."
    )
    @ApiResponse(responseCode = "400", description = "Requisição inválida: O Refresh Token fornecido é nulo ou está em formato incorreto.")
    @ApiResponse(responseCode = "401", description = "Não Autorizado: O Refresh Token é inválido, expirado ou não corresponde a uma sessão ativa.")
    @PostMapping("/update-token")
    public ResponseEntity<TokenDTO> updateTokens(@RequestBody @Valid RequestRefreshTokenDTO refreshTokenDTO){
        return ResponseEntity.ok(service.updateTokens(refreshTokenDTO));
    }
}
