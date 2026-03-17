package de.tobias.kontoapp.ui;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
			return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
		}
}
