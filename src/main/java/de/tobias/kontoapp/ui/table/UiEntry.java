package de.tobias.kontoapp.ui.table;
import java.math.BigDecimal;
import java.time.LocalDate;
public class UiEntry {
	private String type;
	private String description;
	private BigDecimal amount;
	private LocalDate date;
	private String owner;
	private String details;
	private boolean monthSummary;
	private Integer managerIndex;
	
	
	public UiEntry(String owner, String type, String description, BigDecimal amount,
            LocalDate date, String details, boolean monthSummary, Integer managerIndex) {
	this.owner = owner;
	this.type = type;
	this.description = description;
	this.amount = amount;
	this.date = date;
	this.details = details;
	this.monthSummary = monthSummary;
	this.managerIndex = managerIndex;
}
	
	public static UiEntry createMonthSummary(String monthLabel, String details) {
		return new UiEntry("", "Monatsbilanz", monthLabel, null, null, details, true, null);
	}
	private String extractSummaryValue(String prefix) {
	    if (details == null || details.isBlank()) {
	        return "";
	    }

	    String[] lines = details.split("\\R");

	    for (String line : lines) {
	        if (line.startsWith(prefix)) {
	            return line.substring(prefix.length()).trim();
	        }
	    }

	    return "";
	}

	public String getOwner() {
		return owner;
	}
	public String getType() {
		return type;
	}

	public String getDescription() {
		return description;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public LocalDate getDate() {
		return date;
	}
	public String getDetails() {
		return details;
	}
	public boolean isMonthSummary() {
		return monthSummary;
	}
	public Integer getManagerIndex() {
		return managerIndex;
	}
	public String getSummaryTitle() {
	    return description;
	}

	public String getSummaryTotalText() {
	    return extractSummaryValue("Gesamt:");
	}

	public String getSummaryTobiasText() {
	    return extractSummaryValue("Tobias:");
	}

	public String getSummaryBerndText() {
	    return extractSummaryValue("Bernd:");
	}
	
	

}
