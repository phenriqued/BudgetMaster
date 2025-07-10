package phenriqued.BudgetMaster.Infra.Email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BusinessRuleException;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

@Service
class EmailService {

    private final JavaMailSender mailSender;
    private static final String ORIGIN_MAIL = "support@BudgetMaster.com";
    private static final String NAME_SENDER = "Budget Master";

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    protected void sendMail(String userEmail, String subject, String content) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        try {
            contentValidation(userEmail, subject, content);
            helper.setFrom(ORIGIN_MAIL, NAME_SENDER);
            helper.setTo(userEmail);
            helper.setSubject(subject);
            helper.setText(content, true);
        } catch(MessagingException | UnsupportedEncodingException | NullPointerException e){
            throw new BusinessRuleException("[INTERNAL ERROR] unable to send email. \n"+e.getMessage());
        }
        mailSender.send(message);
    }

    private void contentValidation(String userEmail, String subject, String content){
        if(Objects.isNull(userEmail) || Objects.isNull(subject) || Objects.isNull(content)){
            throw new NullPointerException("[INTERNAL ERROR] Email or subject or content is null");
        }
    }
}
