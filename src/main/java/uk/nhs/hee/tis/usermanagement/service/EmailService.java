package uk.nhs.hee.tis.usermanagement.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@Slf4j
@Service
public class EmailService {

  protected static final String TIS_SENDER = "no-reply@tis.nhs.uk";

  private final SesClient sesClient;

  public EmailService(SesClient sesClient) {
    this.sesClient = sesClient;
  }

  public void sendTempPasswordEmail(String toEmail, String temporaryPassword) {
    String subject = "Your temporary password for Trainee Information System (TIS)";
    String bodyText = String.format(
        "Dear User,\n\n"
            + "Your temporary password is: %s\n\n"
            + "Please note, this temporary password is only valid for 7 days. "
            + "Please login before then to reset your password.",
        temporaryPassword
    );

    SendEmailRequest emailRequest = SendEmailRequest.builder()
        .destination(d -> d.toAddresses(toEmail))
        .message(m -> m.subject(c -> c.data(subject)).body(b -> b.text(c -> c.data(bodyText))))
        .source(TIS_SENDER)
        .build();

    try {
      sesClient.sendEmail(emailRequest);
    } catch (Exception e) {
      log.error("Sending temp password to {} failed.", toEmail, e);
      throw e;
    }
  }
}

