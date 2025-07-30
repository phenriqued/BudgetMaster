package phenriqued.BudgetMaster.Controllers.LoginControllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import phenriqued.BudgetMaster.DTOs.Login.LoginGoogleRequestDTO;
import phenriqued.BudgetMaster.DTOs.Security.Token.TokenDTO;
import phenriqued.BudgetMaster.Services.LoginService.LoginGoogleService;

import java.net.URI;

@Tag(name = "autenticação de usuários via Google OAuth 2.0",
        description = "Este controlador expõe endpoints para iniciar o processo de redirecionamento para o Google, " +
                "receber o código de autorização e, finalmente, realizar o login na aplicação utilizando as credenciais do Google.")

@RestController
@RequestMapping("/login/google")
public class LoginGoogleController {

    private final LoginGoogleService service;

    public LoginGoogleController(LoginGoogleService service) {
        this.service = service;
    }

    @Operation(summary = "Redireciona o usuário para a página de consentimento do Google", description = "Este endpoint inicia o fluxo de autenticação do Google.")
    @GetMapping
    public ResponseEntity<Void> redirectGoogleOAuth() {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(service.gerarUrl()));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @Operation(summary = "Endpoint de callback para receber o código de autorização do Google",
            description = "Após o usuário conceder permissão no Google, o Google redireciona o navegador de volta para este endpoint, incluindo um \"code\" como parâmetro de query. " +
                    " Este código é então trocado por um token de acesso do Google.")
    @GetMapping("/authorized")
    public ResponseEntity<String> getAccessTokenGoogle(@RequestParam("code") String code){
        return ResponseEntity.ok(service.getToken(code));
    }

    @Operation(summary = "Realiza o login do usuário na aplicação utilizando o token de acesso do Google.",
            description = "Este endpoint recebe o Access Token do Google (obtido na etapa anterior) e o utiliza para autenticar ou registrar o usuário na sua aplicação." +
                    " Se o usuário já existir, ele será logado; caso contrário, um novo usuário pode ser criado.")
    @PostMapping("/authorized")
    public ResponseEntity<TokenDTO> loginWithGoogle(@RequestBody @Valid LoginGoogleRequestDTO loginGoogleDTO){
        return ResponseEntity.ok(service.loginGoogle(loginGoogleDTO));
    }

}
