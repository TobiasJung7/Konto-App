package de.tobias.kontoapp.ui;

import java.math.BigDecimal;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public class MoneyUtil {

	//Betrag in einen BigDecimal umwandeln:
		public BigDecimal parseAmount(String text) {
					try {
						String raw = text.trim().replace(',', '.');
						return new BigDecimal(raw);
			} catch(NumberFormatException e) {
				return null;
			}
		}
		
		
		//Formatieren von BigDecimal
		public String formatAmount(BigDecimal amount) {
			if(amount == null) {
				return "0,00";
			}
			NumberFormat formatter = NumberFormat.getNumberInstance(Locale.GERMANY);
			formatter.setMinimumFractionDigits(2);
			formatter.setMaximumFractionDigits(2);
			String text;
			text = formatter.format(amount);
			return text;
		}
		//Formatieren in Deutsche Zahl
		
}
