package phenriqued.BudgetMaster.Controllers.LoginControllers;

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

    @PostMapping("/signin")
    public ResponseEntity<TokenSignInDTO> signIn(@RequestBody @Valid SignInDTO signInData){
        return ResponseEntity.ok(service.logIntoAccount(signInData));
    }

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

    @PostMapping("/update-token")
    public ResponseEntity<TokenDTO> updateTokens(@RequestBody @Valid RequestRefreshTokenDTO refreshTokenDTO){
        return ResponseEntity.ok(service.updateTokens(refreshTokenDTO));
    }
}
