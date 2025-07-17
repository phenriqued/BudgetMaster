package phenriqued.BudgetMaster.Infra.Security.PasswordValidation.Validations;

import org.springframework.stereotype.Component;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;

@Component
public class GreaterThanSixCharacters implements Validator{

    @Override
    public void passwordValidator(String password) {
        System.out.println("GreaterThanSixCharacters");
        if(password.length() < 6)
            throw new BusinessRuleException("Password must be longer than 6 characters");
    }
}
