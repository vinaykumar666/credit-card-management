package com.cards.account.config;

import com.cards.account.domain.Account;
import com.cards.account.domain.Transaction;
import com.cards.account.repository.AccountRepository;
import com.cards.account.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Seeds demo card accounts and sample transactions for the catalog users in docs/USERS.md.
 */
@Component
@Profile("!prod")
public class AccountDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AccountDataInitializer.class);

    private static final UUID ADA_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID BEN_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID ADA_ACCOUNT = UUID.fromString("a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1");
    private static final UUID BEN_ACCOUNT = UUID.fromString("b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2");

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountDataInitializer(AccountRepository accountRepository,
                                  TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Inserts demo accounts and transactions when the account table is empty for those users.
     *
     * @param args startup arguments (unused)
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedAccount(ADA_ACCOUNT, ADA_ID, "4111111111111111", "1111", "VISA",
                new BigDecimal("12000.00"), new BigDecimal("8450.25"),
                "Ada Lovelace", "ada.lovelace@cards.local", "+1-415-555-0101");
        seedAccount(BEN_ACCOUNT, BEN_ID, "5500000000000004", "0004", "MASTERCARD",
                new BigDecimal("8000.00"), new BigDecimal("5120.00"),
                "Ben Franklin", "ben.franklin@cards.local", "+1-212-555-0142");

        Account ada = accountRepository.findById(ADA_ACCOUNT).orElse(null);
        Account ben = accountRepository.findById(BEN_ACCOUNT).orElse(null);
        if (ada != null && ben != null && transactionRepository.count() == 0) {
            Instant now = Instant.now();
            transactionRepository.saveAll(List.of(
                    tx(ada, "PURCHASE", "120.50", "USD", "Blue Bottle Coffee", "Cafe", "POSTED", now.minus(2, ChronoUnit.DAYS)),
                    tx(ada, "PURCHASE", "89.00", "USD", "Transit SF", "Metro", "POSTED", now.minus(5, ChronoUnit.DAYS)),
                    tx(ada, "PAYMENT", "500.00", "USD", "Online Payment", "Thank you", "POSTED", now.minus(8, ChronoUnit.DAYS)),
                    tx(ben, "PURCHASE", "240.00", "USD", "Apple Store", "Electronics", "POSTED", now.minus(1, ChronoUnit.DAYS)),
                    tx(ben, "PURCHASE", "64.20", "USD", "Whole Foods", "Groceries", "POSTED", now.minus(3, ChronoUnit.DAYS))
            ));
            log.info("Seeded demo transactions");
        }
    }

    private void seedAccount(UUID id, UUID userId, String number, String lastFour, String brand,
                             BigDecimal limit, BigDecimal available, String holder, String email, String phone) {
        if (accountRepository.existsById(id) || accountRepository.findByAccountNumber(number).isPresent()) {
            return;
        }
        Account account = Account.builder()
                .id(id)
                .userId(userId)
                .accountNumber(number)
                .cardLastFour(lastFour)
                .cardBrand(brand)
                .creditLimit(limit)
                .availableCredit(available)
                .currency("USD")
                .status("ACTIVE")
                .holderName(holder)
                .email(email)
                .phone(phone)
                .build();
        accountRepository.save(account);
        log.info("Seeded account {} for user {}", lastFour, email);
    }

    private static Transaction tx(Account account, String type, String amount, String currency,
                                  String merchant, String description, String status, Instant when) {
        return Transaction.builder()
                .id(UUID.randomUUID())
                .account(account)
                .type(type)
                .amount(new BigDecimal(amount))
                .currency(currency)
                .merchant(merchant)
                .description(description)
                .status(status)
                .occurredAt(when)
                .build();
    }
}
