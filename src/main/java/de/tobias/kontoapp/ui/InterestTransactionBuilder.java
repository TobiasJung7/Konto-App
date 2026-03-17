package de.tobias.kontoapp.ui;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;

import Konto.AverageBalanceCalculator;
import Konto.InterestCalculator;
import Konto.Transaction;
import Konto.TransactionManager;

public class InterestTransactionBuilder {

    private final InterestCalculator interestCalculator;

    public InterestTransactionBuilder(TransactionManager manager) {
        AverageBalanceCalculator averageBalanceCalculator = new AverageBalanceCalculator(manager);
        this.interestCalculator = new InterestCalculator(averageBalanceCalculator);
    }

    public Transaction buildInterestTransaction(
            Month month,
            int year,
            LocalDate bookingDate,
            String description,
            BigDecimal grossInterestRate,
            BigDecimal taxBernd
    ) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = YearMonth.of(year, month).atEndOfMonth();

        return interestCalculator.createInterestTransaction(
                start,
                end,
                bookingDate,
                description,
                grossInterestRate,
                taxBernd
        );
    }
}