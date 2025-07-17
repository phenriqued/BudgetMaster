package phenriqued.BudgetMaster.Infra.Security.Service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterSecurityException;
import phenriqued.BudgetMaster.Infra.Security.Token.SecurityUserToken;
import phenriqued.BudgetMaster.Infra.Security.Token.TokenType;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.SecurityData.SecurityUserTokenRepository;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@AllArgsConstructor
public class SecurityUserTokenService {

    private final SecurityUserTokenRepository repository;

    @Transactional
    public String generatedRefreshToken(UserDetailsImpl userDetails, TokenType tokenType, String deviceIdentifier){
        var user = userDetails.getUser();
        var expirationToken = LocalDateTime.now().plusHours(2);
        var token = repository.findByIdentifierAndUser(deviceIdentifier, user).orElse(null);
        if(Objects.isNull(token)){
            token = repository.save(new SecurityUserToken(user, tokenType, deviceIdentifier, expirationToken));
        }else{
            token.attToken(expirationToken);
            repository.save(token);
        }
        return token.getToken();
    }

    @Transactional
    public String generatedActivationToken(User user){
        String identifier = "internal-activation-user-"+user.getId();
        LocalDateTime expirationToken = LocalDateTime.now().plusMinutes(10);
        var token = repository.findByIdentifierAndUser(identifier, user);
        if(token.isPresent()){
            token.get().attToken(expirationToken);
            return repository.save(token.get()).getToken();
        }
        return repository.save(new SecurityUserToken(user, TokenType.USER_ACTIVATION,
                identifier, expirationToken)).getToken();
    }

    @Transactional
    public String generatedSecurityUserToken(User user, String identifier, Integer expirationTime, TokenType tokenType){
        LocalDateTime expirationToken = LocalDateTime.now().plusMinutes(expirationTime);
        var token = repository.findByIdentifierAndUser(identifier, user);
        if(token.isPresent()){
            token.get().attToken(expirationToken);
            return repository.save(token.get()).getToken();
        }
        return repository.save(new SecurityUserToken(user, tokenType, identifier, expirationToken)).getToken();
    }


    public void tokenValidation(String token) throws BudgetMasterSecurityException{
        var tokenVerification = repository.findByToken(token)
                .orElseThrow(() -> new BudgetMasterSecurityException("Unable to verify a non-existent token, please verify the token"));

        if(LocalDateTime.now().isAfter(tokenVerification.getExpirationToken()))
            throw new BudgetMasterSecurityException("Invalid token! The token expiration time has been exceeded.");

    }


}
