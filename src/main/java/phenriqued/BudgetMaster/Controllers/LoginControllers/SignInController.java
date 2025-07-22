package phenriqued.BudgetMaster.Controllers.LoginControllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import phenriqued.BudgetMaster.DTOs.Login.SignInDTO;
import phenriqued.BudgetMaster.DTOs.Security.TwoFactorAuth.RequestValid2faDTO;
import phenriqued.BudgetMaster.DTOs.Security.Token.TokenDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterSecurityException;
import phenriqued.BudgetMaster.Services.LoginService.SignInService;

import java.net.URI;

@RestController
@RequestMapping("login/signin")
public class SignInController {

    private final SignInService service;

    public SignInController(SignInService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Void> getSignIn(){
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<TokenDTO> signIn(@RequestBody @Valid SignInDTO signInData){
        return ResponseEntity.ok(service.logIntoAccount(signInData));
    }

    @PutMapping("/validation2fa")
    public ResponseEntity<?> validationTwoFactorAuthentication(@RequestBody @Valid RequestValid2faDTO code){
        try{
            return ResponseEntity.ok(service.validation2fa(code));
        }catch (BudgetMasterSecurityException e){
            HttpHeaders httpHeaders = new HttpHeaders();
            String url = "http://localhost:8080/account/two-factor-authentication/resend?code="+code.code();
            httpHeaders.setLocation(URI.create(url));
            return new ResponseEntity<>(httpHeaders, HttpStatus.FOUND);
        }
    }
}
