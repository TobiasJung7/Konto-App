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
	
	public UiEntry(String owner, String type, String description, BigDecimal amount, LocalDate date, String details, boolean monthSummary) {
		this.owner = owner;
		this.type = type;
		this.description = description;
		this.amount = amount;
		this.date = date;
		this.details = details;
		this.monthSummary = monthSummary;
	}
	
	public static UiEntry createMonthSummary(String monthLabel, String details) {
		return new UiEntry("", "Monatsbilanz", monthLabel, null, null, details,true);
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
	

}
