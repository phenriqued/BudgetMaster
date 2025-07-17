package phenriqued.BudgetMaster.Controllers.LoginControllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import phenriqued.BudgetMaster.DTOs.Login.SignInDTO;
import phenriqued.BudgetMaster.DTOs.Token.TokenDTO;
import phenriqued.BudgetMaster.Services.LoginService.SignInService;

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
}
