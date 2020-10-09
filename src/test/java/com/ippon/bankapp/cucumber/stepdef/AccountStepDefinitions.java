package com.ippon.bankapp.cucumber.stepdef;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ippon.bankapp.domain.Account;
import com.ippon.bankapp.repository.AccountRepository;
import com.ippon.bankapp.rest.AccountController;
import com.ippon.bankapp.service.dto.AccountDTO;
import com.ippon.bankapp.service.dto.TransactionDTO;
import com.ippon.bankapp.service.dto.TransferDTO;
import com.ippon.bankapp.service.exception.AccountNotFoundException;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.eclipse.jetty.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AccountStepDefinitions {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AccountRepository accountRepository;

    @Before
    public void before() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    @After
    public void emptyDB() {
        accountRepository.deleteAll();
    }

    private ObjectMapper objectMapper = new ObjectMapper();

    @When("A Person {string} {string} creates an account")
    public void thatAPersonIsCreated(String first, String last) throws Exception {
        String results = mockMvc
                .perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"firstName\" : \"" + first + "\"," +
                                "\"lastName\" : \"" + last + "\"" +
                                "}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AccountDTO accountDTO = new ObjectMapper().readValue(results, AccountDTO.class);

        assertThat(accountDTO.getFirstName(), is(first));
        assertThat(accountDTO.getLastName(), is(last));
        assertThat(accountDTO.getBalance(), is(BigDecimal.ZERO));
        assertThat(accountDTO.getNotificationPreference(), is("email"));
    }

    @Then("{string} has {double} balance")
    public void hasBalance(String lastName, double balance) throws Exception {
        mockMvc
                .perform(get("/api/account/" + lastName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(balance));
    }

    @When("{string} deposits {double}")
    public void deposits(String lastName, double amount) throws Exception{
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setAmount(BigDecimal.valueOf(amount));

        MvcResult mvcResult = mockMvc
                .perform(post("/api/transaction/" + lastName + "/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO)))
                .andReturn();

        if (amount >= 0) {
            assertThat(mvcResult.getResponse().getStatus(), is(HttpStatus.ACCEPTED_202));
        } else {
            assertThat(mvcResult.getResponse().getStatus(), is(HttpStatus.BAD_REQUEST_400));
        }
    }

    @When("{string} withdraws {double}")
    public void withdraws(String lastName, double amount) throws Exception{
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setAmount(BigDecimal.valueOf(amount));

        MvcResult mvcResult = mockMvc
                .perform(post("/api/transaction/" + lastName + "/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO)))
                .andReturn();

        if (amount >= 0) {
            assertThat(mvcResult.getResponse().getStatus(), is(HttpStatus.ACCEPTED_202));
        } else {
            assertThat(mvcResult.getResponse().getStatus(), is(HttpStatus.BAD_REQUEST_400));
        }
    }

    @And("{string} has a balance of {double}")
    public void hasABalanceOfInitialBalance(String lastName, double initialBalance) {
        Account account = accountRepository
                .findByLastName(lastName)
                .orElseThrow(AccountNotFoundException::new);

        account.setBalance(BigDecimal.valueOf(initialBalance));
        accountRepository.save(account);
    }

    @When("{string} transfers {string} {double}")
    public void transfers(String source, String destination, double amount) throws Exception {
        TransferDTO transferDTO = new TransferDTO();
        transferDTO.setAmount(BigDecimal.valueOf(amount));
        transferDTO.setDestinationAccountLastName(destination);

        MvcResult mvcResult = mockMvc
                .perform(post("/api/transaction/" + source + "/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDTO)))
                .andReturn();

        if (amount >= 0) {
            assertThat(mvcResult.getResponse().getStatus(), is(HttpStatus.ACCEPTED_202));
        } else {
            assertThat(mvcResult.getResponse().getStatus(), is(HttpStatus.BAD_REQUEST_400));
        }
    }

}
