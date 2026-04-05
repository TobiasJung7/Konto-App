package de.tobias.kontoapp.ui.table;

import java.math.BigDecimal;
import Konto.Transaction;
import Konto.TransactionType;
import de.tobias.kontoapp.util.MoneyUtil;


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
	
	public UiEntry mapTransactionToEntry(Transaction tx, int managerIndex) {
	    return new UiEntry(
	        mapTransactionOwner(tx),
	        mapTransactionType(tx),
	        tx.getDescription(),
	        tx.getAccountEffekt().abs(),
	        tx.getDate(),
	        mapTransactionDetails(tx),
	        false,
	        managerIndex
	    );
	}
	
	private String mapTransactionDetails(Transaction tx) {
		MoneyUtil moneyUtil = new MoneyUtil();
		if (tx.getType() == TransactionType.Interest) {
		    return "Steuer Bernd: " + moneyUtil.formatAmount(tx.getTaxBernd())
		         + " | Tobias: " + moneyUtil.formatAmount(tx.getTobiasSplit())
		         + " | Bernd: " + moneyUtil.formatAmount(tx.getBerndSplit());
		}
		if (tx.getType() == TransactionType.Transfer && tx.getBerndSplit() == tx.getBerndSplit()) {
			return "Umbuchungssumme: " + moneyUtil.formatAmount(tx.getTobiasSplit());
		}
		else return " - ";
		
	}
	
}
