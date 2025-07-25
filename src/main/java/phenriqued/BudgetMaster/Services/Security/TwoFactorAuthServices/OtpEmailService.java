package phenriqued.BudgetMaster.Services.Security.TwoFactorAuthServices;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.DTOs.Login.SignInDTO;
import phenriqued.BudgetMaster.Infra.Email.TwoFactorAuthEmailService;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterSecurityException;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Models.Security.TwoFactorAuthentication.TwoFactorAuth;
import phenriqued.BudgetMaster.Models.Security.TwoFactorAuthentication.Type2FA;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.Security.TwoFactorAuthRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
class OtpEmailService {

    private final TwoFactorAuthRepository repository;
    private final TwoFactorAuthEmailService factorAuthEmailService;


    public void createSecurityCode(User user){
        var code2fa = new TwoFactorAuth(user, Type2FA.EMAIL, generatedSecureSecretEmail(), LocalDateTime.now().plusMinutes(5));
        repository.save(code2fa);
        factorAuthEmailService.sendVerificationEmail(user, code2fa);
    }

    public void resendSecurityCode(User user){
        var otpEmail = findByUser(user);
        if (codeIsExpired(otpEmail)) {
            var remainingTerm = otpEmail.getExpirationAt().getMinute() - LocalDateTime.now().getMinute();
            throw new BusinessRuleException("The code sent is still valid. You can request a new code only after "+ remainingTerm +" minutes or when the current code expires.");
        }
        otpEmail.setSecret(generatedSecureSecretEmail());
        otpEmail.setExpirationAt(LocalDateTime.now().plusMinutes(5));
        repository.save(otpEmail);
        factorAuthEmailService.sendVerificationEmail(user, otpEmail);
    }

    public void generatedSecurityCodeTwoFactorAuth(User user, SignInDTO data){
        var otpEmail = findByUser(user);
        otpEmail.setSecret(generatedSecureSecretEmail());
        otpEmail.setExpirationAt(LocalDateTime.now().plusMinutes(5));
        repository.save(otpEmail);
        factorAuthEmailService.sendTwoFactorAuthentication(user, otpEmail, data);
    }

    public void validationSecurityCodeEmail(String secret){
        var otpEmail = repository.findBySecret(secret)
                .orElseThrow(() -> new BusinessRuleException("invalid code, check the code entered."));
        if (codeIsExpired(otpEmail)) throw new BudgetMasterSecurityException("Invalid token! The code expiration time has been exceeded.");
    }


    private TwoFactorAuth findByUser(User user){
        return repository.findByUserAndType2FA(user, Type2FA.EMAIL)
                .orElseThrow(() -> new BusinessRuleException("the code cannot be found!"));
    }
    private String generatedSecureSecretEmail(){
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i<6; i++) {
            stringBuilder.append(secureRandom.nextInt(10));
        }
        return stringBuilder.toString();
    }
    private Boolean codeIsExpired(TwoFactorAuth twoFactorAuth){
        if (twoFactorAuth.getExpirationAt() == null) return true;
        return LocalDateTime.now().isAfter(twoFactorAuth.getExpirationAt());
    }
}
