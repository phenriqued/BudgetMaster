package phenriqued.BudgetMaster.Controllers.FamilyControllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import phenriqued.BudgetMaster.DTOs.Family.*;
import phenriqued.BudgetMaster.Infra.Security.Config.SecurityConfiguration;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Services.FamilyService.FamilyConfigService;

@Tag(name = "Configurações da Familia Controller", description = "Este controlador é responsável por atualizar o nome, convidar por email e criar QRCode entrar na familia" +
        "deletar um membro da familia e sair da familia")
@SecurityRequirement(name = SecurityConfiguration.SECURITY)

@RestController
@RequestMapping("/families/{id}")
public class FamilyConfigurationController {

    private final FamilyConfigService service;

    public FamilyConfigurationController(FamilyConfigService service) {
        this.service = service;
    }

    @Operation(summary = "Atualizar o nome de uma família", description = "Responsável por atualizar o nome de uma família. Apenas o proprietário (OWNER) pode efetuar essa requisição.")
    @SecurityRequirement(name = SecurityConfiguration.SECURITY)
    @ApiResponse(responseCode = "204", description = "Nome da Família alterada com sucesso.")
    @ApiResponse(responseCode = "401", description = "Não autenticado.")
    @ApiResponse(responseCode = "403", description = "Proibido. O usuário autenticado não é o proprietário da família e não tem permissão para altera-la.")
    @ApiResponse(responseCode = "404", description = "Não encontrado. A família com o ID especificado não existe.")
    @PatchMapping("/name")
    public ResponseEntity<Void> updateFamilyName(@PathVariable("id") Long id, @RequestBody UpdateFamilyNameDTO updateDTO,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails){
        service.updateFamilyName(id, updateDTO, userDetails);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Gera e envia um convite para um novo membro por e-mail",
            description = "Gera um convite por e-mail para um novo membro se juntar a uma família. Apenas o proprietário da família pode enviar convites.")
    @ApiResponse(responseCode = "200", description = "Convite enviado com sucesso.")
    @ApiResponse(responseCode = "400", description = "Falha na requisição. O usuário convidado já faz parte da família ou o e-mail é inválido.",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"mensagem\": \"O usuário com o email 'teste@email.com' já faz parte desta família.\"}")
            )
    )
    @ApiResponse(responseCode = "403", description = "Proibido. O usuário autenticado não é o proprietário da família e não tem permissão para enviar convites.")
    @ApiResponse(responseCode = "404", description = "Não encontrado. A família com o ID especificado não existe.")
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
    @Operation(summary = "Gera uma URL de convite para um novo membro por QR Code",
            description = "Gera uma URL de convite que pode ser convertida em um QR Code. O token JWT incorporado permite que um usuário entre na família. Apenas o proprietário (OWNER) pode gerar este convite.")
    @ApiResponse(responseCode = "200", description = "URL do convite gerada com sucesso!",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class),
                    examples = @ExampleObject(
                            summary = "Exemplo de URL de convite",
                            value = """
                    {
                      "invitationUrl": "http://localhost:8080/families/10/invitations/accept?access=eyJhbGciOiJIUzI1NiJ9..."
                    }
                    """
                    )
            )
    )
    @ApiResponse(responseCode = "403", description = "Proibido. O usuário autenticado não é o proprietário da família e não tem permissão para gerar o convite.")
    @ApiResponse(responseCode = "404", description = "Não encontrado. A família com o ID especificado não existe.")
    @PostMapping("/invitations/generate/qrCode")
    public ResponseEntity<String> addFamilyMembersWithQrCode(@PathVariable("id") Long id, @RequestBody @Valid RoleIdFamilyDTO roleIdFamilyDTO,
                                                             @AuthenticationPrincipal UserDetailsImpl userDetails){
        String urlQrCode = service.addFamilyMembersByQrCode(id, roleIdFamilyDTO, userDetails);
        return ResponseEntity.ok(urlQrCode);
    }
    @Operation(summary = "Aceita um convite para entrar em uma família", description = "URL utilizada para aceitar um convite de entrada em uma família. Requer um token de convite válido gerado pelo QRCODE")
    @ApiResponse(responseCode = "204", description = "Convite aceito com sucesso!")
    @ApiResponse(responseCode = "400", description = "O token é inválido ou o usuário já faz parte da família.")
    @ApiResponse(responseCode = "404", description = "Não encontrado. O token de convite não existe ou já foi utilizado.")
    @PostMapping("/invitations/accept")
    public ResponseEntity<Void> acceptInvitationByQrCode(@PathVariable("id") Long id, @RequestParam("access") String access,
                                                         @AuthenticationPrincipal UserDetailsImpl userDetails){
        service.acceptInvitationByQrCode(id, access,userDetails);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Atualizar a role de uma família", description = "Responsável por atualizar a role de uma família. Apenas o proprietário (OWNER) pode efetuar essa requisição.")
    @SecurityRequirement(name = SecurityConfiguration.SECURITY)
    @ApiResponse(responseCode = "204", description = "Role de integrante da familia alterada com sucesso.")
    @ApiResponse(responseCode = "401", description = "Não autenticado.")
    @ApiResponse(responseCode = "403", description = "Proibido. O usuário autenticado não é o proprietário da família e não tem permissão para altera-la.")
    @ApiResponse(responseCode = "404", description = "Não encontrado. A família com o ID especificado não existe.")
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
    @Operation(summary = "Deleta um usuário de uma família", description = "Responsável por deletar um usuário de uma família. Apenas o proprietário (OWNER) pode efetuar essa requisição.")
    @SecurityRequirement(name = SecurityConfiguration.SECURITY)
    @ApiResponse(responseCode = "204", description = "Usuário deletado da familia com sucesso.")
    @ApiResponse(responseCode = "401", description = "Não autenticado.")
    @ApiResponse(responseCode = "403", description = "Proibido. O usuário autenticado não é o proprietário da família e não tem permissão para deleta-la.")
    @ApiResponse(responseCode = "404", description = "Não encontrado. A família com o ID especificado não existe.")
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
    @Operation(summary = "Sai da familia", description = "Responsável por sair de uma familia")
    @SecurityRequirement(name = SecurityConfiguration.SECURITY)
    @ApiResponse(responseCode = "204", description = "saiu da familia com sucesso.")
    @ApiResponse(responseCode = "401", description = "Não autenticado.")
    @ApiResponse(responseCode = "404", description = "Não encontrado. A família com o ID especificado não existe.")
    @DeleteMapping("/member/me")
    public ResponseEntity<Void> leaveFamily(@PathVariable("id") Long id, @AuthenticationPrincipal UserDetailsImpl userDetails){
        service.leaveFamily(id, userDetails);
        return ResponseEntity.noContent().build();
    }


}
