package phenriqued.BudgetMaster.Controllers.UserControllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import phenriqued.BudgetMaster.DTOs.Security.PasswordDTOs.RequestConfirmationPasswordUser;
import phenriqued.BudgetMaster.DTOs.Security.PasswordDTOs.RequestOnlyNewPasswordChangeDTO;
import phenriqued.BudgetMaster.DTOs.Security.PasswordDTOs.RequestPasswordChangeUserDTO;
import phenriqued.BudgetMaster.Services.Security.TwoFactorAuthServices.TwoFactorAuthService;
import phenriqued.BudgetMaster.Services.UserServices.UserService;

import java.net.URI;

@RestController
@RequestMapping("account/manager")
public class UserAccountController {

    private final UserService userService;
    private final TwoFactorAuthService twoFactorAuthService;

    public UserAccountController(UserService userService, TwoFactorAuthService twoFactorAuthService) {
        this.userService = userService;
        this.twoFactorAuthService = twoFactorAuthService;
    }

    @PutMapping("disable")
    public ResponseEntity<Void> disableUser(@RequestBody @Valid RequestConfirmationPasswordUser confirmationPasswordUser, Authentication authentication){
        var user = userService.findUserByEmail(authentication.getName());
        userService.disableUser(confirmationPasswordUser, user);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("edit-password")
    public ResponseEntity<Void> editPassword(@RequestBody @Valid RequestPasswordChangeUserDTO requestPasswordChangeUserDTO,
                                             Authentication authentication){
        userService.changePassword(requestPasswordChangeUserDTO, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("change-password-to-activate")
    public ResponseEntity<Void> changePasswordToActivatedAccount(@RequestParam(value = "code") String code,
                                                                 @RequestBody @Valid RequestOnlyNewPasswordChangeDTO newPasswordChangeDTO){
        HttpHeaders httpHeaders = new HttpHeaders();
        String url = userService.changePasswordToActivateAccount(code, newPasswordChangeDTO);
        httpHeaders.setLocation(URI.create(url));

        SecurityContextHolder.clearContext();
        return new ResponseEntity<>(httpHeaders, HttpStatus.FOUND);
    }

}
