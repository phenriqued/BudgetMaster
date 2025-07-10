package phenriqued.BudgetMaster.Infra.Email;

import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.Infra.Security.Service.RefreshTokenService;
import phenriqued.BudgetMaster.Models.UserEntity.User;

@Service
public class SecurityEmailService {

    private final EmailService service;
    private final RefreshTokenService tokenService;

    private static final String URL_SITE = "http://localhost:8080";

    public SecurityEmailService(EmailService service, RefreshTokenService tokenService) {
        this.service = service;
        this.tokenService = tokenService;
    }

    public void sendVerificationEmail(User user){
        String subject = "Seja-bem vindo a Budget Master, seu link para verificar o email";
        String content = generateEmailContent(
                "Olá [[name]], <br>"
                        +"Por favor clique no link abaixo para verificar sua conta. Este link é válido por <strong>10 minutos</strong>:<br>"
                        + "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFICAR</a></h3>"
                        + "Obrigado,<br>"
                        + "Budget Master."
                , user.getName(), URL_SITE+"/login/activate-user?code="+getUserActivation(user)
        );
        service.sendMail(user.getEmail(), subject, content);
    }

    private String generateEmailContent(String content, String name, String url){
        return content.replace("[[name]]", name).replace("[[URL]]", url);
    }

    private String getUserActivation(User user){
        return tokenService.generatedActivationToken(user);
    }

}
