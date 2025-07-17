package phenriqued.BudgetMaster.Infra.Security.PasswordValidation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Infra.Security.PasswordValidation.Validations.Validator;

import java.util.List;

public class PasswordValidator implements ConstraintValidator<PasswordValid, String> {

    @Autowired
    private List<Validator> passwordValidations;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(value == null || value.trim().isBlank()){
            return false;
        }

        try {
            passwordValidations.forEach(valid -> valid.passwordValidator(value));
        }catch (BusinessRuleException | ValidationException e){
            return false;
        }
        return true;
    }


}
