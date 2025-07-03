package phenriqued.BudgetMaster.Controllers.LoginControllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import phenriqued.BudgetMaster.DTOs.Login.RegisterUserDTO;
import phenriqued.BudgetMaster.DTOs.Token.RequestTokenDTO;
import phenriqued.BudgetMaster.DTOs.Token.TokenDTO;
import phenriqued.BudgetMaster.Services.LoginService.SignUpService;

import java.net.URI;

@RestController
@RequestMapping("/login")
public class SignUpController {

    private final SignUpService service;

    public SignUpController(SignUpService service) {
        this.service = service;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody @Valid RegisterUserDTO registerUserDTO, UriComponentsBuilder builder){
        service.registerUser(registerUserDTO);
        URI uri = builder.path("/login/activate-user").buildAndExpand().toUri();
        return ResponseEntity.created(uri).body("Conta criada. Verifique seu e-mail.");
    }

    @PutMapping("/activate-user")
    public ResponseEntity<TokenDTO> activationUser(@RequestParam("code") String code, @RequestBody @Valid RequestTokenDTO requestTokenDTO){
        return ResponseEntity.ok().body(service.activateUser(code, requestTokenDTO));
    }

}
