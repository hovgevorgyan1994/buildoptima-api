package com.vecondev.buildoptima.service.impl;

import com.vecondev.buildoptima.model.user.ConfirmationToken;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Locale;

@Data
@Service
@RequiredArgsConstructor
public class MailService {
  private final TemplateEngine templateEngine;
  private final JavaMailSender javaMailSender;

  @Value("${host.ip}")
  private String ip;

  public void sendConfirm(Locale locale, ConfirmationToken token) throws MessagingException {
    String subject = "WELCOME TO OUR WEBSITE";
    String template = "email";
    send(token, template, subject, locale);
  }

  public void sendVerify(Locale locale, ConfirmationToken token) throws MessagingException {
    String subject = "CREATE NEW PASSWORD";
    String template = "createPwdEmail";
    send(token, template, subject, locale);
  }

  @Async
  protected void send(ConfirmationToken token, String template, String subject, Locale locale)
      throws MessagingException {
    String link = "https://" + ip + ":443/user/activate?token=" + token.getToken();
    final Context context = new Context(locale);
    context.setVariable("name", token.getUser().getFirstName());
    context.setVariable("surname", token.getUser().getLastName());
    context.setVariable("url", link);

    final String htmlContent = templateEngine.process(template, context);

    final MimeMessage mimeMessage = javaMailSender.createMimeMessage();
    final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
    message.setSubject(subject);
    message.setTo(token.getUser().getEmail());
    message.setText(htmlContent, true);
    this.javaMailSender.send(mimeMessage);
  }
}
