package de.tobias.kontoapp.ui;

import java.math.BigDecimal;
import java.math.RoundingMode;

import Konto.Transaction;
import Konto.TransactionType;

public class UiEntryMapper {
	
	private final String transferTobiasBernd;
    private final String transferBerndTobias;
    private MoneyUtil moneyUtil;

    
    public UiEntryMapper(String transferTobiasBernd, String transferBerndTobias) {
    	this.transferBerndTobias = transferBerndTobias;
    	this.transferTobiasBernd = transferTobiasBernd;
    }
	
	
	public String mapTransactionOwner(Transaction tx) {
		BigDecimal accountEffect = tx.getAccountEffekt();
		BigDecimal tobias = tx.getTobiasSplit();
		BigDecimal bernd = tx.getBerndSplit();
		
		if(tx.getType() == TransactionType.Transfer) {
			if (tobias.compareTo(BigDecimal.ZERO) < 0 && bernd.compareTo(BigDecimal.ZERO) > 0) {
				return transferTobiasBernd;
			}
			if(bernd.compareTo(BigDecimal.ZERO) < 0 && tobias.compareTo(BigDecimal.ZERO) > 0) {
				return transferBerndTobias;
			}
		}
		
		if (tobias.compareTo(accountEffect) == 0 && bernd.compareTo(BigDecimal.ZERO) == 0) {
			return "Tobias";
		}
		if(bernd.compareTo(accountEffect) == 0 && tobias.compareTo(BigDecimal.ZERO) == 0) {
			return "Bernd";
		}
		return "Gemeinsam";
	}
	
	//Transactions typ bestimmen
	public String mapTransactionType(Transaction tx) {
		switch (tx.getType()) {
		case Deposit:
			return "Einnahme";
		case Expenses:
			return "Ausgabe";
		case Transfer:
			return "Umbuchung";
		default: 
			return tx.getType().toString();
		}
	}
	//Uientry aus Transaction bauen
	
	public UiEntry mapTransactionToEntry(Transaction tx) {
		return new UiEntry(mapTransactionOwner(tx), mapTransactionType(tx), tx.getDescription(),tx.getAccountEffekt().abs(),tx.getDate(), mapTransactionDetails(tx));
	}
	
	private String mapTransactionDetails(Transaction tx) {
		if (tx.getType() == TransactionType.Interest) {
		    return "Brutto: " + formatMoney(tx.getGrossInterestRate())
		         + " | Steuer Bernd: " + formatMoney(tx.getTaxBernd())
		         + " | Tobias: " + formatMoney(tx.getTobiasSplit())
		         + " | Bernd: " + formatMoney(tx.getBerndSplit());
		}else return "";
	}
	private String formatMoney(BigDecimal value) {
	    return value.setScale(2, RoundingMode.HALF_UP).toPlainString() + " €";
	}
}
