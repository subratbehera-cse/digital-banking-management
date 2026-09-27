package com.bankapp.controller;

import com.bankapp.dto.request.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end integration test covering registration, login, account creation,
 * deposit, withdrawal and transfer through the real REST API, backed by an
 * in-memory H2 database (see application-test.yml).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String tokenA;
    private Long accountAId;
    private String accountANumber;

    private String tokenB;
    private String accountBNumber;

    @BeforeEach
    void setUp() throws Exception {
        tokenA = registerAndLogin("txuser_a", "txuser_a@example.com");
        tokenB = registerAndLogin("txuser_b", "txuser_b@example.com");

        CreateAccountRequest accReq = new CreateAccountRequest();
        accReq.setAccountType(com.bankapp.entity.AccountType.SAVINGS);
        accReq.setInitialDeposit(new BigDecimal("1000.00"));

        MvcResult resultA = mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accReq)))
                .andExpect(status().isCreated())
                .andReturn();
        Map<?, ?> bodyA = objectMapper.readValue(resultA.getResponse().getContentAsString(), Map.class);
        Map<?, ?> dataA = (Map<?, ?>) bodyA.get("data");
        accountAId = Long.valueOf(dataA.get("id").toString());
        accountANumber = dataA.get("accountNumber").toString();

        MvcResult resultB = mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accReq)))
                .andExpect(status().isCreated())
                .andReturn();
        Map<?, ?> bodyB = objectMapper.readValue(resultB.getResponse().getContentAsString(), Map.class);
        Map<?, ?> dataB = (Map<?, ?>) bodyB.get("data");
        accountBNumber = dataB.get("accountNumber").toString();
    }

    private String registerAndLogin(String username, String email) throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setUsername(username);
        register.setEmail(email);
        register.setPassword("Password123");
        register.setFirstName("Test");
        register.setLastName("User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest();
        login.setUsername(username);
        login.setPassword("Password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        Map<?, ?> body = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        Map<?, ?> data = (Map<?, ?>) body.get("data");
        return data.get("token").toString();
    }

    @Test
    void deposit_shouldIncreaseAccountBalance() throws Exception {
        DepositRequest deposit = new DepositRequest();
        deposit.setAccountId(accountAId);
        deposit.setAmount(new BigDecimal("250.50"));

        mockMvc.perform(post("/api/transactions/deposit")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deposit)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.balanceAfter").value(1250.50))
                .andExpect(jsonPath("$.data.transactionRef").isNotEmpty());
    }

    @Test
    void withdraw_shouldFailWithUnprocessableEntity_whenBalanceInsufficient() throws Exception {
        WithdrawRequest withdraw = new WithdrawRequest();
        withdraw.setAccountId(accountAId);
        withdraw.setAmount(new BigDecimal("999999.00"));

        mockMvc.perform(post("/api/transactions/withdraw")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdraw)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void transfer_shouldMoveMoneyBetweenTwoDifferentUsersAccounts() throws Exception {
        TransferRequest transfer = new TransferRequest();
        transfer.setFromAccountId(accountAId);
        transfer.setToAccountNumber(accountBNumber);
        transfer.setAmount(new BigDecimal("300.00"));

        mockMvc.perform(post("/api/transactions/transfer")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transfer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.toAccountNumber").value(accountBNumber));
    }

    @Test
    void accessingAnotherUsersAccount_shouldBeForbidden() throws Exception {
        mockMvc.perform(get("/api/accounts/" + accountAId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedRequest_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isUnauthorized());
    }
}
