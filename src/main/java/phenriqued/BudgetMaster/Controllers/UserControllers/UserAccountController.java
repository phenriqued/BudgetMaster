package phenriqued.BudgetMaster.Controllers.UserControllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import phenriqued.BudgetMaster.Services.UserServices.UserService;

@RestController
@RequestMapping("account/manager")
public class UserAccountController {

    private final UserService userService;

    public UserAccountController(UserService userService) {
        this.userService = userService;
    }


}
