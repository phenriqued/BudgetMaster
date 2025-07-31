package phenriqued.BudgetMaster.Controllers.LoginControllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import phenriqued.BudgetMaster.DTOs.Login.RegisterUserDTO;
import phenriqued.BudgetMaster.DTOs.Security.Token.RequestTokenDTO;
import phenriqued.BudgetMaster.DTOs.Security.Token.TokenDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterSecurityException;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Services.LoginService.SignUpService;

import java.net.URI;

@Tag(name = "Sign Up", description = "responsável pela criação de um usuário e ativação do mesmo.")

@RestController
@RequestMapping("/login")
public class SignUpController {

    private final SignUpService service;

    public SignUpController(SignUpService service) {
        this.service = service;
    }

    @Operation(summary = "cadastro de usuário", description = "Faz o cadastro de um novo usuário, mas não ativa o mesmo.")
    @ApiResponse(responseCode = "201", description = "Usuário salvo com sucesso")
    @ApiResponse(responseCode = "409", description = "Criar um usuário já existente no banco de dados")
    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody @Valid RegisterUserDTO registerUserDTO, UriComponentsBuilder builder){
        service.registerUser(registerUserDTO);
        URI uri = builder.path("/login/activate-user").buildAndExpand().toUri();
        return ResponseEntity.created(uri).body("Conta criada. Verifique seu e-mail.");
    }

    @Operation(summary = "ativação do usuário", description = "Faz a validação do código e ativa o usuário")
    @ApiResponse(responseCode = "200", description = "Código válido e usuário ativado com sucesso")
    @ApiResponse(responseCode = "302", description = "O código está correto, mas o tempo de validação se expirou. Encaminhado para URL para reenviar um código válido.")
    @ApiResponse(responseCode = "400", description = "Código digitado não está válido.")
    @PutMapping("/activate-user")
    public ResponseEntity<?> activationUser(@RequestParam("code") String code, @RequestBody @Valid RequestTokenDTO requestTokenDTO){
        try {
            return ResponseEntity.ok().body(service.activateUser(code, requestTokenDTO));
        } catch (BusinessRuleException e) {
            String uriSendNewCode = "http://localhost:8080/login/resend-code?code="+code;
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(uriSendNewCode));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        } catch (BudgetMasterSecurityException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



    @GetMapping("/resend-code")
    public ResponseEntity<String> resendCodeActivationUser(@RequestParam("code") String code){
        if(service.resendCodeActivateUser(code)){
            return ResponseEntity.ok().body("Token enviado novamente.");
        }
        return ResponseEntity.badRequest().body("Token está valido!");
    }
}
