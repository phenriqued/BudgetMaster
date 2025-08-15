package phenriqued.BudgetMaster.Infra.Security.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterSecurityException;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterUnauthorizedException;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.FamilyEntity.Family;
import phenriqued.BudgetMaster.Models.UserEntity.User;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class JWTService {

    @Value("${ALGORITHM_JWT}")
    private String secret;

    public String generatedTokenJWT(UserDetailsImpl user){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("Budget_Master")
                    .withSubject(user.getUsername())
                    .withExpiresAt(expirationToken(30L))
                    .sign(algorithm);
        } catch (JWTCreationException exception){
            throw new BudgetMasterSecurityException("[ERROR] Error creating a JWT Token: " +exception.getMessage());
        }
    }

    public String generatedJwtAtFamily(User user, Family family, Long roleId){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("Budget_Master-Family")
                    .withSubject(user.getEmail())
                    .withExpiresAt(expirationToken(4320L))
                    .withClaim("familyId", family.getId())
                    .withClaim("roleId", roleId)
                    .sign(algorithm);
        } catch (JWTCreationException exception){
            throw new BudgetMasterSecurityException("[ERROR] Error creating a JWT Token: " +exception.getMessage());
        }
    }

    public String tokenJWTValidation(String tokenJwt){
        DecodedJWT decodedJWT;
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    // specify any specific claim validations
                    .withIssuer("Budget_Master")
                    .build();

            decodedJWT = verifier.verify(tokenJwt);
            return decodedJWT.getSubject();
        } catch (JWTVerificationException exception){
            throw new BudgetMasterUnauthorizedException(exception.getMessage());
        }
    }

    public DecodedJWT tokenJWTFamilyValidation(String tokenJwt){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("Budget_Master-Family")
                    .build();

            return verifier.verify(tokenJwt);
        } catch (JWTVerificationException exception){
            throw new BudgetMasterUnauthorizedException(exception.getMessage());
        }
    }


    private Instant expirationToken(Long minutes){
        return LocalDateTime.now().plusMinutes(minutes).toInstant(ZoneOffset.of("-03:00"));
    }

}
