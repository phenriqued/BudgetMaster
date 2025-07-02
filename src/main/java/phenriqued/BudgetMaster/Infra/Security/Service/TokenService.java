package phenriqued.BudgetMaster.Infra.Security.Service;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.DTOs.Token.TokenDTO;
import phenriqued.BudgetMaster.Infra.Security.Token.DeviceType;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;

@Service
@AllArgsConstructor
public class TokenService {

    private final JWTService jwtService;
    private final RefreshTokenService refreshTokenService;

    public TokenDTO generatedTokens(UserDetailsImpl user, DeviceType deviceType, String deviceIdentifier) {
        var tokenJWT = jwtService.generatedTokenJWT(user);
        var refreshToken = refreshTokenService.generatedRefreshToken(user, deviceType, deviceIdentifier);
        return new TokenDTO(tokenJWT, refreshToken);
    }



}
