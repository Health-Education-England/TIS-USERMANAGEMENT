package uk.nhs.hee.tis.usermanagement.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

/**
 * A email sending service.
 */
@Slf4j
@Service
public class EmailService {

  private static String TIS_SENDER;

  @Value("${application.tis-sender-email}")
  private void setTisSender(String value) {
    EmailService.TIS_SENDER = value;
  }

  private final SesClient sesClient;

  public EmailService(SesClient sesClient) {
    this.sesClient = sesClient;
  }

  /**
   * Send temporary password to an email address.
   *
   * @param toEmail the email to be sent to
   * @param temporaryPassword the temporary password
   */
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
        .source(EmailService.TIS_SENDER)
        .build();

    try {
      sesClient.sendEmail(emailRequest);
    } catch (Exception e) {
      log.error("Sending temp password to {} failed.", toEmail, e);
      throw e;
    }
  }
}

