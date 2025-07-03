package phenriqued.BudgetMaster.Infra.Security.Service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import phenriqued.BudgetMaster.Infra.Security.Token.DeviceType;
import phenriqued.BudgetMaster.Infra.Security.Token.RefreshToken;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.SecurityData.RefreshTokenRepository;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;

    @Transactional
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

    public String generatedActivationToken(User user){
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        String deviceIdentifier = "internal-activation-user-"+user.getId();
        return repository.save( new RefreshToken(user, token, DeviceType.USER_ACTIVATION,
                deviceIdentifier, LocalDateTime.now().plusMinutes(10))).getToken();
    }


}
