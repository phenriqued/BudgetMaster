package phenriqued.BudgetMaster.Services.Security.TwoFactorAuthServices;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.DTOs.Login.SignInDTO;
import phenriqued.BudgetMaster.DTOs.Security.TwoFactorAuth.Request2faActiveDTO;
import phenriqued.BudgetMaster.DTOs.Security.TwoFactorAuth.RequestValid2faDTO;
import phenriqued.BudgetMaster.Infra.Email.TwoFactorAuthEmailService;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterSecurityException;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Models.Security.TwoFactorAuthentication.TwoFactorAuth;
import phenriqued.BudgetMaster.Models.Security.TwoFactorAuthentication.Type2FA;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.Security.TwoFactorAuthRepository;
import phenriqued.BudgetMaster.Services.UserServices.UserService;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class TwoFactorAuthService {

    private final TwoFactorAuthRepository repository;
    private final TwoFactorAuthEmailService factorAuthEmailService;
    private final UserService userService;

    public void createTwoFactorAuth(Request2faActiveDTO request2faDTO, User user) {
        var type2fa = Type2FA.valueOf(request2faDTO.type2FA());
        if(repository.existsByUserAndType2FA(user, type2fa))
            throw new BusinessRuleException("there is already an active two-factor authentication of type "+type2fa);

        var entity2fa = new TwoFactorAuth(user, type2fa, generatedSecureSecret(type2fa), generatedExpirationAt(type2fa));
        System.out.println(entity2fa.getType2FA());
        repository.save(entity2fa);
        factorAuthEmailService.sendVerificationEmail(user, entity2fa);
    }
    public void resendActivatedTwoFactorAuth(String code){
        var twoFactorAuth = repository.findBySecret(code)
                .orElseThrow(()-> new BusinessRuleException("Invalid code, cannot find code!"));
        try{
            validationExpirationCode(twoFactorAuth);
            var remainingTerm = twoFactorAuth.getExpirationAt().getMinute() - LocalDateTime.now().getMinute();
            throw new BusinessRuleException("The code sent is still valid. You can request a new code only after "+ remainingTerm +" minutes or when the current code expires.");
        }catch (BudgetMasterSecurityException e){
            twoFactorAuth.setSecret(generatedSecureSecret(twoFactorAuth.getType2FA()));
            twoFactorAuth.setExpirationAt(generatedExpirationAt(twoFactorAuth.getType2FA()));
            repository.save(twoFactorAuth);
            factorAuthEmailService.sendVerificationEmail(twoFactorAuth.getUser(), twoFactorAuth);
        }
    }

    public void generatedCodeTwoFactorAuth(SignInDTO data){
        User user = userService.findUserByEmail(data.email());
        var twoFactorAuth = user.priorityOrderTwoFactorAuth();
        if(twoFactorAuth.getType2FA().equals(Type2FA.AUTHENTICATOR)){
            //gerar Secret
        }else {
            twoFactorAuth.setSecret(generatedSecureSecret(twoFactorAuth.getType2FA()));
            twoFactorAuth.setExpirationAt(generatedExpirationAt(twoFactorAuth.getType2FA()));
            repository.save(twoFactorAuth);
            factorAuthEmailService.sendTwoFactorAuthentication(user, twoFactorAuth, data);
        }
    }
    public TwoFactorAuth validationAndActivationTwoFactorAuth(RequestValid2faDTO requestValid2faDTO){
        var twoFactorAuth = repository.findBySecret(requestValid2faDTO.code())
                .orElseThrow(() -> new BusinessRuleException("invalid code, check the code entered."));

        if(twoFactorAuth.getType2FA().equals(Type2FA.EMAIL)){
            validationExpirationCode(twoFactorAuth);
        }


        if(!twoFactorAuth.getIsActive()){
            twoFactorAuth.setActive();
            repository.save(twoFactorAuth);
        }
        return twoFactorAuth;
    }


    private String generatedSecureSecret(Type2FA type2FA){
        if (type2FA.equals(Type2FA.EMAIL)){
            SecureRandom secureRandom = new SecureRandom();
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i<6; i++){
                stringBuilder.append(secureRandom.nextInt(10));
            }
            return stringBuilder.toString();
        }else if (type2FA.equals(Type2FA.AUTHENTICATOR)){
            return "...";
        }
        return "...";
    }
    private LocalDateTime generatedExpirationAt(Type2FA type2FA){
        return type2FA.equals(Type2FA.EMAIL) ? LocalDateTime.now().plusMinutes(5) : null;
    }
    private void validationExpirationCode(TwoFactorAuth twoFactorAuth){
        if (twoFactorAuth.getExpirationAt() == null) return;
        if (LocalDateTime.now().isAfter(twoFactorAuth.getExpirationAt()))
            throw new BudgetMasterSecurityException("Invalid token! The code expiration time has been exceeded.");
    }
}
