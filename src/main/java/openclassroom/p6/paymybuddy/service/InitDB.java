package openclassroom.p6.paymybuddy.service;

import lombok.RequiredArgsConstructor;
import openclassroom.p6.paymybuddy.domain.Account;
import openclassroom.p6.paymybuddy.domain.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class InitDB {

    private static final Logger logger = LoggerFactory.getLogger(InitDB.class);
    private final String LOG_ID = "[InitDB]";
    private final List<String> descriptionList = List.of("Restaurant bill share","Trip money","Movie tickets","Bowling game","Grocery shopping","Gasoline expenses","Coffee date","Concert tickets","Online shopping","Fitness class fee","Taxi fare","Birthday gift","Book purchase","Charity donation","Vacation rental payment");
    private final List<Double> amountList = List.of(2.36, 1.75, 4.56, 3.97, 1.84);

    private final AccountService accountService;
    private final TransactionService transactionService;


    public void initTransactionTable() {
        logger.info("{} - START - Transaction table initialization", LOG_ID);
        List<Transaction> savedTransactions = new ArrayList<>();

        logger.info("{} - Transaction creation start...", LOG_ID);
        for (int i = 0; i < 200; i++) {
            int randomDayNb = new Random().nextInt(100);
            int randomHourNb = new Random().nextInt(23);
            int randomDescriptionIndex = new Random().nextInt(descriptionList.size() - 1);
            double amount = amountList.get(new Random().nextInt(amountList.size() - 1)) * new Random().nextInt(20);
            String description = descriptionList.get(randomDescriptionIndex);

            Transaction transaction = Transaction.builder()
                    .amount(Math.floor(amount * 100) / 100)
                    .description(description)
                    .fee(Math.floor((amount * 0.05) * 100) / 100)
                    .date(LocalDateTime.ofInstant(
                            Instant.now().minus(randomDayNb, ChronoUnit.DAYS).minus(randomHourNb, ChronoUnit.HOURS),
                            ZoneOffset.UTC)
                    ).build();
            transaction = transactionService.saveTransaction(transaction);
            if (transaction.getId() != null) savedTransactions.add(transaction);
        }
        logger.info("{} - Created & saved {} transactions", LOG_ID, savedTransactions.size());

        logger.info("{} - Retrieving users' account from db...", LOG_ID);
        Iterable<Account> accounts = accountService.getAccounts();
        long accountNb = StreamSupport.stream(accounts.spliterator(), false).count();
        logger.info("{} - Retrieved {} accounts", LOG_ID, accountNb);

        logger.info("{} - Distributing 20 transactions into users' account", LOG_ID);
        int index = 0;
        for (Account account : accounts) {
            for(int count = 0; count < 20; count++) {
                account.addTransaction(savedTransactions.get(index));
                index++;
            }
            accountService.saveAccount(account);
        }
        logger.info("{} - END - Transaction table initialization", LOG_ID);
    }

}
