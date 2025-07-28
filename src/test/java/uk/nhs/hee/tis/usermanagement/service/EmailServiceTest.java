package uk.nhs.hee.tis.usermanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

  @Mock
  private SesClient sesClient;

  private EmailService emailService;

  @BeforeEach
  void setUp() {
    emailService = new EmailService("no-reply@tis.nhs.uk",sesClient);
  }

  @Test
  void shouldSendEmailWithTempPassword() {
    // Given
    String email = "test@example.com";
    String password = "temp12345!";

    // When
    emailService.sendTempPasswordEmail(email, password);

    // Then
    ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);
    verify(sesClient, times(1)).sendEmail(captor.capture());

    SendEmailRequest request = captor.getValue();
    assertEquals(email, request.destination().toAddresses().get(0));
    assertTrue(request.message().body().text().data().contains(password));
    assertEquals("no-reply@tis.nhs.uk", request.source());
  }

  @Test
  void shouldThrowExceptionWhenSesFails() {
    // Given
    String email = "test@example.com";
    String password = "temp12345!";
    doThrow(SesException.builder().message("SES failed").build())
        .when(sesClient).sendEmail(any(SendEmailRequest.class));

    // Then
    SesException ex = assertThrows(SesException.class, () -> {
      emailService.sendTempPasswordEmail(email, password);
    });

    assertEquals("SES failed", ex.getMessage());
    verify(sesClient, times(1)).sendEmail(any(SendEmailRequest.class));
  }
}
