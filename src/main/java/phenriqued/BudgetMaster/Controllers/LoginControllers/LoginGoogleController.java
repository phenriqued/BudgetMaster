package phenriqued.BudgetMaster.Controllers.LoginControllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import phenriqued.BudgetMaster.DTOs.Login.LoginGoogleRequestDTO;
import phenriqued.BudgetMaster.DTOs.Token.TokenDTO;
import phenriqued.BudgetMaster.Services.LoginService.LoginGoogleService;

import java.net.URI;

@RestController
@RequestMapping("/login/google")
public class LoginGoogleController {

    private final LoginGoogleService service;

    public LoginGoogleController(LoginGoogleService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Void> redirectGoogleOAuth() {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(service.gerarUrl()));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/authorized")
    public ResponseEntity<String> getAccessTokenGoogle(@RequestParam("code") String code){
        return ResponseEntity.ok(service.getToken(code));
    }

    @PostMapping("/authorized")
    public ResponseEntity<TokenDTO> loginWithGoogle(@RequestBody @Valid LoginGoogleRequestDTO loginGoogleDTO){
        return ResponseEntity.ok(service.loginGoogle(loginGoogleDTO));
    }

}
