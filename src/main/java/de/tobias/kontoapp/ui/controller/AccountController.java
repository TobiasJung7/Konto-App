package de.tobias.kontoapp.ui.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import Konto.Transaction;
import Konto.TransactionManager;
import Konto.TransactionType;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.nio.file.Path;

import de.tobias.kontoapp.application.InterestTransactionBuilder;
import de.tobias.kontoapp.application.TransactionBuilder;
import de.tobias.kontoapp.persistence.TransactionFileService;
import de.tobias.kontoapp.ui.AccountView;
import de.tobias.kontoapp.ui.table.UiEntry;
import de.tobias.kontoapp.ui.table.UiEntryMapper;
import de.tobias.kontoapp.util.MoneyUtil;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import de.tobias.kontoapp.application.AccountProfile;


public class AccountController {
	
	private final AccountView view;
    private final ObservableList<UiEntry> entries;
    private final TransactionManager manager;
    private final TransactionBuilder transactionBuilder;
    private final UiEntryMapper uiEntryMapper;
    private final MoneyUtil moneyUtil;
    private final InterestTransactionBuilder interestTransactionBuilder;
    private boolean editMode;
    private int editingManagerIndex = -1;
    private final TransactionFileService fileService = new TransactionFileService();
    private AccountProfile currentProfile = AccountProfile.TOBIAS;
    
    
    public AccountController(
            AccountView view,
            ObservableList<UiEntry> entries,
            TransactionManager manager,
            TransactionBuilder transactionBuilder,
            InterestTransactionBuilder interestTransactionBuilder,
            UiEntryMapper uiEntryMapper,
            MoneyUtil moneyUtil
            
    ) {
        this.view = view;
        this.entries = entries;
        this.manager = manager;
        this.transactionBuilder = transactionBuilder;
        this.interestTransactionBuilder = interestTransactionBuilder;
        this.uiEntryMapper = uiEntryMapper;
        this.moneyUtil = moneyUtil;
        this.view.getEntriesTable().setItems(entries);
        registerListeners();
        clearForm();
        updateFormMode();
        updateFilterMode();
        view.getAccountBox().setValue(currentProfile);
        loadCurrentProfileFromFile();
        view.applyProfileTexts(currentProfile);
        updateAccountUi();
    }
		
	private void registerListeners() {
		this.view.getAccountBox().valueProperty().addListener((obs, oldProfile, newProfile) -> {
		    if (newProfile == null || newProfile == oldProfile) {
		        return;
		    }

		    saveToFile();
		    currentProfile = newProfile;
		    loadCurrentProfileFromFile();
		    view.applyProfileTexts(currentProfile);
		    updateAccountUi();
		});
		 	this.view.getYearFilterBox().valueProperty().addListener((obs, oldValue, newValue) -> { updateFilterMode(); refreshEntriesFromManager(); });
	        this.view.getPersonFilterBox().valueProperty().addListener((obs, oldValue, newValue) -> refreshEntriesFromManager());
	        this.view.getMonthFilterBox().valueProperty().addListener((obs, oldValue, newValue) -> refreshEntriesFromManager());
	        this.view.getBalanceCheckBox().selectedProperty().addListener((obs, oldValue, newValue) -> { refreshBalanceLabels(); });
	        this.view.getTypFilterBox().valueProperty().addListener((obs, oldValue, newValue) -> refreshEntriesFromManager());
	        this.view.getSubmitButton().setOnAction(e -> handleSubmit());
	        this.view.getDeleteButton().setOnAction(e -> handleDelete());
	        this.view.getTypBox().setOnAction(e -> updateFormMode());
	        this.view.getCancelEditButton().setOnAction(e -> clearForm());
	        this.view.getEditButton().setOnAction(e -> loadSelectedTransactionIntoForm());
	        this.view.getEntriesTable().setOnMouseClicked(e -> {
	            if (e.getClickCount() == 2) {loadSelectedTransactionIntoForm();}});
	        this.view.getClearButton().setOnAction(e -> clearForm());
	}
	
	
	private static final DateTimeFormatter GERMAN_MONTH_FORMATTER =
	        DateTimeFormatter.ofPattern("MMMM yyyy", Locale.GERMAN);
	
