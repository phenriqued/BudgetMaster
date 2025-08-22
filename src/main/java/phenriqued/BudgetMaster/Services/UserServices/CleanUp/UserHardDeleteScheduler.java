package phenriqued.BudgetMaster.Services.UserServices.CleanUp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import phenriqued.BudgetMaster.Infra.Email.UserEmailService;
import phenriqued.BudgetMaster.Repositories.UserRepository.UserRepository;
import phenriqued.BudgetMaster.Services.FamilyService.FamilyConfigService;

import java.time.LocalDateTime;


@Component
@EnableScheduling
public class UserHardDeleteScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserHardDeleteScheduler.class);
    private final long HOUR = 60000 * 60;

    private final UserRepository userRepository;
    private final UserEmailService userEmailService;
    private final FamilyConfigService familyConfigService;

    public UserHardDeleteScheduler(UserRepository userRepository, UserEmailService userEmailService, FamilyConfigService familyConfigService) {
        this.userRepository = userRepository;
        this.userEmailService = userEmailService;
        this.familyConfigService = familyConfigService;
    }

    @Transactional
    @Scheduled(fixedDelay = HOUR)
    void automaticHardDeleteUser(){
        LOGGER.info("Starting automatic scanning and deletion of disabled users!");

        userRepository.findByDeleteAtIsNotNull().ifPresent(
                userList -> userList.stream().filter(user -> expirationDate(user.getDeleteAt()))
                        .forEach(user -> {
                            familyConfigService.hardDeleteUserFamily(user);
                            user.getSecurityUserTokens().clear();
                            userRepository.deleteById(user.getId());
                            userEmailService.sendHardDeleteUser(user);
                        })
        );

        LOGGER.info("Finishing automatic scanning and deletion of disabled users!");
    }

    private Boolean expirationDate(LocalDateTime deleteAt){
        return deleteAt.isBefore(LocalDateTime.now());
    }

}
