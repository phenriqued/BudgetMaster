package phenriqued.BudgetMaster.Services.UserServices;

import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.Repositories.UserRepository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }



}