	//Methode für Transaktionsausgabe
		private void handleSubmit() {
		    String type = view.getTypBox().getValue();

		    if ("Zinsen".equals(type)) {
		        String error = validateInterestInput();
		        if (error != null) {
		            view.getStatusLabel().setText(error);
		            return;
		        }

		        BigDecimal grossInterest = moneyUtil.parseAmount(view.getGrossInterestField().getText());
		        BigDecimal taxBernd = moneyUtil.parseAmount(view.getTaxBerndField().getText());

		        Transaction tx;
		        try {
		            tx = interestTransactionBuilder.buildInterestTransaction(
		                    view.getMonthBox().getValue(),
		                    view.getYearSpinner().getValue(),
		                    view.getDatumPicker().getValue(),
		                    view.getBeschreibung().getText(),
		                    grossInterest,
		                    taxBernd,
		                    view.getEnteredByBox().getValue()
		            );
		        } catch (IllegalArgumentException ex) {
		            view.getStatusLabel().setText(ex.getMessage());
		            return;
		        }

		        if (editMode) {
		            manager.replaceTransactionAt(editingManagerIndex, tx);
		        } else {
		            manager.addTransaction(tx);
		        }
				   afterSuccessfulSubmit();
		        return;
		    }

		    
		    String error = validateStandardInput();
		    if (error != null) {
		        view.getStatusLabel().setText(error);
		        return;
		    }

		    BigDecimal amount = moneyUtil.parseAmount(view.getBetrag().getText());

		    Transaction tx;
		    try {
		        tx = transactionBuilder.buildTransaction(
		                view.getTypBox().getValue(),
		                view.getOwnerBox().getValue(),
		                view.getTransferBox().getValue(),
		                view.getBeschreibung().getText(),
		                view.getDatumPicker().getValue(),
		                amount,
		                view.getEnteredByBox().getValue()
		        );
		    } catch (IllegalArgumentException ex) {
		        view.getStatusLabel().setText(ex.getMessage());
		        return;
		    }
		   if(editMode) {
			   manager.replaceTransactionAt(editingManagerIndex, tx);
		   }else {
			   manager.addTransaction(tx);
		   }
		   afterSuccessfulSubmit();
		    return;
		}
		
		
	//Hilfsmethode für HandleSumbmit
		private void afterSuccessfulSubmit() {
				saveToFile();
				refreshEntriesFromManager();
			    refreshBalanceLabels();
			    clearForm();
				}
		
		private void saveToFile() {
		    try {
		        fileService.saveTransactions(getSavePath(), manager.getAllTransactions());
		    } catch (IOException e) {
		        view.getStatusLabel().setText("Fehler beim Speichern der Datei");
		        e.printStackTrace();
		    }
		}
		private void updateAccountUi() {
		    view.getActiveAccountLabel().setText("Gerade geöffnet: " + currentProfile.getDisplayName());
		    view.setFormTitleText("Buchung – " + currentProfile.getDisplayName());
		}	
				
				
		private String validateInterestInput() {
			if(view.getBeschreibung().getText().isBlank() || view.getDatumPicker().getValue() == null || view.getGrossInterestField().getText().isBlank() || view.getMonthBox().getValue() == null || view.getYearSpinner().getValue() == null || view.getEnteredByBox().getValue() == null) {
				return "Bitte alle Zinsfelder ausfüllen";
			}
			
			BigDecimal grossinterest = moneyUtil.parseAmount(view.getGrossInterestField().getText());
			BigDecimal taxBernd = moneyUtil.parseAmount(view.getTaxBerndField().getText());
			
			if(grossinterest == null || taxBernd == null) {
				return "Ungültige Zinswerte";
			}
			
			if (grossinterest.compareTo(BigDecimal.ZERO) <= 0){
				return "Bruttozins muss größer als Null sein";
			}
			if (taxBernd.compareTo(BigDecimal.ZERO) < 0) {
				return "Steuer BErnd darf nicht negativ sein";
			}
			if ( taxBernd.compareTo(grossinterest) > 0) {
				return "Steuer Bernd darf nicht größer als der Bruttozins sein";
			}
			LocalDate monthEnd = YearMonth.of(view.getYearSpinner().getValue(), view.getMonthBox().getValue()).atEndOfMonth();

			if (view.getDatumPicker().getValue().isBefore(monthEnd)) {
			    return "Buchungsdatum darf nicht vor dem Ende des Zinsmonats liegen";
			}
			return null;
		}
		
