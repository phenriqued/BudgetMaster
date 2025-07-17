package phenriqued.BudgetMaster.Infra.Security.PasswordValidation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<PasswordValid, String> {

    private final static String PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[*@%!?#$&-_])(?=\\S+$).{6,}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(value == null || value.trim().isBlank()){
            return false;
        }
        return value.matches(PASSWORD_PATTERN);
    }


}
