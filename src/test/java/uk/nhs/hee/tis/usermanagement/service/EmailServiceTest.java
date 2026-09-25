package uk.nhs.hee.tis.usermanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

  private static final String USER_EMAIL_ADRESS = "test@example.com";
  private static final String USER_TEMP_PASSWORD = "temp12345!";
  private static final String TIS_SENDER_EMAIL = "no-reply@tis.nhs.uk";
  private static final int TEMP_PASSWORD_VALIDITY_DAYS = 1;
  private static final int MULTIPLE_TEMP_PASSWORD_VALIDITY_DAYS = 2;

  @Mock
  private SesClient sesClient;

  private EmailService emailService;

  void setUp(int tempPasswordValidityDays) {
    emailService = new EmailService(TIS_SENDER_EMAIL, tempPasswordValidityDays, sesClient);
  }

  @ParameterizedTest
  @ValueSource(ints = {TEMP_PASSWORD_VALIDITY_DAYS, MULTIPLE_TEMP_PASSWORD_VALIDITY_DAYS})
  void shouldSendEmailWithTempPassword(int tempPasswordValidityDays) {
    // Given
    setUp(tempPasswordValidityDays);

    // When
    emailService.sendTempPasswordEmail(USER_EMAIL_ADRESS, USER_TEMP_PASSWORD);

    // Then
    ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);
    verify(sesClient, times(1)).sendEmail(captor.capture());

    SendEmailRequest request = captor.getValue();
    assertEquals(USER_EMAIL_ADRESS, request.destination().toAddresses().get(0));
    String textData = request.message().body().text().data();
    assertTrue(textData.contains(USER_TEMP_PASSWORD));
    assertTrue(textData.contains(String.format(
        "Please note, this temporary password is only valid for %d day%s. "
            + "Please login before then to reset your password.", tempPasswordValidityDays,
        tempPasswordValidityDays > 1 ? "s" : "")));
    assertEquals(TIS_SENDER_EMAIL, request.source());
  }

  @Test
  void shouldThrowExceptionWhenSesFails() {
    // Given
    setUp(TEMP_PASSWORD_VALIDITY_DAYS);
    doThrow(SesException.builder().message("SES failed").build())
        .when(sesClient).sendEmail(any(SendEmailRequest.class));

    // Then
    SesException ex = assertThrows(SesException.class,
        () -> emailService.sendTempPasswordEmail(USER_EMAIL_ADRESS, USER_TEMP_PASSWORD));

    assertEquals("SES failed", ex.getMessage());
    verify(sesClient, times(1)).sendEmail(any(SendEmailRequest.class));
  }
}
