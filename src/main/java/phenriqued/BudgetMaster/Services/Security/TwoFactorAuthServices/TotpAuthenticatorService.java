package phenriqued.BudgetMaster.Services.Security.TwoFactorAuthServices;

import com.atlassian.onetime.core.TOTPGenerator;
import com.atlassian.onetime.model.TOTPSecret;
import com.atlassian.onetime.service.RandomSecretProvider;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Models.Security.TwoFactorAuthentication.TwoFactorAuth;
import phenriqued.BudgetMaster.Models.Security.TwoFactorAuthentication.Type2FA;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.Security.TwoFactorAuthRepository;

@Service
@AllArgsConstructor
class TotpAuthenticatorService {

    private final TwoFactorAuthRepository repository;

    public String generatedAuthenticatorQrCode(User user){
        String secret = new RandomSecretProvider().generateSecret().getBase32Encoded();
        repository.save(new TwoFactorAuth(user, Type2FA.AUTHENTICATOR, secret, null));
        return generatedUrl(user.getEmail(), secret);
    }

    public void validationAuthenticatorCode(String code, TwoFactorAuth secret){
        TOTPSecret decodedSecret = TOTPSecret.Companion.fromBase32EncodedString(secret.getSecret());
        var applicationCode = new TOTPGenerator().generateCurrent(decodedSecret).getValue();
        if (!applicationCode.equals(code)) throw new BusinessRuleException("[ERROR] Invalid Code!");
    }


    private String generatedUrl(String username, String secret) {
        var issuer = "Budget Master";
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer, username, secret, issuer
        );
    }

}
