package phenriqued.BudgetMaster.Infra.Security.PasswordValidation.Validations;

import org.springframework.stereotype.Component;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;

@Component
public class UppercaseAndLowercaseValid implements Validator {

    @Override
    public void passwordValidator(String password) {
        System.out.println("UppercaseAndLowercaseValid");
        if (password.equals(password.toLowerCase())) throw new BusinessRuleException("The password should contain at least 1 uppercase character");

        if (password.equals(password.toUpperCase())) throw new BusinessRuleException("The password should contain at least 1 lower character");
    }
}
