package com.vecondev.buildoptima.service.mail;

import com.vecondev.buildoptima.model.user.ConfirmationToken;
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

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

  public static final String CONFIRMATION_MAIL_SUBJECT = "WELCOME TO OUR WEBSITE";
  public static final String VERIFICATION_MAIL_SUBJECT = "CREATE NEW PASSWORD";
  public static final String CONFIRMATION_MAIL_TEMPLATE = "email";
  public static final String VERIFICATION_MAIL_TEMPLATE = "createPwdEmail";
  public static final String CONFIRMATION_URI = "/user/activate?token=";
  public static final String VERIFICATION_URI = "/recover?confirmationToken=";

  private final TemplateEngine templateEngine;
  private final JavaMailSender javaMailSender;

  @Value("${host.address}")
  private String address;

  @Override
  public void sendConfirm(Locale locale, ConfirmationToken token) throws MessagingException {
    String url = address + CONFIRMATION_URI;
    send(token, CONFIRMATION_MAIL_TEMPLATE, CONFIRMATION_MAIL_SUBJECT, locale, url);
  }

  @Override
  public void sendVerify(Locale locale, ConfirmationToken token) throws MessagingException {
    String url = address + VERIFICATION_URI;
    send(token, VERIFICATION_MAIL_TEMPLATE, VERIFICATION_MAIL_SUBJECT, locale, url);
  }

  @Async
  protected void send(
      ConfirmationToken token, String template, String subject, Locale locale, String host)
      throws MessagingException {
    String link = host + token.getToken();
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
