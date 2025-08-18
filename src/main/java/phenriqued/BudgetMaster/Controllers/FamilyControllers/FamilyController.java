package phenriqued.BudgetMaster.Controllers.FamilyControllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import phenriqued.BudgetMaster.DTOs.Family.RequestCreateFamilyDTO;
import phenriqued.BudgetMaster.DTOs.Family.ResponseAllFamiliesDTO;
import phenriqued.BudgetMaster.DTOs.Family.ResponseGetFamilyDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Services.FamilyService.FamilyService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/family")
public class FamilyController {

    private final FamilyService service;

    public FamilyController(FamilyService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ResponseAllFamiliesDTO>> getAllFamily(@AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseEntity.ok(service.getAllFamiliesByUser(userDetails));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseGetFamilyDTO> getFamilyById(@PathVariable(value = "id") Long id, @AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseEntity.ok(service.getFamilyById(id, userDetails));
    }

    @PostMapping
    public ResponseEntity<?> createFamily(@RequestBody @Valid RequestCreateFamilyDTO createFamilyDTO, @AuthenticationPrincipal UserDetailsImpl userDetails){
        try{
            var data = service.createFamily(userDetails, createFamilyDTO);
            URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/add/{id}").buildAndExpand(data.id()).toUri();
            return ResponseEntity.created(uri).body(data);
        }catch (BusinessRuleException e ){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/invitations/accept")
    public ResponseEntity<?> addFamilyMembers(@RequestParam("code") String tokenFamily){
        try{
            service.acceptFamilyInvitation(tokenFamily);
            return ResponseEntity.noContent().build();
        }catch (BusinessRuleException e ){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}
