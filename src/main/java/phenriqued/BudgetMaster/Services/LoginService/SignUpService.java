package phenriqued.BudgetMaster.Services.LoginService;


import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import phenriqued.BudgetMaster.DTOs.Login.RegisterUserDTO;
import phenriqued.BudgetMaster.DTOs.Security.Token.RequestTokenDTO;
import phenriqued.BudgetMaster.DTOs.Security.Token.TokenDTO;
import phenriqued.BudgetMaster.Infra.Email.SecurityEmailService;
import phenriqued.BudgetMaster.Infra.Email.UserEmailService;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterSecurityException;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.Security.Token.SecurityUserToken;
import phenriqued.BudgetMaster.Models.Security.Token.TokenType;
import phenriqued.BudgetMaster.Models.UserEntity.Role.Role;
import phenriqued.BudgetMaster.Models.UserEntity.Role.RoleName;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.RoleRepository.RoleRepository;
import phenriqued.BudgetMaster.Repositories.UserRepository.UserRepository;
import phenriqued.BudgetMaster.Services.Security.TokensService.TokenService;

@Service
@AllArgsConstructor
public class SignUpService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final TokenService tokenService;
    private final SecurityEmailService emailService;
    private final UserEmailService userEmailService;

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
        tokenService.verifySecurityUserToken(token.getToken());
        var user = token.getUser();
        user.setIsActive();
        userRepository.save(user);
        tokenService.deleteToken(token);
        userEmailService.sendActivateAccount(user);
        return tokenService.generatedRefreshTokenAndTokenJWT(new UserDetailsImpl(user), TokenType.valueOf(requestTokenDTO.tokenType().toUpperCase()),
                                                requestTokenDTO.identifier());
    }

    public Boolean resendCodeActivateUser(String code){
        SecurityUserToken token = tokenService.findByToken(code);
        try{
            tokenService.verifySecurityUserToken(token.getToken());
            return false;
        }catch (BusinessRuleException e){
            emailService.sendVerificationEmail(token.getUser());
            return true;
        }
    }

}
