package phenriqued.BudgetMaster.Infra.Security.PasswordValidation.Validations;

import org.springframework.stereotype.Component;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HasSpecialCharacter implements Validator{

    @Override
    public void passwordValidator(String password) {
        System.out.println("HasSpecialCharacter");
        Pattern pattern = Pattern.compile("[!@#$%&*_+\\-.?]");
        Matcher matcher = pattern.matcher(password);
        if (!matcher.find()) throw new BusinessRuleException("the password must have at least one special character: !@#$%&*_+\\-.?");
    }
}
