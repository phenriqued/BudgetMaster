package phenriqued.BudgetMaster.Services.Security.TokensService;


import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import phenriqued.BudgetMaster.DTOs.Security.Token.TokenDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterSecurityException;
import phenriqued.BudgetMaster.Infra.Security.Service.JWTService;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.FamilyEntity.Family;
import phenriqued.BudgetMaster.Models.Security.Token.SecurityUserToken;
import phenriqued.BudgetMaster.Models.Security.Token.TokenType;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.Security.SecurityUserTokenRepository;
import phenriqued.BudgetMaster.Services.Security.SecurityUserTokensService.SecurityUserTokenService;

import java.util.List;
import java.util.Objects;

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

    public String generatedSecurityUserToken2FA(User user){
        return securityUserTokenService.generatedSecurityUserToken2FA(user, TokenType.OPEN_ID, 5);
    }

    public String generatedTokenJwtAtFamily(User user, Family family, Long roleId){
        return jwtService.generatedJwtAtFamily(user, family, roleId);
    }

    public String validationTokenJWT(String tokenJwt){
        return jwtService.tokenJWTValidation(tokenJwt);
    }

    public void verifySecurityUserToken(String code) throws BudgetMasterSecurityException{
        securityUserTokenService.tokenValidation(code);
    }

    public DecodedJWT validationTokenJwtAtFamily(String token){
        return jwtService.tokenJWTFamilyValidation(token);
    }

    @Transactional
    public User redeemSecurityUserToken(String code) throws BudgetMasterSecurityException{
        var token = findByToken(code);
        var user = token.getUser();
        securityUserTokenService.tokenValidation(code);
        user.getSecurityUserTokens().remove(token);
        tokenRepository.delete(token);
        return user;
    }

    public SecurityUserToken findByToken(String token){
        return tokenRepository.findByToken(token)
                .orElseThrow(() -> new BudgetMasterSecurityException("[ERROR] invalid code or could not find token!"));
    }

    @Transactional
    public void deleteToken(SecurityUserToken token){
        if(!tokenRepository.existsById(token.getId())) throw new BudgetMasterSecurityException("invalid token!");
        tokenRepository.deleteById(token.getId());
    }

    @Transactional
    public void deleteAllTokensByUser(User user){
        if (Objects.isNull(user)) return;
        user.getSecurityUserTokens().clear();
    }

    @Transactional
    public void deleteAllTokensByUserExceptOpenID(User user){
        if (Objects.isNull(user)) return;

        List<SecurityUserToken> toKeep = user.getSecurityUserTokens().stream()
                .filter(t -> t.getTokenType().equals(TokenType.OPEN_ID))
                .toList();

        user.getSecurityUserTokens().clear(); // limpa tudo
        user.getSecurityUserTokens().addAll(toKeep);
    }


}
