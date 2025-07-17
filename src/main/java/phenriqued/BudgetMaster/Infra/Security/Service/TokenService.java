package phenriqued.BudgetMaster.Infra.Security.Service;


import lombok.AllArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import phenriqued.BudgetMaster.DTOs.Token.TokenDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterSecurityException;
import phenriqued.BudgetMaster.Infra.Security.Token.SecurityUserToken;
import phenriqued.BudgetMaster.Infra.Security.Token.TokenType;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.SecurityData.SecurityUserTokenRepository;

import java.util.Objects;
import java.util.logging.Logger;

@Service
@AllArgsConstructor
public class TokenService {

    private final JWTService jwtService;
    private final SecurityUserTokenService securityUserTokenService;
    private final SecurityUserTokenRepository tokenRepository;

    public TokenDTO generatedRefreshTokenAndTokenJWT(UserDetailsImpl user, TokenType tokenType, String deviceIdentifier) {
        var tokenJWT = jwtService.generatedTokenJWT(user);
        var refreshToken = securityUserTokenService.generatedRefreshToken(user, tokenType, deviceIdentifier);
        return new TokenDTO(tokenJWT, refreshToken);
    }

    public String validationTokenJWT(String tokenJwt){
        return jwtService.tokenJWTValidation(tokenJwt);
    }

    public void verifySecurityUserToken(String code) throws BudgetMasterSecurityException{
        securityUserTokenService.tokenValidation(code);
    }

    public SecurityUserToken findByToken(String token){
        return tokenRepository.findByToken(token)
                .orElseThrow(() -> new BudgetMasterSecurityException("[ERROR] invalid code or could not find token!"));
    }

    @Transactional
    public void deleteToken(SecurityUserToken token){
        if(!tokenRepository.existsById(token.getId())) throw new BudgetMasterSecurityException("invalid token!");
        tokenRepository.delete(token);
    }

    @Transactional
    public void deleteAllTokensByUser(User user){
        if (Objects.isNull(user)){
            Logger logger = (Logger) LoggerFactory.getLogger(TokenService.class);
            logger.warning("Unable to delete tokens because user is null.");
            return;
        }
        tokenRepository.deleteAllByUser(user);
    }



}
