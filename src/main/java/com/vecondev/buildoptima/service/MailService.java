package com.vecondev.buildoptima.service;

import com.vecondev.buildoptima.model.user.ConfirmationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MailService {
  private final TemplateEngine templateEngine;
  private final JavaMailSender javaMailSender;

  @Async
  public void send(Locale locale, ConfirmationToken token) throws MessagingException {
    String subject = "WELCOME TO OUR WEBSITE";
    String link = "https://localhost:443/registration/activate?token=" + token.getToken();
    final Context context = new Context(locale);
    context.setVariable("name", token.getUser().getFirstName());
    context.setVariable("surname", token.getUser().getLastName());
    context.setVariable("url", link);

    final String htmlContent = templateEngine.process("email", context);

    final MimeMessage mimeMessage = javaMailSender.createMimeMessage();
    final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
    message.setSubject(subject);
    message.setTo(token.getUser().getEmail());
    message.setText(htmlContent, true);
    this.javaMailSender.send(mimeMessage);
  }
}
