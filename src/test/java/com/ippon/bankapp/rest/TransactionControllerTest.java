package com.ippon.bankapp.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ippon.bankapp.rest.errors.RestErrorHandler;
import com.ippon.bankapp.rest.errors.TransactionController;
import com.ippon.bankapp.service.AccountService;
import com.ippon.bankapp.service.dto.AccountDTO;
import com.ippon.bankapp.service.dto.TransactionDTO;
import com.ippon.bankapp.service.dto.TransferDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {TransactionControllerTest.class, RestErrorHandler.class})
class TransactionControllerTest {

    @MockBean
    private AccountService accountService;

    @Autowired
    private RestErrorHandler restErrorHandler;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void before() {
        TransactionController subject = new TransactionController(accountService);
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(subject)
                .setControllerAdvice(restErrorHandler)
                .build();
    }

    @Test
    void deposit_validRequest() throws Exception {
        given(accountService.deposit("Scott", BigDecimal.TEN))
                .willReturn(new AccountDTO()
                        .lastName("Scott")
                        .firstName("Ben"));

        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setAmount(BigDecimal.TEN);

        mockMvc
                .perform(post("/api/transaction/Scott/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.firstName").value("Ben"))
                .andExpect(jsonPath("$.lastName").value("Scott"));
    }

    @Test
    void deposit_missingAmount() throws Exception {
        TransactionDTO transactionDTO = new TransactionDTO();

        mockMvc
                .perform(post("/api/transaction/Scott/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deposit_amountTooSmall() throws Exception {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setAmount(BigDecimal.ZERO);

        mockMvc
                .perform(post("/api/transaction/Scott/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void withdraw_validRequest() throws Exception {
        given(accountService.withdraw("Scott", BigDecimal.TEN))
                .willReturn(new AccountDTO()
                        .lastName("Scott")
                        .firstName("Ben"));

        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setAmount(BigDecimal.TEN);

         mockMvc
                .perform(post("/api/transaction/Scott/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.firstName").value("Ben"))
                .andExpect(jsonPath("$.lastName").value("Scott"));
    }

    @Test
    void withdraw_missingAmount() throws Exception {
        TransactionDTO transactionDTO = new TransactionDTO();

        mockMvc
                .perform(post("/api/transaction/Scott/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void withdraw_amountTooSmall() throws Exception {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setAmount(BigDecimal.ZERO);

        mockMvc
                .perform(post("/api/transaction/Scott/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_validRequest() throws Exception {
        given(accountService.transfer("Scott", "destination", BigDecimal.TEN))
                .willReturn(new AccountDTO()
                        .lastName("Scott")
                        .firstName("Ben"));

        TransferDTO transferDTO = new TransferDTO();
        transferDTO.setAmount(BigDecimal.TEN);
        transferDTO.setDestinationAccountLastName("destination");

        mockMvc
                .perform(post("/api/transaction/Scott/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDTO)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.firstName").value("Ben"))
                .andExpect(jsonPath("$.lastName").value("Scott"));
    }

    @Test
    void transfer_amountTooSmall() throws Exception {
        TransferDTO transferDTO = new TransferDTO();
        transferDTO.setAmount(BigDecimal.ZERO);
        transferDTO.setDestinationAccountLastName("destination");

        mockMvc
                .perform(post("/api/transaction/Scott/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_missingAmount() throws Exception {
        TransferDTO transferDTO = new TransferDTO();
        transferDTO.setDestinationAccountLastName("destination");

        mockMvc
                .perform(post("/api/transaction/Scott/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_missingDestinationAccount() throws Exception {
        TransferDTO transferDTO = new TransferDTO();
        transferDTO.setAmount(BigDecimal.TEN);

        mockMvc
                .perform(post("/api/transaction/Scott/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDTO)))
                .andExpect(status().isBadRequest());
    }
}