		private String validateStandardInput() {
		    if (view.getBeschreibung().getText().isBlank()
		            || view.getTypBox().getValue() == null
		            || view.getDatumPicker().getValue() == null
		            || view.getBetrag().getText().isBlank()
		    		|| view.getEnteredByBox().getValue() == null)
		        
		    		return "Bitte alle Pflichtfelder ausfüllen";
		    

		    if ("Umbuchung".equals(view.getTypBox().getValue()) && view.getTransferBox().getValue() == null) {
		        return "Bitte Umbuchungsrichtung wählen";
		    }

		    if (!"Umbuchung".equals(view.getTypBox().getValue()) && view.getOwnerBox().getValue() == null) {
		        return "Bitte Zuordnung wählen";
		    }

		    BigDecimal amount = moneyUtil.parseAmount(view.getBetrag().getText());

		    if (amount == null) {
		        return "Ungültiger Betrag";
		    }

		    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
		        return "Betrag muss größer als Null sein";
		    }

		    return null;
		}
		

    
		private boolean shouldShowMonthSummaries() {
			String personFilter = view.getPersonFilterBox().getValue();
			String typFilter = view.getTypFilterBox().getValue();
			
			return "Alle".equals(personFilter) && "Alle".equals(typFilter);
				
		}
		private UiEntry createMonthSummaryEntry(YearMonth month) {
			LocalDate monthEnd = month.atEndOfMonth();
			BigDecimal totalBalance = manager.getAccountBalanceAt(monthEnd);
			BigDecimal tobiasBalance = manager.getTobiasBalanceAt(monthEnd);
			BigDecimal berndBalance = manager.getBerndBalanceAt(monthEnd);
			String monthLabel = "Monatsbilanz " + month.format(GERMAN_MONTH_FORMATTER);
			 String details = "Gesamt: " + moneyUtil.formatAmount(totalBalance)
	            + "\nTobias: " + moneyUtil.formatAmount(tobiasBalance)
	            + "\nBernd: " + moneyUtil.formatAmount(berndBalance);

	    return UiEntry.createMonthSummary(monthLabel, details);
		}
		
		
		
		
		
	//refresh Entries From Manager
		private void refreshEntriesFromManager() {
		    entries.clear();

		    String personFilter = view.getPersonFilterBox().getValue();
		    String typFilter = view.getTypFilterBox().getValue();
		    String yearFilter = view.getYearFilterBox().getValue();
		    String monthFilter = view.getMonthFilterBox().getValue();

		    List<Transaction> allTransactions = manager.getAllTransactions();
		    List<Integer> filteredManagerIndices = new ArrayList<>();

		    for (int i = 0; i < allTransactions.size(); i++) {
		        Transaction tx = allTransactions.get(i);

		        if (matchesPersonFilter(tx, personFilter)
		                && matchesTypFilter(tx, typFilter)
		                && matchesYearFilter(tx, yearFilter)
		                && matchesMonthFilter(tx, monthFilter)) {
		            filteredManagerIndices.add(i);
		        }
		    }

		    filteredManagerIndices.sort(
		            Comparator.comparing(i -> allTransactions.get(i).getDate())
		    );

		    if (!shouldShowMonthSummaries()) {
		        for (int i = filteredManagerIndices.size() - 1; i >= 0; i--) {
		            int managerIndex = filteredManagerIndices.get(i);
		            Transaction tx = allTransactions.get(managerIndex);

		            UiEntry entry = uiEntryMapper.mapTransactionToEntry(tx, managerIndex);
		            entries.add(entry);
		        }
		        return;
		    }

		    YearMonth currentMonth = null;

		    for (int i = filteredManagerIndices.size() - 1; i >= 0; i--) {
		        int managerIndex = filteredManagerIndices.get(i);
		        Transaction tx = allTransactions.get(managerIndex);

		        YearMonth txMonth = YearMonth.from(tx.getDate());

		        if (currentMonth == null) {
		            currentMonth = txMonth;
		        } else if (!txMonth.equals(currentMonth)) {
		            entries.add(createMonthSummaryEntry(currentMonth));
		            currentMonth = txMonth;
		        }

		        UiEntry entry = uiEntryMapper.mapTransactionToEntry(tx, managerIndex);
		        entries.add(entry);
		    }

		    if (currentMonth != null) {
		        entries.add(createMonthSummaryEntry(currentMonth));
		    }
		}
		private boolean matchesPersonFilter(Transaction tx, String filter) {
			if(filter == null || "Alle".equals(filter)) {
				return true;
			}
			if("Gemeinsam".equals(filter)){
				return (tx.getBerndSplit().compareTo(BigDecimal.ZERO) != 0 && tx.getTobiasSplit().compareTo(BigDecimal.ZERO) != 0);
			}
			if("Tobias".equals(filter)) {
				return tx.getTobiasSplit().compareTo(BigDecimal.ZERO) != 0;
			}
			if("Bernd".equals(filter)) {
				return tx.getBerndSplit().compareTo(BigDecimal.ZERO) != 0;
			}
			else {
			throw new IllegalArgumentException("Unbekannter Filter");
			}
		}
		private boolean matchesTypFilter(Transaction tx, String filter) {
			if(filter == null || "Alle".equals(filter)) {
				return true;
			}
			if("Einnahmen".equals(filter)) {
				return tx.getType() == TransactionType.Deposit;
			}
			if("Zinsen".equals(filter)) {
				return tx.getType() == TransactionType.Interest;
			}
			if("Umbuchungen".equals(filter)) {
				return tx.getType() == TransactionType.Transfer;
			}
			if("Ausgaben".equals(filter)) {
				return tx.getType() == TransactionType.Expenses;
			}
			else {
			throw new IllegalArgumentException("Unbekannter Filter");
			}
		}
		private boolean matchesYearFilter(Transaction tx, String yearFilter) {
			if("Alle Jahre".equals(yearFilter)) {
				return true;
			}
			if("2023".equals(yearFilter)) {
				return Integer.toString(tx.getDate().getYear()).equals("2023");
			}
			if("2024".equals(yearFilter)){
				return Integer.toString(tx.getDate().getYear()).equals("2024");
			}
			if("2025".equals(yearFilter)){
				return Integer.toString(tx.getDate().getYear()).equals("2025");
			}
			if("2026".equals(yearFilter)){
				return Integer.toString(tx.getDate().getYear()).equals("2026");
			}
			if("2027".equals(yearFilter)){
				return Integer.toString(tx.getDate().getYear()).equals("2027");
			}
			if("2028".equals(yearFilter)){
				return Integer.toString(tx.getDate().getYear()).equals("2028");
			}
			if("2029".equals(yearFilter)){
				return Integer.toString(tx.getDate().getYear()).equals("2029");
			}
			if("2030".equals(yearFilter)){
				return Integer.toString(tx.getDate().getYear()).equals("2030");
			}else {
				throw new IllegalArgumentException("unbekanntes Jahr");
			}
		}
		private boolean matchesMonthFilter(Transaction tx, String monthFilter) {
			if("Alle Monate".equals(monthFilter)) {
				return true;
			}
			if("Januar".equals(monthFilter)) {
				return "JANUARY".equals(tx.getDate().getMonth().toString());
			}
			if("Februar".equals(monthFilter)) {
				return "FEBRUARY".equals(tx.getDate().getMonth().toString());
			}
			if("März".equals(monthFilter)) {
				return "MARCH".equals(tx.getDate().getMonth().toString());
			}
			if("April".equals(monthFilter)) {
				return "APRIL".equals(tx.getDate().getMonth().toString());
			}
			if("Mai".equals(monthFilter)) {
				return "MAY".equals(tx.getDate().getMonth().toString());
			}
			if("Juni".equals(monthFilter)) {
				return "JUNE".equals(tx.getDate().getMonth().toString());
			}
			if("Juli".equals(monthFilter)) {
				return "JULY".equals(tx.getDate().getMonth().toString());
			}
			if("August".equals(monthFilter)) {
				return "AUGUST".equals(tx.getDate().getMonth().toString());
			}
			if("September".equals(monthFilter)) {
				return "SEPTEMBER".equals(tx.getDate().getMonth().toString());
			}
			if("Oktober".equals(monthFilter)) {
				return "OCTOBER".equals(tx.getDate().getMonth().toString());
			}
			if("November".equals(monthFilter)) {
				return "NOVEMBER".equals(tx.getDate().getMonth().toString());
			}
			if("Dezember".equals(monthFilter)) {
				return "DECEMBER".equals(tx.getDate().getMonth().toString());
			}
			else {
				throw new IllegalArgumentException("unbekannter Monat");
				
			}
		}
		
		private void updateFilterMode() {
		    String year = view.getYearFilterBox().getValue();

		    boolean showMonthFilter = year != null && !"Alle Jahre".equals(year);
		  
		    if(!showMonthFilter) {
		    	view.getMonthFilterBox().setValue("Alle Monate");
		    }
		    showRow(view.getMonthFilterRow(), showMonthFilter);
		}
		
		
		
		
		
		// Transaktion löschen
		private void handleDelete() {
		    UiEntry selectedEntry = view.getEntriesTable().getSelectionModel().getSelectedItem();

		    if (selectedEntry == null) {
		        view.getStatusLabel().setText("Bitte eine Buchung auswählen");
		        return;
		    }

		    if (selectedEntry.isMonthSummary() || selectedEntry.getManagerIndex() == null) {
		        view.getStatusLabel().setText("Monatsbilanzen können nicht gelöscht werden");
		        return;
		    }

		    int managerIndex = selectedEntry.getManagerIndex();

		    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		    alert.setTitle("Löschen bestätigen");
		    alert.setHeaderText("Buchung wirklich löschen?");
		    alert.setContentText("Die ausgewählte Buchung wird dauerhaft entfernt.");

		    var result = alert.showAndWait();
		    if (result.isEmpty() || result.get() != ButtonType.OK) {
		        view.getStatusLabel().setText("Löschen abgebrochen.");
		        return;
		    }

		    manager.removeTransactionAt(managerIndex);
		    editMode = false;
		    editingManagerIndex = -1;
		    afterSuccessfulSubmit();

		    view.getStatusLabel().setText("Buchung wurde gelöscht.");
		}
		private int getSelectedManagerIndex() {
		    UiEntry selectedEntry = view.getEntriesTable().getSelectionModel().getSelectedItem();

		    if (selectedEntry == null) {
		        return -1;
		    }

		    if (selectedEntry.isMonthSummary() || selectedEntry.getManagerIndex() == null) {
		        return -1;
		    }

		    return selectedEntry.getManagerIndex();
		}
		
		private void loadSelectedTransactionIntoForm() {
		    int managerIndex = getSelectedManagerIndex();

		    if (managerIndex < 0) {
		        view.getStatusLabel().setText("Bitte eine echte Buchung auswählen");
		        return;
		    }

		    Transaction tx = manager.getAllTransactions().get(managerIndex);

		    editingManagerIndex = managerIndex;
		    editMode = true;

		    view.getBeschreibung().setText(tx.getDescription());
		    view.getDatumPicker().setValue(tx.getDate());

		    if (tx.getType() == TransactionType.Interest) {
		        view.getTypBox().setValue("Zinsen");
		        updateFormMode();

		        view.getGrossInterestField().setText(moneyUtil.formatAmount(tx.getGrossInterestRate()));
		        view.getTaxBerndField().setText(moneyUtil.formatAmount(tx.getTaxBernd()));

		        view.getMonthBox().setValue(tx.getDate().getMonth());
		        view.getYearSpinner().getValueFactory().setValue(tx.getDate().getYear());

		        view.getBetrag().clear();
		        view.getOwnerBox().setValue(null);
		        view.getTransferBox().setValue(null);

		    }else if (tx.getType() == TransactionType.Transfer) {
		        view.getTypBox().setValue("Umbuchung");
		        updateFormMode();

		        view.getBetrag().setText(moneyUtil.formatAmount(tx.getTobiasSplit().abs()));

		        if (tx.getTobiasSplit().compareTo(BigDecimal.ZERO) < 0
		                && tx.getBerndSplit().compareTo(BigDecimal.ZERO) > 0) {
		            view.getTransferBox().setValue(AccountView.TRANSFER_TOBIAS_BERND);
		        } else {
		            view.getTransferBox().setValue(AccountView.TRANSFER_BERND_TOBIAS);
		        }

		        view.getOwnerBox().setValue(null);
		        view.getGrossInterestField().clear();
		        view.getTaxBerndField().clear();
		        view.getMonthBox().setValue(null);

		    } else {
		        if (tx.getType() == TransactionType.Deposit) {
		            view.getTypBox().setValue("Einnahme");
		        } else if (tx.getType() == TransactionType.Expenses) {
		            view.getTypBox().setValue("Ausgabe");
		        } else {
		            view.getTypBox().setValue(tx.getType().toString());
		        }

		        updateFormMode();

		        view.getBetrag().setText(moneyUtil.formatAmount(tx.getAccountEffekt().abs()));

		        if (tx.getTobiasSplit().compareTo(tx.getAccountEffekt()) == 0
		                && tx.getBerndSplit().compareTo(BigDecimal.ZERO) == 0) {
		            view.getOwnerBox().setValue("Tobias");
		        } else if (tx.getBerndSplit().compareTo(tx.getAccountEffekt()) == 0
		                && tx.getTobiasSplit().compareTo(BigDecimal.ZERO) == 0) {
		            view.getOwnerBox().setValue("Bernd");
		        } else {
		            view.getOwnerBox().setValue("Gemeinsam");
		        }

		        view.getTransferBox().setValue(null);
		        view.getGrossInterestField().clear();
		        view.getTaxBerndField().clear();
		        view.getMonthBox().setValue(null);
		    }

		    view.getSubmitButton().setText("Änderung speichern");
		    showButton(view.getCancelEditButton(),true);
		    view.getCancelEditButton().setDisable(false);
		    showButton(view.getDeleteButton(),true);
		    showButton(view.getEditButton(),false);
		    view.getStatusLabel().setText("Bearbeitungsmodus aktiv");
		}
		
		
		// Aktualliseren der Kontostände
		private void refreshBalanceLabels() {
		    boolean ausblenden = view.getBalanceCheckBox().isSelected();

		    if (ausblenden) {
		        view.getBalanceCheckBox().setText("Kontostände einblenden");

		        view.getGesamtLabel().setText("••••••");
		        view.getTobiasLabel().setText("••••••");
		        view.getBerndLabel().setText("••••••");

		        view.getGesamtImpactLabel().setText("");
		        view.getTobiasImpactLabel().setText("");
		        view.getBerndImpactLabel().setText("");
		        return;
		    }

		    view.getBalanceCheckBox().setText("Kontostände ausblenden");

		    view.getGesamtLabel().setText(moneyUtil.formatAmount(manager.getCurrentAccountBalance()));
		    view.getTobiasLabel().setText(moneyUtil.formatAmount(manager.getCurrentTobiasBalance()));
		    view.getBerndLabel().setText(moneyUtil.formatAmount(manager.getCurrentBerndBalance()));

		    BigDecimal gesamtImpact = BigDecimal.ZERO;
		    BigDecimal tobiasImpact = BigDecimal.ZERO;
		    BigDecimal berndImpact = BigDecimal.ZERO;

		    List<Transaction> transactions = manager.getAllTransactions();
		    if (!transactions.isEmpty()) {
		        Transaction last = transactions.get(transactions.size() - 1);
		        gesamtImpact = last.getAccountEffekt();
		        tobiasImpact = last.getTobiasSplit();
		        berndImpact = last.getBerndSplit();
		    }

		    updateImpactLabel(view.getGesamtImpactLabel(), gesamtImpact);
		    updateImpactLabel(view.getTobiasImpactLabel(), tobiasImpact);
		    updateImpactLabel(view.getBerndImpactLabel(), berndImpact);
		}
		
		private void updateImpactLabel(Label label, BigDecimal amount) {
		    boolean positiveOrZero = amount.compareTo(BigDecimal.ZERO) >= 0;
		    String arrow = positiveOrZero ? "▲ " : "▼ ";
		    String sign = positiveOrZero ? "+" : "";

		    label.setText(arrow + sign + moneyUtil.formatAmount(amount));

		    label.getStyleClass().removeAll("balance-impact-positive", "balance-impact-negative");
		    label.getStyleClass().add(positiveOrZero ? "balance-impact-positive" : "balance-impact-negative");
		}
		
		
		
		
		private void showRow(HBox row, boolean visible) {
		    row.setVisible(visible);
		    row.setManaged(visible);
		}
		private void showButton(Button button, boolean visible) {
			button.setVisible(visible);
			button.setManaged(visible);
		}
		
		//Intelligente Oberfläche
		private void updateFormMode() {
		    String type = view.getTypBox().getValue();
		    
		    if ("Umbuchung".equals(type)) {
		        showRow(view.getOwnerRow(), false);
		        showRow(view.getTransferRow(), true);
		        showRow(view.getAmountRow(), true);
		        showRow(view.getGrossInterestRow(), false);
		        showRow(view.getTaxBerndRow(), false);
		        showRow(view.getInterestMonthRow(), false);

		        view.getOwnerBox().setValue(null);
		        view.getGrossInterestField().clear();
		        view.getTaxBerndField().clear();
		        view.getMonthBox().setValue(null);
		        
		        view.setFormTitleText("Buchung");
		        view.getLabelGrossInterest().setText("Erhaltene Brutto Zinsen:");
		        view.getLabelTaxBernd().setText("Steuern Bernd:");
		        view.getLabelInterestMonth().setText("Monat an dem Zinsen angefallen sind:");

		    } else if ("Zinsen".equals(type)) {
		        showRow(view.getOwnerRow(), false);
		        showRow(view.getTransferRow(), false);
		        showRow(view.getAmountRow(), false);
		        showRow(view.getGrossInterestRow(), true);
		        showRow(view.getTaxBerndRow(), true);
		        showRow(view.getInterestMonthRow(), true);
		       
		        view.setFormTitleText("Zinsbuchung");
		        view.getLabelGrossInterest().setText("Bruttozinsen:");
		        view.getLabelTaxBernd().setText("Steuer Bernd:");
		        view.getLabelInterestMonth().setText("Zinsmonat:");

		        view.getOwnerBox().setValue(null);
		        view.getTransferBox().setValue(null);
		        view.getBetrag().clear();

		        if (view.getBeschreibung().getText().isBlank()) {
		            view.getBeschreibung().setText("Zinsen");
		        }

		    } else {
		        showRow(view.getOwnerRow(), true);
		        showRow(view.getTransferRow(), false);
		        showRow(view.getAmountRow(), true);
		        showRow(view.getGrossInterestRow(), false);
		        showRow(view.getTaxBerndRow(), false);
		        showRow(view.getInterestMonthRow(), false);

		        view.getTransferBox().setValue(null);
		        view.getGrossInterestField().clear();
		        view.getTaxBerndField().clear();
		        view.getMonthBox().setValue(null);
		        view.setFormTitleText("Buchung");
		        view.getLabelGrossInterest().setText("Erhaltene Brutto Zinsen:");
		        view.getLabelTaxBernd().setText("Steuern Bernd:");
		        view.getLabelInterestMonth().setText("Monat an dem Zinsen angefallen sind:");
		    }
		}
		private void clearForm() {
		    view.getBeschreibung().clear();
		    view.getBetrag().clear();
		    view.getGrossInterestField().clear();
		    view.getTaxBerndField().clear();
		    view.getDatumPicker().setValue(null);

		    view.getEnteredByBox().getSelectionModel().clearSelection();
		    view.getEnteredByBox().setValue(null);

		    view.getTypBox().getSelectionModel().clearSelection();
		    view.getTypBox().setValue(null);

		    view.getOwnerBox().getSelectionModel().clearSelection();
		    view.getOwnerBox().setValue(null);

		    view.getTransferBox().getSelectionModel().clearSelection();
		    view.getTransferBox().setValue(null);

		    view.getMonthBox().getSelectionModel().clearSelection();
		    view.getMonthBox().setValue(null);

		    view.getStatusLabel().setText("");
		    view.getSubmitButton().setText("Abschicken");
		    view.getCancelEditButton().setDisable(true);

		    view.getStatusLabel().setText("");
		    view.getSubmitButton().setText("Abschicken");
		    view.getCancelEditButton().setDisable(true);
		    
		    showButton(view.getDeleteButton(),false);
		    showButton(view.getCancelEditButton(), false);
		    showButton(view.getEditButton(),true);
		    
		    view.getEditButton().setDisable(false);
		    
		    editMode = false;
		    editingManagerIndex = -1;

		    updateFormMode();
		   
	}
		private Path getSavePath() {
		    return Path.of(currentProfile.getFileName());
		}
		
		private void loadCurrentProfileFromFile() {
		    try {
		        List<Transaction> loadedTransactions = fileService.loadTransactions(getSavePath());
		        manager.replaceAllTransactions(loadedTransactions);

		        refreshEntriesFromManager();
		        refreshBalanceLabels();
		        clearForm();
		        view.getStatusLabel().setText("Konto " + currentProfile.getDisplayName() + " geladen.");
		    } catch (IOException e) {
		        view.getStatusLabel().setText("Fehler beim Laden der Datei");
		        e.printStackTrace();
		    }
		}

		public TransactionManager getManager() {
			return manager;
		}
}
