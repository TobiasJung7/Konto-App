package de.tobias.kontoapp.ui;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;

import Konto.Transaction;
import Konto.TransactionManager;
import Konto.TransactionType;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.nio.file.Path;
import de.tobias.kontoapp.persistence.TransactionFileService;

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
    TransactionFileService fileService = new TransactionFileService();
    Path savePath = Path.of("transactions.csv");
    
    
    public AccountController(
            AccountView view,
            ObservableList<UiEntry> entries,
            TransactionManager manager,
            TransactionBuilder transactionBuilder,
            InterestTransactionBuilder interestTransactionBuilder,
            UiEntryMapper uiEntryMapper,
            MoneyUtil moneyUtil,
            TransactionFileService fileService,
            Path savePath
    ) {
        this.view = view;
        this.entries = entries;
        this.manager = manager;
        this.transactionBuilder = transactionBuilder;
        this.interestTransactionBuilder = interestTransactionBuilder;
        this.uiEntryMapper = uiEntryMapper;
        this.moneyUtil = moneyUtil;
        this.fileService = fileService;
        this.savePath = savePath;

        this.view.getEntriesTable().setItems(entries);
        this.view.getSubmitButton().setOnAction(e -> handleSubmit());
        this.view.getDeleteButton().setOnAction(e -> handleDelete());
        this.view.getTypBox().setOnAction(e -> updateFormMode());
        this.view.getCancelEditButton().setOnAction(e -> clearForm());
        this.view.getEditButton().setOnAction(e -> loadSelectedTransactionIntoForm() );
        this.view.getEntriesTable().setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                loadSelectedTransactionIntoForm();
            }
        });
        clearForm();
        updateFormMode();
        refreshEntriesFromManager();
        refreshBalanceLabels();
    }
		
	
	//refresh Entries From Manager
		private void refreshEntriesFromManager() {
			entries.clear();
			
			
			for (Transaction tx : manager.getAllTransactions()) {
			    UiEntry entry = uiEntryMapper.mapTransactionToEntry(tx);
			    entries.add(0, entry);
			}
				
			}
			
		
	
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
		                    taxBernd
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
		        refreshEntriesFromManager();
		        refreshBalanceLabels();
		        clearForm();
		        refreshEntriesFromManager();
		        refreshBalanceLabels();
		        clearForm();
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
		                amount
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
		   saveToFile();
		   refreshEntriesFromManager();
		   refreshBalanceLabels();
		   clearForm();
		    
		}
		
		// Transaktion löschen
		private void handleDelete() {
			    int selectedIndex = view.getEntriesTable().getSelectionModel().getSelectedIndex();

			    if (selectedIndex < 0) {
			        view.getStatusLabel().setText("Bitte eine Buchung auswählen");
			        return;
			    }

			    int managerIndex = manager.getAllTransactions().size() - 1 - selectedIndex;

			   
			    manager.removeTransactionAt(managerIndex);
			    saveToFile();
			    refreshEntriesFromManager();
			    refreshBalanceLabels();
			    view.getStatusLabel().setText("");
			
		}
		// Aktualliseren der Kontostände
		private void refreshBalanceLabels() {
			view.getGesamtLabel().setText( moneyUtil.formatAmount(manager.getCurrentAccountBalance()) + " €");
			view.getTobiasLabel().setText( moneyUtil.formatAmount(manager.getCurrentTobiasBalance()) + " €");
			view.getBerndLabel().setText( moneyUtil.formatAmount(manager.getCurrentBerndBalance()) + " €");
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
			
		private void showRow(HBox row, boolean visible) {
		    row.setVisible(visible);
		    row.setManaged(visible);
		}
		private void showButton(Button button, boolean visible) {
			button.setVisible(visible);
			button.setManaged(visible);
		}
		
		private void clearForm() {
			
			    view.getBeschreibung().clear();
			    view.getBetrag().clear();
			    view.getGrossInterestField().clear();
			    view.getTaxBerndField().clear();

			    view.getTypBox().setValue(null);
			    view.getOwnerBox().setValue(null);
			    view.getTransferBox().setValue(null);
			    view.getDatumPicker().setValue(null);
			    view.getMonthBox().setValue(null);

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
			    view.getBeschreibung().requestFocus();
		}
		public TransactionManager getManager() {
			return manager;
		}
		
		private String validateInterestInput() {
			if(view.getBeschreibung().getText().isBlank() || view.getDatumPicker().getValue() == null || view.getGrossInterestField().getText().isBlank() || view.getMonthBox().getValue() == null || view.getYearSpinner().getValue() == null) {
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
		            || view.getBetrag().getText().isBlank()) {
		        return "Bitte alle Pflichtfelder ausfüllen";
		    }

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
		
	
	private void loadSelectedTransactionIntoForm() {
	    int managerIndex = getSelectedManagerIndex();

	    if (managerIndex < 0) {
	        view.getStatusLabel().setText("Bitte eine Buchung auswählen");
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

	    } else if (tx.getType() == TransactionType.Transfer) {
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
	private int getSelectedManagerIndex() {
	    int selectedTableIndex = view.getEntriesTable().getSelectionModel().getSelectedIndex();

	    if (selectedTableIndex < 0) {
	        return -1;
	    }

	    return manager.getAllTransactions().size() - 1 - selectedTableIndex;
	}
	
	private void saveToFile() {
	    try {
	        fileService.saveTransactions(savePath, manager.getAllTransactions());
	    } catch (IOException e) {
	        view.getStatusLabel().setText("Fehler beim Speichern der Datei");
	        e.printStackTrace();
	    }
	}

}
