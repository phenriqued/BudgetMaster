package phenriqued.BudgetMaster.Infra.Email;

import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.Services.Security.SecurityUserTokensService.SecurityUserTokenService;
import phenriqued.BudgetMaster.Models.Security.Token.TokenType;
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

    public void sendActivateAccount(User user) {
        String subject = "Ativação da sua conta Budget Master.";
        String content = "Agradecemos por visita Budget Master! Sua conta foi ativada com sucesso. <br>"
                +"Mantenha seu endereço de e-mail sempre atualizado na sua conta da Budget Master, pois o e-mail associado à sua conta é o único para o qual enviamos confirmações e informações."
                +"<br>Agradecemos novamente por utilizar Budget Master.";

        service.sendMail(user.getEmail(), subject, content);
    }

    public void sendDisableUserEmail(User user){
        String code = tokenService.generatedSecurityUserToken(user, "open-id-"+user.getId()+"security-management", 4320, TokenType.OPEN_ID);
        String subject = "Desativação da sua conta.";
        String content = generateEmailContent(
                "Olá [[name]], <br>"
                        +"Sentimos muito que você decidiu desativar sua conta.<br>"
                        +"Caso não foi você que solicitou a desativação da conta, acesse o link abaixo: "
                        +"<h3><a href=\"[[URL]]\" target=\"_self\">Budget Master Alterar a senha - [[name]]</a></h3>"
                        +"<strong>O link é válido até 72 horas, após a expiração a conta é excluida!</strong><br>"
                        +"Se porventura queira ativar novamente sua conta,<strong> basta efetuar o login novamente dentro do prazo de 72 horas.</strong><br>"
                        +"Obrigado,<br>"
                        +"Budget Master."
                , user.getName(), URL_SITE+"/account/manager/change-password-to-activate?code="+code
        );
        service.sendMail(user.getEmail(), subject, content);
    }

    public void sendHardDeleteUser(User user) {
        String subject = "Sua conta Budget Master expirou e foi removida.";
        String content = generateEmailContent(
                "Olá [[name]],<br>"
                + "Este e-mail é para informar que sua conta no Budget Master atingiu a data de expiração e, de acordo com nossos termos de serviço, foi removida de nossos sistemas."
                + "Agradecemos por ter feito parte da Budget Master. Se você tiver um minuto, adoraríamos ouvir sobre sua experiência com o Budget Master e como podemos melhorar. Sua opinião é muito importante para nós!"
                +"<h3><a href=\"[[URL]]\" target=\"_self\">Pesquisa de Feedback</a></h3>"
                +"<br>Se tiver alguma dúvida, por favor, entre em contato."
                +"<br>Atenciosamente,<br>"
                +"A Equipe Budget Master"
                , user.getName(), URL_SITE+"/feedback");

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
