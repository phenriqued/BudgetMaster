package phenriqued.BudgetMaster.Infra.Exceptions.ErrorsHandlers;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import phenriqued.BudgetMaster.DTOs.ExceptionHandler.DataErrorValidationDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterSecurityException;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;
import java.util.List;

@RestControllerAdvice
public class ExceptionsHandlers {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<DataErrorValidationDTO>> handlerMethodArgumentNotValidException(MethodArgumentNotValidException e){
        var errors = e.getFieldErrors();
        return ResponseEntity.badRequest().body(
                errors.stream().map(DataErrorValidationDTO::new).toList());
    }
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<String> handlerBusinessRuleException(BusinessRuleException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handlerAllException(Exception e){
        System.out.println("Tracker:\n"+e.getStackTrace()+"\nDefault Message: "+e.getMessage()+"\nClass: "+e.getClass());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("[INTERNAL ERROR]");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handler404NotFound(NoResourceFoundException e){
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handlerDataIntegrityViolationException(DataIntegrityViolationException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body("[ERROR] Unique index or primary key violation: The resource you tried to create already exists.");
    }

    @ExceptionHandler(BudgetMasterSecurityException.class)
    public ResponseEntity<String> handlerBudgetMasterSecurityException(BudgetMasterSecurityException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handlerAuthenticationException(AuthenticationException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handlerAccessDeniedException(AccessDeniedException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<String> handlerDisabledException(DisabledException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handlerBadCredentialsException(BadCredentialsException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
    }



}
