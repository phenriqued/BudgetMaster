package phenriqued.BudgetMaster.Controllers.LoginControllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import phenriqued.BudgetMaster.DTOs.Login.SignInDTO;
import phenriqued.BudgetMaster.DTOs.Token.TokenDTO;
import phenriqued.BudgetMaster.Services.LoginService.SignInService;

@RestController
@RequestMapping("login")
public class SignInController {

    private final SignInService service;

    public SignInController(SignInService service) {
        this.service = service;
    }

    @PostMapping("/signin")
    public ResponseEntity<TokenDTO> signIn(@RequestBody @Valid SignInDTO signInData){
        return ResponseEntity.ok(service.logIntoAccount(signInData));
    }
}
