package phenriqued.BudgetMaster.Services.LoginService;


import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import phenriqued.BudgetMaster.DTOs.Login.RegisterUserDTO;
import phenriqued.BudgetMaster.DTOs.Token.RequestTokenDTO;
import phenriqued.BudgetMaster.DTOs.Token.TokenDTO;
import phenriqued.BudgetMaster.Infra.Email.EmailService;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterSecurityException;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Infra.Security.Service.TokenService;
import phenriqued.BudgetMaster.Infra.Security.Token.DeviceType;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Models.UserEntity.Role.Role;
import phenriqued.BudgetMaster.Models.UserEntity.Role.RoleName;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.RoleRepository.RoleRepository;
import phenriqued.BudgetMaster.Repositories.SecurityData.RefreshTokenRepository;
import phenriqued.BudgetMaster.Repositories.UserRepository.UserRepository;

@Service
@AllArgsConstructor
public class SignUpService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository tokenRepository;
    private final PasswordEncoder encoder;
    private final TokenService tokenService;
    private final EmailService emailService;

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
        var token = tokenRepository.findByToken(code).orElseThrow(() -> new BusinessRuleException("[ERROR] invalid code!"));
        var user = token.getUser();
        user.setIsActive();
        userRepository.save(user);
        tokenRepository.delete(token);
        return tokenService.generatedTokens(new UserDetailsImpl(user), DeviceType.valueOf(requestTokenDTO.deviceType().toUpperCase()),
                                                requestTokenDTO.deviceIdentifier());
    }
}
