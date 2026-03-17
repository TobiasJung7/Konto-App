package de.tobias.kontoapp.ui;
import java.math.BigDecimal;
import java.time.LocalDate;
public class UiEntry {
	private String type;
	private String description;
	private BigDecimal amount;
	private LocalDate date;
	private String owner;
	private String details;
	
	public UiEntry(String owner, String type, String description, BigDecimal amount, LocalDate date, String details) {
		this.owner = owner;
		this.type = type;
		this.description = description;
		this.amount = amount;
		this.date = date;
		this.details = details;
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
	

}
