package phenriqued.BudgetMaster.Controllers.TwoFactorAuthenticationController;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import phenriqued.BudgetMaster.DTOs.Security.TwoFactorAuth.Request2faActiveDTO;
import phenriqued.BudgetMaster.DTOs.Security.TwoFactorAuth.RequestValid2faDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Services.Security.TwoFactorAuthServices.TwoFactorAuthService;

@RestController
@RequestMapping("/account/two-factor-auth")
public class TwoFactorAuthController {

    private final TwoFactorAuthService twoFactorAuthService;

    public TwoFactorAuthController(TwoFactorAuthService twoFactorAuthService) {
        this.twoFactorAuthService = twoFactorAuthService;
    }

    @PostMapping("initiate")
    public ResponseEntity<String> activeTwoFactorAuthentication(@RequestBody @Valid Request2faActiveDTO request2faActiveDTO,
                                                                Authentication authentication){
        String result = twoFactorAuthService.createTwoFactorAuth(request2faActiveDTO, authentication.getName());
        return ResponseEntity.ok(result);
    }

    @PutMapping("validate")
    public ResponseEntity<?> verifyTwoFactorAuthentication(@RequestBody @Valid RequestValid2faDTO requestValid2faDTO, Authentication authentication){
        try{
            twoFactorAuthService.validationAndActivationTwoFactorAuth(authentication.getName(), requestValid2faDTO.code(), requestValid2faDTO.type2fa());
            return ResponseEntity.noContent().build();
        }catch (BusinessRuleException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("resend-code")
    public ResponseEntity<Void> getResendCodeTwoFactorAuthentication(@RequestParam(value = "code") String code){
        return ResponseEntity.ok().build();
    }
    @PostMapping("resend-code")
    public ResponseEntity<Void> resendCodeTwoFactorAuthentication(@RequestParam(value = "code") String code){
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        twoFactorAuthService.resendActivatedTwoFactorAuth(code, username);
        return ResponseEntity.noContent().build();
    }

}
