package phenriqued.BudgetMaster.Services.LoginService;


import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import phenriqued.BudgetMaster.DTOs.Login.RegisterUserDTO;
import phenriqued.BudgetMaster.DTOs.Token.RequestTokenDTO;
import phenriqued.BudgetMaster.DTOs.Token.TokenDTO;
import phenriqued.BudgetMaster.Infra.Email.SecurityEmailService;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterSecurityException;
import phenriqued.BudgetMaster.Infra.Security.Service.TokenService;
import phenriqued.BudgetMaster.Infra.Security.Token.RefreshToken;
import phenriqued.BudgetMaster.Infra.Security.Token.TokenType;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.UserEntity.Role.Role;
import phenriqued.BudgetMaster.Models.UserEntity.Role.RoleName;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.RoleRepository.RoleRepository;
import phenriqued.BudgetMaster.Repositories.UserRepository.UserRepository;

@Service
@AllArgsConstructor
public class SignUpService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final TokenService tokenService;
    private final SecurityEmailService emailService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registerUser(RegisterUserDTO registerUserDTO) {
        String password = encoder.encode(registerUserDTO.password());
        Role role = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new BudgetMasterSecurityException("[INTERNAL ERROR] error role user."));
        var newUser = userRepository.save(new User(registerUserDTO, password, role));
        emailService.sendVerificationEmail(newUser);
    }

    @Transactional
    public TokenDTO activateUser(String code, RequestTokenDTO requestTokenDTO) {
        var token = tokenService.findByToken(code);
        tokenService.verifyToken(token.getToken());
        var user = token.getUser();
        user.setIsActive();
        userRepository.save(user);
        tokenService.deleteToken(token);
        return tokenService.generatedTokens(new UserDetailsImpl(user), TokenType.valueOf(requestTokenDTO.tokenType().toUpperCase()),
                                                requestTokenDTO.identifier());
    }

    public Boolean resendCodeActivateUser(String code){
        RefreshToken token = tokenService.findByToken(code);
        try{
            tokenService.verifyToken(token.getToken());
            return false;
        }catch (BudgetMasterSecurityException e){
            emailService.sendVerificationEmail(token.getUser());
            return true;
        }
    }

}
