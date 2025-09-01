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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import phenriqued.BudgetMaster.DTOs.Family.RequestCreateFamilyDTO;
import phenriqued.BudgetMaster.DTOs.Family.ResponseAllFamiliesDTO;
import phenriqued.BudgetMaster.DTOs.Family.ResponseCreatedFamilyDTO;
import phenriqued.BudgetMaster.DTOs.Family.ResponseGetFamilyDTO;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Infra.Security.Config.SecurityConfiguration;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Services.FamilyService.FamilyService;

import java.net.URI;
import java.util.List;

@Tag(name = "Familia Controller", description = "Este controlador é responsável por criar, listar todos, listar por ID " +
        "e deletar uma familia, cuja o usuário seja o proprietário da familia")
@SecurityRequirement(name = SecurityConfiguration.SECURITY)

@RestController
@RequestMapping("/families")
public class FamilyController {

    private final FamilyService service;

    public FamilyController(FamilyService service) {
        this.service = service;
    }

    @Operation(summary = "Lista todas familia que o usuário faz parte", description = "responsável por listar todas as familias que usuário faz parte")
    @SecurityRequirement(name = SecurityConfiguration.SECURITY)
    @ApiResponse(responseCode = "200", description = "Lista com sucesso todos as familias")
    @ApiResponse(responseCode = "401", description = "Não autenticado.")
    @GetMapping
    public ResponseEntity<List<ResponseAllFamiliesDTO>> getAllFamily(@AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseEntity.ok(service.getAllFamiliesByUser(userDetails));
    }
    @Operation(summary = "Lista uma familia por ID que o usuário faz parte", description = "Requer autenticação. O usuário deve ser membro da família para visualizá-la.")
    @SecurityRequirement(name = SecurityConfiguration.SECURITY)
    @ApiResponse(responseCode = "200", description = "Lista com sucesso a familia por ID")
    @ApiResponse(responseCode = "401", description = "Não autenticado.")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseGetFamilyDTO> getFamilyById(@PathVariable(value = "id") Long id, @AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseEntity.ok(service.getFamilyById(id, userDetails));
    }

    @Operation(summary = "Cria uma familia", description = "Cria uma familia, sendo obrigatório adicionar um membro inserindo um email válido vinculado ao usuário existente, " +
            "sendo possível adicinar e colando a ROLE que achar necessário para o usuário, somente a ROLE OWNER é indisponível colocar em outro usuário no momento da criação")
    @SecurityRequirement(name = SecurityConfiguration.SECURITY)
    @ApiResponse(responseCode = "201", description = "Família criada com sucesso!",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseCreatedFamilyDTO.class),
                    examples = @ExampleObject(
                            summary = "Exemplo de resposta de sucesso",
                            value = """
                    {
                      "id": 123,
                      "name": "Família Exemplo",
                      "userFamilies": [
                        {
                          "id": 1,
                          "userId": 10,
                          "roleFamily": "OWNER"
                        }
                      ],
                      "invitesNotSent": {"Invitations sent successfully": "invitations were sent to all members!"}
                    }
                    """
                    )
            )
    )
    @ApiResponse(responseCode = "201", description = "Família criada com sucesso, mas caso coloque alguns usuário incorretos, com email ou role incorreto",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseCreatedFamilyDTO.class),
                    examples = @ExampleObject(
                            summary = "Exemplo de resposta de sucesso",
                            value = """
                    {
                      "id": 123,
                      "name": "Família Exemplo",
                      "userFamilies": [
                        {
                          "id": 1,
                          "userId": 10,
                          "roleFamily": "OWNER"
                        }
                      ],
                      "invitesNotSent": {
                            "email incorreto": "Non-existent email or username, check email!",
                            "user email com role incorreto": "Role Family - 1 - Unable to add a member as OWNER at this time"
                      }
                    }
                    """
                    )
            )
    )
    @ApiResponse(responseCode = "400", description = "BusinessRuleException - Caso o usuário seja OWNER de mais de 5 familias")
    @ApiResponse(responseCode = "400", description = "BusinessRuleException - Caso tente criar uma familia sem adicionar nenhum usuáario válido")
    @ApiResponse(responseCode = "401", description = "Não autenticado.")
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

    @Operation(summary = "Aceita um convite para entrar em uma família", description = "URL utilizada para aceitar um convite de entrada em uma família. Requer um token de convite válido enviado por e-mail.")
    @ApiResponse(responseCode = "204", description = "Convite aceito com sucesso!")
    @ApiResponse(responseCode = "400", description = "O token é inválido ou o usuário já faz parte da família.")
    @ApiResponse(responseCode = "404", description = "Não encontrado. O token de convite não existe ou já foi utilizado.")
    @PostMapping("/invitations/accept")
    public ResponseEntity<?> addFamilyMembers(@RequestParam("code") String tokenFamily){
        try{
            service.acceptFamilyInvitation(tokenFamily);
            return ResponseEntity.noContent().build();
        }catch (BusinessRuleException e ){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @Operation(summary = "Deleta uma família", description = "Responsável por deletar uma família. Apenas o proprietário (OWNER) pode efetuar essa requisição.")
    @SecurityRequirement(name = SecurityConfiguration.SECURITY)
    @ApiResponse(responseCode = "204", description = "Família deletada com sucesso.")
    @ApiResponse(responseCode = "401", description = "Não autenticado.")
    @ApiResponse(responseCode = "403", description = "Proibido. O usuário autenticado não é o proprietário da família e não tem permissão para deletá-la.")
    @ApiResponse(responseCode = "404", description = "Não encontrado. A família com o ID especificado não existe.")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFamily(@PathVariable(value = "id") Long id, @AuthenticationPrincipal UserDetailsImpl userDetails){
        service.deleteFamilyByIdAndUser(id, userDetails);
        return ResponseEntity.noContent().build();
    }

}
