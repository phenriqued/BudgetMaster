package phenriqued.BudgetMaster.Infra.Email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;
import phenriqued.BudgetMaster.Infra.Security.Service.RefreshTokenService;
import phenriqued.BudgetMaster.Models.UserEntity.User;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    private final RefreshTokenService tokenService;
    private final JavaMailSender mailSender;
    private static final String ORIGIN_MAIL = "support@BudgetMaster.com";
    private static final String NAME_SENDER = "Budget Master";

    private static final String URL_SITE = "http://localhost:8080";

    public EmailService(RefreshTokenService tokenService, JavaMailSender mailSender) {
        this.tokenService = tokenService;
        this.mailSender = mailSender;
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
        sendMail(user.getEmail(), subject, content);
    }

    @Async
    private void sendMail(String userEmail, String subject, String content) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        try {
            helper.setFrom(ORIGIN_MAIL, NAME_SENDER);
            helper.setTo(userEmail);
            helper.setSubject(subject);
            helper.setText(content, true);
        } catch(MessagingException | UnsupportedEncodingException e){
            throw new BusinessRuleException("[INTERNAL ERROR] unable to send email. \n"+e.getMessage());
        }
        mailSender.send(message);
    }
    private String generateEmailContent(String template, String name, String url){
        return template.replace("[[name]]", name).replace("[[URL]]", url);
    }
    private String getUserActivation(User user){
        return tokenService.generatedActivationToken(user);
    }



}
