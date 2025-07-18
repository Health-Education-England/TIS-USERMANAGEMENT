package uk.nhs.hee.tis.usermanagement.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
@ConditionalOnProperty(name = "application.authentication-provider", havingValue = "cognito")
public class EmailService {

  private final SesClient sesClient;

  public EmailService(SesClient sesClient) {
    this.sesClient = sesClient;
  }

  public void sendTemporaryPasswordEmail(String toEmail, String temporaryPassword) {
    String subject = "Your temporary password for Trainee Information System (TIS)";
    String bodyText = String.format(
        "Dear User,\n\n"
            + "Your temporary password is: %s\n\n"
            + "Please log in and reset your password as soon as possible.",
        temporaryPassword
    );

    SendEmailRequest emailRequest = SendEmailRequest.builder()
        .destination(Destination.builder().toAddresses(toEmail).build())
        .message(Message.builder()
            .subject(Content.builder().data(subject).build())
            .body(Body.builder()
                .text(Content.builder().data(bodyText).build())
                .build())
            .build())
        .source("no-reply@tis.nhs.uk")
        .build();

    sesClient.sendEmail(emailRequest);
  }
}

