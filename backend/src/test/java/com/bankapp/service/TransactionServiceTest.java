package com.bankapp.service;

import com.bankapp.dto.request.DepositRequest;
import com.bankapp.dto.request.TransferRequest;
import com.bankapp.dto.request.WithdrawRequest;
import com.bankapp.dto.response.TransactionDto;
import com.bankapp.entity.*;
import com.bankapp.exception.InsufficientBalanceException;
import com.bankapp.exception.InvalidOperationException;
import com.bankapp.exception.UnauthorizedAccessException;
import com.bankapp.repository.BankAccountRepository;
import com.bankapp.repository.TransactionRepository;
import com.bankapp.service.impl.FailedTransactionRecorder;
import com.bankapp.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private BankAccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private FailedTransactionRecorder failedTransactionRecorder;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User owner;
    private BankAccount account;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).username("john").build();
        account = BankAccount.builder()
                .id(10L)
                .accountNumber("100000000001")
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("500.00"))
                .status(AccountStatus.ACTIVE)
                .user(owner)
                .build();
    }

    @Test
    void deposit_shouldIncreaseBalanceAndCreateSuccessTransaction() {
        DepositRequest request = new DepositRequest();
        request.setAccountId(10L);
        request.setAmount(new BigDecimal("100.00"));

        when(accountRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(BankAccount.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        TransactionDto result = transactionService.deposit(1L, request);

        assertThat(result.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(account.getBalance()).isEqualByComparingTo("600.00");
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void withdraw_shouldThrowInsufficientBalanceException_whenAmountExceedsBalance() {
        WithdrawRequest request = new WithdrawRequest();
        request.setAccountId(10L);
        request.setAmount(new BigDecimal("1000.00"));

        when(accountRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> transactionService.withdraw(1L, request))
                .isInstanceOf(InsufficientBalanceException.class);

        verify(failedTransactionRecorder, times(1))
                .record(eq(account), isNull(), eq(TransactionType.WITHDRAWAL), any(BigDecimal.class), anyString());
        assertThat(account.getBalance()).isEqualByComparingTo("500.00");
    }

    @Test
    void withdraw_shouldThrowUnauthorized_whenUserDoesNotOwnAccount() {
        WithdrawRequest request = new WithdrawRequest();
        request.setAccountId(10L);
        request.setAmount(new BigDecimal("50.00"));

        when(accountRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> transactionService.withdraw(999L, request))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void deposit_shouldFail_whenAccountIsInactive() {
        account.setStatus(AccountStatus.INACTIVE);
        DepositRequest request = new DepositRequest();
        request.setAccountId(10L);
        request.setAmount(new BigDecimal("50.00"));

        when(accountRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> transactionService.deposit(1L, request))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void transfer_shouldMoveFundsBetweenTwoAccounts() {
        User otherOwner = User.builder().id(2L).username("mary").build();
        BankAccount destination = BankAccount.builder()
                .id(20L)
                .accountNumber("100000000002")
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("200.00"))
                .status(AccountStatus.ACTIVE)
                .user(otherOwner)
                .build();

        TransferRequest request = new TransferRequest();
        request.setFromAccountId(10L);
        request.setToAccountNumber("100000000002");
        request.setAmount(new BigDecimal("150.00"));

        when(accountRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(account));
        when(accountRepository.findByAccountNumber("100000000002")).thenReturn(Optional.of(destination));
        when(accountRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(destination));
        when(accountRepository.save(any(BankAccount.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(2L);
            return t;
        });

        TransactionDto result = transactionService.transfer(1L, request);

        assertThat(result.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(account.getBalance()).isEqualByComparingTo("350.00");
        assertThat(destination.getBalance()).isEqualByComparingTo("350.00");
    }

    @Test
    void transfer_shouldThrow_whenTransferringToSameAccount() {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(10L);
        request.setToAccountNumber("100000000001");
        request.setAmount(new BigDecimal("10.00"));

        when(accountRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(account));
        when(accountRepository.findByAccountNumber("100000000001")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> transactionService.transfer(1L, request))
                .isInstanceOf(InvalidOperationException.class);
    }
}
