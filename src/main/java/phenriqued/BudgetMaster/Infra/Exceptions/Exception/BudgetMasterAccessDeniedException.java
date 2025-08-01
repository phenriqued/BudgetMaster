package phenriqued.BudgetMaster.Infra.Exceptions.Exception;

public class BudgetMasterAccessDeniedException extends RuntimeException {
    public BudgetMasterAccessDeniedException(String message) {
        super(message);
    }
}
