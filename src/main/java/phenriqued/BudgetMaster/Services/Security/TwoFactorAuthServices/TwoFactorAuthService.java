package phenriqued.BudgetMaster.Services.Security.TwoFactorAuthServices;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.DTOs.Login.SignInDTO;
import phenriqued.BudgetMaster.DTOs.Security.TwoFactorAuth.Request2faActiveDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterSecurityException;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Models.Security.TwoFactorAuthentication.TwoFactorAuth;
import phenriqued.BudgetMaster.Models.Security.TwoFactorAuthentication.Type2FA;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.Security.TwoFactorAuthRepository;
import phenriqued.BudgetMaster.Services.UserServices.UserService;

import java.util.Comparator;
import java.util.Objects;

@Service
@AllArgsConstructor
public class TwoFactorAuthService {

    private final TwoFactorAuthRepository repository;
    private final UserService userService;
    private final OtpEmailService otpEmail;
    private final TotpAuthenticatorService totpAuthenticator;

    public String createTwoFactorAuth(Request2faActiveDTO request2faDTO, String username) {
        var user = userService.findUserByEmail(username);
        var type2fa = Type2FA.valueOf(request2faDTO.type2FA());
        if(repository.existsByUserAndType2FA(user, type2fa))
            throw new BusinessRuleException("there is already an active two-factor authentication of type "+type2fa);

        if (type2fa.equals(Type2FA.AUTHENTICATOR)){
            return totpAuthenticator.generatedAuthenticatorQrCode(user);
        } else if (type2fa.equals(Type2FA.EMAIL)) {
            otpEmail.createSecurityCode(user);
        }
        return "Code sent successfully";
    }

    public void resendActivatedTwoFactorAuth(String code){
        var user = repository.findBySecret(code)
                .orElseThrow(() -> new BusinessRuleException("invalid code, check the code entered.")).getUser();
        var twoFactorAuth = repository.findBySecretAndUser(code, user)
                .orElseThrow(() -> new BusinessRuleException("invalid code, check the code entered."));
        if (twoFactorAuth.getType2FA().equals(Type2FA.EMAIL)){
            otpEmail.resendSecurityCode(twoFactorAuth.getUser());
        }
    }

    public String generatedCodeTwoFactorAuth(SignInDTO data){
        User user = userService.findUserByEmail(data.email());
        var twoFactorAuth = priorityOrderTwoFactorAuth(user);

        if(twoFactorAuth.getType2FA().equals(Type2FA.AUTHENTICATOR)){
            return "access the authenticator";
        }else {
            otpEmail.generatedSecurityCodeTwoFactorAuth(user, data);
        }
        return "Code sent successfully";
    }

    public TwoFactorAuth validationAndActivationTwoFactorAuth(String username, String code, String type2fa){
        User user = userService.findUserByEmail(username);
        var twoFactorAuth = repository.findByUserAndType2FA(user, Type2FA.valueOf(type2fa.toUpperCase()))
                .orElseThrow(() -> new BusinessRuleException("[ERROR] Code or Type two factor auth is invalid!"));

        if(twoFactorAuth.getType2FA().equals(Type2FA.AUTHENTICATOR)){
            totpAuthenticator.validationAuthenticatorCode(code, twoFactorAuth);
        } else if (twoFactorAuth.getType2FA().equals(Type2FA.EMAIL)) {
            otpEmail.validationSecurityCodeEmail(code);
        }

        if(!twoFactorAuth.getIsActive()){
            twoFactorAuth.setActive();
            repository.save(twoFactorAuth);
        }
        return twoFactorAuth;
    }

    private TwoFactorAuth priorityOrderTwoFactorAuth(User  user){
        if (!user.isTwoFactorAuthEnabled()) {
            throw new BusinessRuleException("there is no active two-factor authentication");
        }

        //primeiro denifir uma ordem de prioridade e depois pegar o tipo de a2f com base na ordem
        Comparator<TwoFactorAuth> priorityComparator = Comparator.comparingInt(a2f -> {
            if (a2f.getType2FA().equals(Type2FA.AUTHENTICATOR)) {
                return 0;
            } else if (a2f.getType2FA().equals(Type2FA.EMAIL)) {
                return 1;
            }
            return Integer.MAX_VALUE;
        });

        return user.getTwoFactorAuths().stream()
                .filter(Objects::nonNull)
                .filter(TwoFactorAuth::getIsActive)
                .sorted(priorityComparator)
                .findFirst()
                .orElseThrow(() -> new BudgetMasterSecurityException("[INTERNAL ERROR]: No active two-factor authentication with recognized priority found."));
    }

}
