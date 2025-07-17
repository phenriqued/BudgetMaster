package phenriqued.BudgetMaster.Infra.Email;

import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.Infra.Security.Service.SecurityUserTokenService;
import phenriqued.BudgetMaster.Infra.Security.Token.TokenType;
import phenriqued.BudgetMaster.Models.UserEntity.User;

@Service
public class UserEmailService {

    private final EmailService service;
    private final SecurityUserTokenService tokenService;

    private static final String URL_SITE = "http://localhost:8080";

    public UserEmailService(EmailService service, SecurityUserTokenService tokenService) {
        this.service = service;
        this.tokenService = tokenService;
    }

    public void sendDisableUserEmail(User user){
        String code = tokenService.generatedSecurityUserToken(user, "open-id-"+user.getId()+"security-management", 4320, TokenType.OPEN_ID);
        String subject = "Desativação da sua conta.";
        String content = generateEmailContent(
                "Olá [[name]], <br>"
                        +"Sentimos muito que você decidiu desativar sua conta.<br>"
                        +"Caso não foi você que solicitou a desativação da conta, acesse o link abaixo: "
                        + "<h3><a href=\"[[URL]]\" target=\"_self\">Budget Master - [[name]]</a></h3>"
                        +"<strong>O link é válido até 72 horas, após a expiração a conta é excluida!</strong>"
                        + "Obrigado,<br>"
                        + "Budget Master."
                , user.getName(), URL_SITE+"/account/manager/change-password-to-activate?code="+code
        );
        service.sendMail(user.getEmail(), subject, content);
    }

    public void sendChangedPassword(User user) {
        String subject = "Alteração na sua conta Budget Master.";
        String content = "Agradecemos por visita Budget Master! Conforme sua solicitação, sua senha foi alterada com sucesso. <br>"
                        +"Mantenha seu endereço de e-mail sempre atualizado na sua conta da Budget Master, pois o e-mail associado à sua conta é o único para o qual enviamos confirmações e informações."
                        +"<br>Agradecemos novamente por utilizar Budget Master.";

        service.sendMail(user.getEmail(), subject, content);
    }

    private String generateEmailContent(String content, String name, String url){
        return content.replace("[[name]]", name).replace("[[URL]]", url);
    }


}
