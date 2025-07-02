package phenriqued.BudgetMaster.Infra.Security.Service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.Infra.Security.Token.DeviceType;
import phenriqued.BudgetMaster.Infra.Security.Token.RefreshToken;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Repositories.SecurityData.RefreshTokenRepository;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;

    public String generatedRefreshToken(UserDetailsImpl userDetails, DeviceType deviceType, String deviceIdentifier){
        var user = userDetails.getUser();
        var token =  UUID.randomUUID().toString().replaceAll("-", "");
        var expirationToken = LocalDateTime.now().plusHours(2);
        var existsToken = repository.findByDeviceIdentifierAndUser(deviceIdentifier, user).orElse(null);
        if(Objects.isNull(existsToken)){
            repository.save(new RefreshToken(user, token,
                    deviceType, deviceIdentifier, expirationToken));
        }else{
            existsToken.attToken(token, expirationToken);
            repository.save(existsToken);
        }

        return token;
    }


}
