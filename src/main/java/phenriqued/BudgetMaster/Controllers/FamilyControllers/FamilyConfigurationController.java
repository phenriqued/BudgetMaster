package phenriqued.BudgetMaster.Controllers.FamilyControllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import phenriqued.BudgetMaster.DTOs.Family.*;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Services.FamilyService.FamilyConfigService;


@RestController
@RequestMapping("/families/{id}")
public class FamilyConfigurationController {

    private final FamilyConfigService service;

    public FamilyConfigurationController(FamilyConfigService service) {
        this.service = service;
    }

    @PatchMapping("/name")
    public ResponseEntity<Void> updateFamilyName(@PathVariable("id") Long id, @RequestBody UpdateFamilyNameDTO updateDTO,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails){
        service.updateFamilyName(id, updateDTO, userDetails);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/invitations/generate/email")
    public ResponseEntity<?> addFamilyMembers(@PathVariable("id") Long id, @RequestBody @Valid AddFamilyMemberDTO addFamilyMemberDTO,
                                              @AuthenticationPrincipal UserDetailsImpl userDetails){
        try{
            service.addFamilyMemberByEmail(id, addFamilyMemberDTO, userDetails);
            return ResponseEntity.ok().build();
        }catch (UsernameNotFoundException exception){
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

    @PostMapping("/invitations/generate/qrCode")
    public ResponseEntity<String> addFamilyMembersWithQrCode(@PathVariable("id") Long id, @RequestBody @Valid RoleIdFamilyDTO roleIdFamilyDTO,
                                                             @AuthenticationPrincipal UserDetailsImpl userDetails){
        String urlQrCode = service.addFamilyMembersByQrCode(id, roleIdFamilyDTO, userDetails);
        return ResponseEntity.ok(urlQrCode);
    }

    @PostMapping("/invitations/accept")
    public ResponseEntity<Void> acceptInvitationByQrCode(@PathVariable("id") Long id, @RequestParam("access") String access,
                                                         @AuthenticationPrincipal UserDetailsImpl userDetails){
        service.acceptInvitationByQrCode(id, access,userDetails);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/role")
    public ResponseEntity<?> updateFamilyMemberRole(@PathVariable("id") Long id, @RequestBody @Valid UpdateRoleIdFamilyDTO updateRoleDTO,
                                                    @AuthenticationPrincipal UserDetailsImpl userDetails){
        try{
            service.updateFamilyRole(id, updateRoleDTO, userDetails);
            return ResponseEntity.ok().build();
        }catch (UsernameNotFoundException exception){
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

    @DeleteMapping("/delete/member")
    public ResponseEntity<?> deleteFamilyMember(@PathVariable("id") Long id, @RequestBody @Valid FamilyMemberIdDTO familyMemberIdDTO,
                                                   @AuthenticationPrincipal UserDetailsImpl userDetails){
        try{
            service.deleteFamilyMember(id, familyMemberIdDTO, userDetails);
            return ResponseEntity.noContent().build();
        }catch (UsernameNotFoundException exception){
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }


}
