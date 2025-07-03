package phenriqued.BudgetMaster.Infra.Exceptions.Exception;

public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
