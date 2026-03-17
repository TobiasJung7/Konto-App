package de.tobias.kontoapp.ui;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import Konto.Split;
import Konto.Transaction;
import Konto.TransactionType;


public class TransactionBuilder {

	public static final String TRANSFER_TOBIAS_BERND = "Tobias -> Bernd";
    public static final String TRANSFER_BERND_TOBIAS = "Bernd  -> Tobias";
	
	public TransactionType mapUiType(String uiType) {
		switch(uiType) {
		case "Einnahme":
			return TransactionType.Deposit;
		case "Ausgabe" :
			return TransactionType.Expenses;
		case "Umbuchung":
			return TransactionType.Transfer;
		default: 
			throw new IllegalArgumentException("Unbeknannter Buchungstyp");
		}
	}
	
	
	public BigDecimal toSignedAmount(TransactionType type, BigDecimal amount) {
		if (type == TransactionType.Deposit) {
			return amount;
		}
		if (type == TransactionType.Expenses) {
			return amount.negate();
		}
		throw new IllegalArgumentException("Vorzeichen für diesen Typ noch nicht definiert");
	}
	
	public Split buildSplit(String owner, BigDecimal signedAmount) {
		switch (owner) {
		case "Tobias": 
			return new Split(signedAmount, BigDecimal.ZERO);
		case "Bernd":
			return new Split(BigDecimal.ZERO, signedAmount);
		case "Gemeinsam":
			BigDecimal tobiasPart = signedAmount.divide(BigDecimal.valueOf(2),2, RoundingMode.HALF_UP);
			BigDecimal berndPart = signedAmount.subtract(tobiasPart);
			return new Split(tobiasPart, berndPart);
		default: 
			throw new IllegalArgumentException("Unbekannte Zuordnung");
		}
	}
	
	public Split buildTransferSplit(String transferDirection, BigDecimal amount) {
		if(TRANSFER_TOBIAS_BERND.equals(transferDirection)) {
			return new Split(amount.negate(), amount);
		}
		if(TRANSFER_BERND_TOBIAS.equals(transferDirection)) {
			return new Split(amount, amount.negate()); 
		}else {
			throw new IllegalArgumentException("Unbekannte Umbuchungsrichtung");
		}
	}
	
	public Transaction buildTransaction(String uiType, String owner, String transferDirection, String description, LocalDate date, BigDecimal amount) {
		TransactionType backendType = mapUiType(uiType);
		
		if (backendType == TransactionType.Transfer) {
			Split split = buildTransferSplit(transferDirection, amount);
			return new Transaction(date, backendType, description, BigDecimal.ZERO, split);
		}
		
		BigDecimal signedAmount = toSignedAmount(backendType, amount);
		Split split = buildSplit(owner, signedAmount);
		
		return new Transaction(date, backendType, description, signedAmount, split);
	}

}
