package phenriqued.BudgetMaster.DTOs.ExceptionHandler;

import org.springframework.validation.FieldError;

public record DataErrorValidationDTO(
        String fieldError,
        String defaultMessage) {

    public DataErrorValidationDTO(FieldError error){
        this(error.getField(), error.getDefaultMessage());
    }
}
