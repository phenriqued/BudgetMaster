package phenriqued.BudgetMaster.Infra.Email;

import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Models.FamilyEntity.Family;
import phenriqued.BudgetMaster.Models.FamilyEntity.RoleFamily;
import phenriqued.BudgetMaster.Models.FamilyEntity.UserFamily;
import phenriqued.BudgetMaster.Models.UserEntity.User;
import phenriqued.BudgetMaster.Services.Security.TokensService.TokenService;

@Service
public class FamilyEmailService {

    private final EmailService emailService;
    private final TokenService tokenService;
    private static final String URL_SITE = "http://localhost:8080";

    public FamilyEmailService(EmailService emailService, TokenService tokenService) {
        this.emailService = emailService;
        this.tokenService = tokenService;
    }

    public void invitedMember(User user, Family family, RoleFamily roleFamily, UserFamily userOwner){
        String code = tokenService.generatedTokenJwtAtFamily(user, family, roleFamily.id);
        String subject = userOwner.getUser().getName()+" está de convidando para fazer parte da "+ family.getName();
        String content = generateEmailContent(
                "Olá " + user.getName() + ",<br><br>"
                        + userOwner.getUser().getName() + " te convidou para o grupo **" + family.getName() + "** no Budget Master.<br><br>"
                        + getRoleDescription(roleFamily) + "<br><br>"
                        + "Para aceitar o convite, basta clicar no botão abaixo:<br><br>"
                        + "<a href=\" [[URL]] \" style=\"background-color:#007BFF;color:#ffffff;padding:10px 20px;text-decoration:none;border-radius:5px;display:inline-block;\">Aceitar convite</a><br><br>"
                        + "Se preferir, copie e cole o link no seu navegador:<br>"
                        + "<code> [[URL]] </code><br><br>"
                        + "O convite é válido por **72 horas**. Após esse período, ele será automaticamente negado.<br><br>"
                        + "Aproveite para organizar suas finanças com o Budget Master!<br><br>"
                        + "Atenciosamente,<br>"
                        + "Equipe Budget Master"
        ,user.getName() , URL_SITE+"/family/invitations/accept?code="+code);

        emailService.sendMail(user.getEmail(), subject, content);
    }

    private String getRoleDescription(RoleFamily roleFamily) {
        if (roleFamily.equals(RoleFamily.MEMBER)) {
            return "Neste grupo, você será um **membro**, e poderá adicionar e visualizar suas próprias despesas e receitas, que serão compartilhadas com o grupo.";
        } else if (roleFamily.equals(RoleFamily.VIEWER)) {
            return "Neste grupo, você será um **visualizador**, e poderá ver todas as receitas e despesas do grupo, mas sem a permissão de adicionar as suas próprias.";
        }

        throw new BusinessRuleException("Unable to invite a user by pasting the role as \"OWNER\"");
    }

    private String generateEmailContent(String content, String name, String url) {
        return content.replace("[[name]]", name).replace("[[URL]]", url);
    }
}
