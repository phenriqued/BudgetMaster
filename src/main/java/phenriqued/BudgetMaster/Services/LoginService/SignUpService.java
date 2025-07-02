package phenriqued.BudgetMaster.Services.LoginService;

import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.DTOs.Login.RegisterUserDTO;
import phenriqued.BudgetMaster.DTOs.Token.TokenDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterSecurityException;
import phenriqued.BudgetMaster.Infra.Security.Service.TokenService;
import phenriqued.BudgetMaster.Infra.Security.Token.DeviceType;
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

    public TokenDTO registerUser(RegisterUserDTO registerUserDTO) {
        String password = encoder.encode(registerUserDTO.password());
        Role role = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new BudgetMasterSecurityException("[ERROR] error role user."));
        var newUser = userRepository.save(new User(registerUserDTO, password, role));
        return tokenService.generatedTokens(new UserDetailsImpl(newUser), DeviceType.valueOf(registerUserDTO.deviceType().toUpperCase()),
                registerUserDTO.deviceIdentifier());
    }
}
