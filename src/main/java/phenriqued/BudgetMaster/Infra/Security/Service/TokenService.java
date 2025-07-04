package phenriqued.BudgetMaster.Infra.Security.Service;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.DTOs.Token.TokenDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterSecurityException;
import phenriqued.BudgetMaster.Infra.Security.Token.RefreshToken;
import phenriqued.BudgetMaster.Infra.Security.Token.TokenType;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Repositories.SecurityData.RefreshTokenRepository;

@Service
@AllArgsConstructor
public class TokenService {

    private final JWTService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository tokenRepository;

    public TokenDTO generatedTokens(UserDetailsImpl user, TokenType tokenType, String deviceIdentifier) {
        var tokenJWT = jwtService.generatedTokenJWT(user);
        var refreshToken = refreshTokenService.generatedRefreshToken(user, tokenType, deviceIdentifier);
        return new TokenDTO(tokenJWT, refreshToken);
    }

    public void verifyToken(String code){
        refreshTokenService.tokenValidation(code);
    }

    public RefreshToken findByToken(String token){
        return tokenRepository.findByToken(token)
                .orElseThrow(() -> new BudgetMasterSecurityException("[ERROR] invalid code or could not find token!"));
    }

    public void deleteToken(RefreshToken token){
        if(!tokenRepository.existsById(token.getId())) throw new BudgetMasterSecurityException("invalid token!");
        tokenRepository.delete(token);
    }



}
