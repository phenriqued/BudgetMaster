package phenriqued.BudgetMaster.Infra.Email;

import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.DTOs.Login.SignInDTO;
import phenriqued.BudgetMaster.Models.Security.TwoFactorAuthentication.TwoFactorAuth;
import phenriqued.BudgetMaster.Models.UserEntity.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class TwoFactorAuthEmailService {

    private final EmailService service;

    private static final String URL_SITE = "http://localhost:8080";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public TwoFactorAuthEmailService(EmailService service) {
        this.service = service;
    }

    public void sendVerificationEmail(User user, TwoFactorAuth code){
        String subject = "Verificação de duas etapas";
        String content = generateEmailContent(
                "Olá [[name]], <br>"
                        +"A verificação de duas etapas está quase ativa. utilize o código a baixo para verificar e ativar-lá:<br>"
                        +"<h3>[[code]]</h3>"
                        +"<strong>Este código só é válido durante 5 minutos!</strong>"
                        + "Obrigado,<br>"
                        + "Budget Master."
                , user.getName(), code.getSecret()
        );
        service.sendMail(user.getEmail(), subject, content);
    }
    public void sendTwoFactorAuthentication(User user, TwoFactorAuth code, SignInDTO data){
        String subject = "Verificação de duas etapas";
        String content = generateEmailContent(
                "Olá [[name]], <br>"
                        +"Alguém está tentando acessar sua conta.<br>"
                        +"Quando: "+ LocalDateTime.now().format(DATE_TIME_FORMATTER)
                        +"<br>Dispositivo: "+ data.identifier()
                        +"<br><strong>Se foi você, seu código de verificação é:</strong>"
                        +"<h3>[[code]]</h3>"
                        +"<strong>Este código só é válido durante 5 minutos!</strong>"
                        + "Obrigado,<br>"
                        + "Budget Master."
                , user.getName(), code.getSecret()
        );
        service.sendMail(user.getEmail(), subject, content);
    }

    private String generateEmailContent(String content, String name, String code){
        return content.replace("[[name]]", name).replace("[[code]]", code);
    }

}
