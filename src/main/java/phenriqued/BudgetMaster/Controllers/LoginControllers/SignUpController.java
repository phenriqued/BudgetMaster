package phenriqued.BudgetMaster.Controllers.LoginControllers;

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
        try {
            return ResponseEntity.ok().body(service.activateUser(code, requestTokenDTO));
        } catch (BudgetMasterSecurityException | BusinessRuleException e) {
            String uriSendNewCode = "http://localhost:8080/login/resend-code?code="+code;
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(uriSendNewCode));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }
    }

    @GetMapping("/resend-code")
    public ResponseEntity<String> resendCodeActivationUser(@RequestParam("code") String code){
        if(service.resendCodeActivateUser(code)){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("Token está valido!");
    }


}
