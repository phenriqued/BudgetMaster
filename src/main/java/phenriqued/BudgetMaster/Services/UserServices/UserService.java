package phenriqued.BudgetMaster.Services.UserServices;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.DTOs.Security.PasswordDTOs.RequestConfirmationPasswordUser;
import phenriqued.BudgetMaster.DTOs.Security.PasswordDTOs.RequestOnlyNewPasswordChangeDTO;
import phenriqued.BudgetMaster.DTOs.Security.PasswordDTOs.RequestPasswordChangeUserDTO;
import phenriqued.BudgetMaster.Infra.Email.UserEmailService;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterSecurityException;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Services.Security.TokensService.TokenService;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Repositories.UserRepository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserEmailService userEmailService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public UserService(UserRepository userRepository, UserEmailService userEmailService, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userRepository = userRepository;
        this.userEmailService = userEmailService;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public void changePassword(RequestPasswordChangeUserDTO passwordChangeUserDTO, String userAuthenticated) {
        User user = findUserByEmail(userAuthenticated);
        matchesCurrentPassword(passwordChangeUserDTO.password(), user);

        newPasswordCheck(passwordChangeUserDTO.newPassword(), user.getPassword());
        user.setPassword(passwordEncoder.encode(passwordChangeUserDTO.newPassword()));
        userRepository.save(user);
        userEmailService.sendChangedPassword(user);
    }

    public String changePasswordToActivateAccount(String code, RequestOnlyNewPasswordChangeDTO newPasswordChangeDTO){
        var token = tokenService.findByToken(code);
        User user = token.getUser();
        user.setIsActive();
        user.setDeleteAt();
        newPasswordCheck(newPasswordChangeDTO.newPassword(), user.getPassword());
        user.setPassword(passwordEncoder.encode(newPasswordChangeDTO.newPassword()));
        tokenService.deleteAllTokensByUser(user);
        userRepository.save(user);
        userEmailService.sendChangedPassword(user);
        return "http://localhost:8080/login/signin";
    }

    public void disableUser(RequestConfirmationPasswordUser confirmationPassword, User user) {
        matchesCurrentPassword(confirmationPassword.password(), user);
        user.setIsActive();
        user.setDeleteAt();
        userEmailService.sendDisableUserEmail(user);
        tokenService.deleteAllTokensByUserExceptOpenID(user);
        userRepository.save(user);
    }

    public void activateAccount(String email){
        var userActivate = findUserByEmail(email);
        userActivate.setIsActive();
        userActivate.setDeleteAt();
        userEmailService.sendActivateAccount(userActivate);
        tokenService.deleteAllTokensByUser(userActivate);
        userRepository.save(userActivate);
    }

    public User findUserByEmail(String email){
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User cannot be found"));
    }

    private void matchesCurrentPassword(String confirmationPasswordUser, User user){
        if(!passwordEncoder.matches(confirmationPasswordUser, user.getPassword())){
            throw new BudgetMasterSecurityException("Invalid Password!");
        }
    }
    private void newPasswordCheck(String newPassword, String password){
        if(passwordEncoder.matches(newPassword, password)){
            throw new BusinessRuleException("New password cannot match current password!");
        }
    }

}
