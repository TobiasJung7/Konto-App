package de.tobias.kontoapp.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import Konto.Split;
import Konto.Transaction;
import Konto.TransactionType;

public class TransactionFileService {

    public void saveTransactions(Path path, List<Transaction> transactions) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("date;type;description;accountEffect;grossInterestRate;taxBernd;tobiasSplit;berndSplit");
            writer.newLine();

            for (Transaction tx : transactions) {
                writer.write(toCsvLine(tx));
                writer.newLine();
            }
        }
    }

    public List<Transaction> loadTransactions(Path path) throws IOException {
        List<Transaction> transactions = new ArrayList<>();

        if (!Files.exists(path)) {
            return transactions;
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line = reader.readLine(); // Kopfzeile überspringen

            while ((line = reader.readLine()) != null) {
                transactions.add(fromCsvLine(line));
            }
        }

        return transactions;
    }

    private String toCsvLine(Transaction tx) {
        return tx.getDate() + ";"
                + tx.getType() + ";"
                + escape(tx.getDescription()) + ";"
                + tx.getAccountEffekt().toPlainString() + ";"
                + tx.getGrossInterestRate().toPlainString() + ";"
                + tx.getTaxBernd().toPlainString() + ";"
                + tx.getTobiasSplit().toPlainString() + ";"
                + tx.getBerndSplit().toPlainString();
    }

    private Transaction fromCsvLine(String line) {
        String[] parts = line.split(";", -1);

        LocalDate date = LocalDate.parse(parts[0]);
        TransactionType type = TransactionType.valueOf(parts[1]);
        String description = parts[2];
        BigDecimal accountEffect = new BigDecimal(parts[3]);
        BigDecimal grossInterestRate = new BigDecimal(parts[4]);
        BigDecimal taxBernd = new BigDecimal(parts[5]);
        BigDecimal tobiasSplit = new BigDecimal(parts[6]);
        BigDecimal berndSplit = new BigDecimal(parts[7]);

        Split split = new Split(tobiasSplit, berndSplit);

        return new Transaction(
                date,
                type,
                description,
                accountEffect,
                grossInterestRate,
                taxBernd,
                split
        );
    }

    private String escape(String text) {
        return text.replace(";", ",");
    }
}