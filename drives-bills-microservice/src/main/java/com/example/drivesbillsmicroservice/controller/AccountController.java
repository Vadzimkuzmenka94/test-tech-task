package com.example.drivesbillsmicroservice.controller;

import com.example.drivesbillsmicroservice.dto.message.MessageDto;
import com.example.drivesbillsmicroservice.enums.Currency;
import com.example.drivesbillsmicroservice.service.AccountService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@OpenAPIDefinition(
        info = @Info(
                title = "Rik Masters Test",
                version = "1.0.0",
                description = "Service for working with cars"
        ),
        servers = @Server(url = "http://localhost:8082")
)
@RestController
@RequestMapping("/accounts")
public class AccountController {
  private final AccountService accountService;
  private final MessageSource messageSource;

  @Autowired
  public AccountController(AccountService accountService, MessageSource messageSource) {
    this.accountService = accountService;
    this.messageSource = messageSource;
  }

  @Operation(
          summary = "Endpoint for credit money to account",
          description = "This endpoint allows to credit money",
          responses = {
              @ApiResponse(
                          responseCode = "200",
                          description = "Money was credited",
                          content = @Content(
                                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                                  schema = @Schema(implementation = MessageDto.class)
                          )
                  ),
              @ApiResponse(
                          responseCode = "404",
                          description = "Not found",
                          content = @Content(
                                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                                  schema = @Schema(implementation = MessageDto.class)
                          )
                  )
          }
  )
  @PostMapping("/{accountId}/credit")
  public ResponseEntity<MessageDto> credit(@PathVariable Long accountId,
                                           @RequestParam Double amount,
                                           @RequestParam Currency currency) {
    accountService.credit(accountId, amount, currency);
    return createResponseEntityOk("money.credit.account");
  }

  @Operation(
          summary = "Endpoint for debit money from account",
          description = "This endpoint allows to debit money",
          responses = {
              @ApiResponse(
                          responseCode = "200",
                          description = "Money was debited",
                          content = @Content(
                                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                                  schema = @Schema(implementation = MessageDto.class)
                          )
                  ),
              @ApiResponse(
                          responseCode = "404",
                          description = "Not found",
                          content = @Content(
                                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                                  schema = @Schema(implementation = MessageDto.class)
                          )
                  ),
              @ApiResponse(
                          responseCode = "402",
                          description = "Insufficient balance",
                          content = @Content(
                                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                                  schema = @Schema(implementation = MessageDto.class)
                          )
                  )
          }
  )
  @PostMapping("/{accountId}/debit")
  public ResponseEntity<MessageDto> debit(@PathVariable Long accountId,
                                          @RequestParam double amount,
                                          @RequestParam Currency currency) {
    accountService.debit(accountId, amount, currency);
    return createResponseEntityOk("money.debit.account");
  }

  @Operation(
          summary = "Endpoint for get current balance",
          description = "This endpoint allows to get balance",
          responses = {
              @ApiResponse(
                          responseCode = "200",
                          description = "Balance",
                          content = @Content(
                                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                                  schema = @Schema(implementation = MessageDto.class)
                          )
                  ),
              @ApiResponse(
                          responseCode = "404",
                          description = "Not found",
                          content = @Content(
                                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                                  schema = @Schema(implementation = MessageDto.class)
                          )
                  )
          }
  )
  @GetMapping("/{accountId}/balance")
  public ResponseEntity<MessageDto> getBalance(@PathVariable Long accountId,
                                               @RequestParam Currency currency) {
    BigDecimal balance = accountService.getBalance(accountId, currency);
    return ResponseEntity.status(HttpStatus.OK)
            .body(new MessageDto(String.valueOf(balance)));
  }

  private ResponseEntity<MessageDto> createResponseEntityOk(String messageKey) {
    String message = messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());
    return ResponseEntity.status(HttpStatus.OK).body(new MessageDto(message));
  }
}