package com.vecondev.buildoptima.service.mail;

import com.vecondev.buildoptima.model.user.ConfirmationToken;

import javax.mail.MessagingException;
import java.util.Locale;

public interface MailService {

    void sendConfirm(Locale locale, ConfirmationToken token) throws MessagingException;

    void sendVerify(Locale locale, ConfirmationToken token) throws MessagingException;

}
