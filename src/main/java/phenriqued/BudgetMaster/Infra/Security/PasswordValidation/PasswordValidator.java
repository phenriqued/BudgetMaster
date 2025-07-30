package phenriqued.BudgetMaster.Infra.Security.PasswordValidation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


/**
 * <p>Implementação de {@link ConstraintValidator} para validar senhas.</p>
 *
 * <p>Esta classe garante que a senha fornecida atenda a critérios de segurança específicos,
 * definidos pelo padrão de expressão regular {@code PASSWORD_PATTERN}.</p>
 *
 * <h3>Critérios de Validação da Senha:</h3>
 * <ul>
 * <li>Deve conter no mínimo uma letra maiúscula (<code>A-Z</code>).</li>
 * <li>Deve conter no mínimo um dígito numérico (<code>0-9</code>).</li>
 * <li>Deve conter no mínimo um caractere especial (<code>*@%!?#$&-_</code>).</li>
 * <li>Não deve conter espaços em branco (<code>\S+</code>).</li>
 * <li>Deve ter um comprimento mínimo de 6 caracteres.</li>
 * </ul>
 *
 * <p>Se a senha for nula, vazia ou contiver apenas espaços em branco, a validação falhará.</p>
 */
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
