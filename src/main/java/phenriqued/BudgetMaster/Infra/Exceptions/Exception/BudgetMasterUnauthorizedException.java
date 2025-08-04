package phenriqued.BudgetMaster.Infra.Exceptions.Exception;

public class BudgetMasterUnauthorizedException extends RuntimeException {
    public BudgetMasterUnauthorizedException(String message) {
        super(message);
    }
}
