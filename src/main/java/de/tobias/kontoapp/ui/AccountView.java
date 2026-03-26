package de.tobias.kontoapp.ui;

import java.math.RoundingMode;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import de.tobias.kontoapp.ui.table.UiEntry;
import de.tobias.kontoapp.util.MoneyUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.control.CheckBox;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class AccountView {

    public static final String TRANSFER_TOBIAS_BERND = "Tobias -> Bernd";
    public static final String TRANSFER_BERND_TOBIAS = "Bernd  -> Tobias";

    private VBox root;

    private Label labelBeschreibung;
    private TextField beschreibung;

    private Label labelBetrag;
    private TextField betrag;

    private TableView<UiEntry> entriesTable;

    private Label statusLabel;
    private Label formTitle;

    private Button submitButton;
    private Button deleteButton;
    private Button editButton;
    private Button cancelEditButton;
    private Button clearButton;

    private ComboBox<String> typBox;
    private Label labelTyp;

    private ComboBox<String> ownerBox;
    private Label ownerLabel;

    private ComboBox<String> transferBox;
    private Label transferLabel;
    
    private ComboBox<String> enteredByBox;
    private Label enteredByLabel;

    private Label labelDatum;
    private DatePicker datumPicker;
    
    private Label labelGrossInterest;
    private TextField grossInterestField;
    
    private Label labelTaxBernd;
    private TextField taxBerndField;
    
    private Label labelInterestMonth;
    private ComboBox <Month> monthBox;
    private Spinner<Integer> yearSpinner;
    
    private VBox werteBalanceBox;
    private VBox namenBalanceBox;

    private Label gesamtLabel;
    private Label tobiasLabel;
    private Label berndLabel;
    private Label tobias;
    private Label bernd;
    private Label gesamt;
    private Label gesamtImpactLabel;
    private Label tobiasImpactLabel;
    private Label berndImpactLabel;
    
    private HBox ownerRow;
    private HBox typRow;
    private HBox transferRow;
    private HBox grossInterestRow;
    private HBox taxBerndRow;
    private HBox interestMonthRow;
    private HBox descriptionRow;
    private HBox amountRow;
    private HBox dateRow;
    private HBox enteredByRow;
    
    private CheckBox balanceCheckBox;
    
    private ComboBox<String> personFilterBox;
    private ComboBox<String> typFilterBox;
    private ComboBox<String> monthFilterBox;
    private ComboBox<String> yearFilterBox;
    private HBox monthFilterRow;
    private HBox personFilterRow;
    private HBox typFilterRow;
    private HBox yearFilterRow;

    

    public AccountView() {
        buildUi();
    }
    
    private static final DateTimeFormatter GERMAN_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMAN);

    private void buildUi() {
    	MoneyUtil moneyUtil = new MoneyUtil();
        entriesTable = new TableView<>();

        TableColumn<UiEntry, String> dateCol = new TableColumn<>("Datum");
        
        dateCol.setCellValueFactory(cellData -> {
        	if (cellData.getValue().getDate() == null) {
        		return new SimpleStringProperty("");
        	}
        	return new SimpleStringProperty(
        			cellData.getValue().getDate().format(GERMAN_DATE_FORMATTER)
               );
        });
        dateCol.setPrefWidth(110);
        dateCol.setSortable(false);

        TableColumn<UiEntry, String> typeCol = new TableColumn<>("Typ");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(130);
        typeCol.setSortable(false);

        TableColumn<UiEntry, String> amountCol = new TableColumn<>("Betrag");
        amountCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                   moneyUtil.formatAmount(cellData.getValue().getAmount())
                )
        );
        amountCol.setPrefWidth(130);
        amountCol.setSortable(false);

        TableColumn<UiEntry, String> ownerCol = new TableColumn<>("Aufteilung");
        ownerCol.setCellValueFactory(new PropertyValueFactory<>("owner"));
        ownerCol.setPrefWidth(130);
        ownerCol.setSortable(false);

        TableColumn<UiEntry, String> detailsCol = new TableColumn<>("Details");
        detailsCol.setCellValueFactory(new PropertyValueFactory<>("details"));
        detailsCol.setSortable(false);
        
        
        detailsCol.setCellFactory(col -> {
            TableCell<UiEntry, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item);
                }
            };
            cell.setWrapText(true);
            return cell;
        });

        TableColumn<UiEntry, String> descriptionCol = new TableColumn<>("Beschreibung");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionCol.setSortable(false);
        
        double fixedWidth = 110 + 130 + 130 + 130;

        detailsCol.prefWidthProperty().bind(
            Bindings.createDoubleBinding(
                () -> Math.max(180, (entriesTable.getWidth() - fixedWidth )/ 2),
                entriesTable.widthProperty()
            )
        );

        descriptionCol.prefWidthProperty().bind(
            Bindings.createDoubleBinding(
                () -> Math.max(180, (entriesTable.getWidth() - fixedWidth - 40) / 2),
                entriesTable.widthProperty()
            )
        );
        

        entriesTable.getColumns().addAll(dateCol, typeCol, amountCol, ownerCol, detailsCol, descriptionCol);
        entriesTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        entriesTable.setPrefHeight(340);
        entriesTable.setPlaceholder(new Label("Noch keine Buchungen vorhanden"));
        entriesTable.setSortPolicy(table -> false);
        
        personFilterBox = new ComboBox<>();
        personFilterBox.getItems().addAll("Alle","Tobias", "Bernd", "Gemeinsam");
        personFilterBox.setValue("Alle");
        Label personFilterLabel = new Label("Person:");
        personFilterRow = new HBox(20, personFilterLabel, personFilterBox);
 
        typFilterBox = new ComboBox<>();
        typFilterBox.getItems().addAll("Alle","Einnahmen","Ausgaben", "Umbuchungen", "Zinsen" );
        typFilterBox.setValue("Alle");
        Label typFilterLabel = new Label("Transaktions Typ:");
        typFilterRow = new HBox(20, typFilterLabel, typFilterBox);
        
        yearFilterBox = new ComboBox<>();
        yearFilterBox.getItems().addAll("Alle Jahre", "2023", "2024","2025","2026","2027","2028","2029","2030");
        yearFilterBox.setValue("Alle Jahre");
        Label yearFilterLabel = new Label ("Jahr: ");
        yearFilterRow = new HBox(20, yearFilterLabel, yearFilterBox);
        
        monthFilterBox = new ComboBox<>();
        monthFilterBox.getItems().addAll("Alle Monate", "Januar", "Februar","März","April", "Mai", "Juni", "Juli","August", "September", "Oktober", "November", "Dezember");
        monthFilterBox.setValue("Alle Monate");
        Label monthFilterLabel = new Label("Monat: ");
        monthFilterRow = new HBox(20, monthFilterLabel, monthFilterBox);
        
        HBox filterRow = new HBox(personFilterRow, typFilterRow, yearFilterRow, monthFilterRow);
        filterRow.setSpacing(30);
        
        
        gesamtLabel = new Label();
        tobiasLabel = new Label();
        berndLabel = new Label();

        gesamt = new Label("Gesamt:");
        tobias = new Label("Tobias:");
        bernd = new Label("Bernd:");

        gesamtImpactLabel = new Label();
        tobiasImpactLabel = new Label();
        berndImpactLabel = new Label();

        balanceCheckBox = new CheckBox("Kontostände ausblenden");
        balanceCheckBox.setSelected(false);

        HBox gesamtRow = createBalanceRow(gesamt, gesamtLabel, gesamtImpactLabel);
        HBox tobiasRow = createBalanceRow(tobias, tobiasLabel, tobiasImpactLabel);
        HBox berndRow = createBalanceRow(bernd, berndLabel, berndImpactLabel);

        VBox balanceRowsBox = new VBox(8, gesamtRow, tobiasRow, berndRow);
        VBox balanceContent = new VBox(10, balanceRowsBox, balanceCheckBox);
        balanceContent.setAlignment(Pos.CENTER_LEFT);
        
        enteredByBox = new ComboBox<>();
        enteredByLabel = new Label("Eingetragen von: ");
        enteredByBox.setPromptText("Bitte wählen");
        enteredByBox.getItems().addAll("Tobias", "Bernd", "Anderer");
        
        ownerLabel = new Label("Zuordnung: ");
        ownerBox = new ComboBox<>();
        ownerBox.setPromptText("Bitte wählen");
        ownerBox.getItems().addAll("Tobias", "Bernd", "Gemeinsam");

        transferLabel = new Label("Umbuchung: ");
        transferBox = new ComboBox<>();
        transferBox.setPromptText("Bitte wählen");
        transferBox.getItems().addAll(TRANSFER_TOBIAS_BERND, TRANSFER_BERND_TOBIAS);

        labelBeschreibung = new Label("Beschreibung: ");
        beschreibung = new TextField();
        beschreibung.setPromptText("z.B. Miete");

        labelBetrag = new Label("Betrag: ");
        betrag = new TextField();
        betrag.setPromptText("z.B. 450 €");

        labelTyp = new Label("Typ: ");
        typBox = new ComboBox<>();
        typBox.setPromptText("Bitte wählen");
        typBox.getItems().addAll("Einnahme", "Ausgabe", "Umbuchung", "Zinsen");

        
        labelGrossInterest = new Label("Bruttozinsen: ");
        grossInterestField = new TextField();
        grossInterestField.setPromptText("z.B. 150 €");

        labelTaxBernd = new Label("Steuer Bernd: ");
        taxBerndField = new TextField();
        taxBerndField.setPromptText("z.B. 50 €");

        labelInterestMonth = new Label("Zinsmonat: ");
        monthBox = new ComboBox<>();
        monthBox.getItems().addAll(Month.values());
        monthBox.setPromptText("Bitte wählen");

        yearSpinner = new Spinner<>(1900, 2100, 2026);
        yearSpinner.setEditable(true);

        labelDatum = new Label("Datum: ");
        datumPicker = new DatePicker();
        datumPicker.setPromptText("Bitte wählen");

        statusLabel = new Label();
        statusLabel.setTextFill(Color.RED);

        submitButton = new Button("Abschicken");
        submitButton.setDefaultButton(true);

        deleteButton = new Button("Ausgewählten Eintrag löschen");
        editButton = new Button("Ausgewählte Buchung bearbeiten");
        cancelEditButton = new Button("Bearbeiten abbrechen");
        cancelEditButton.setDisable(true);
        
        clearButton = new Button("Formular leeren");
        clearButton.setDisable(false);

        enteredByRow = new HBox(15, enteredByLabel, enteredByBox);
        typRow = new HBox(15, labelTyp, typBox);
        ownerRow = new HBox(15, ownerLabel, ownerBox);
        transferRow = new HBox(15, transferLabel, transferBox);
        grossInterestRow = new HBox(15, labelGrossInterest, grossInterestField);
        taxBerndRow = new HBox(15, labelTaxBernd, taxBerndField);
        interestMonthRow = new HBox(8, labelInterestMonth, monthBox, yearSpinner);
        descriptionRow = new HBox(15, labelBeschreibung, beschreibung);
        amountRow = new HBox(15, labelBetrag, betrag);
        dateRow = new HBox(15, labelDatum, datumPicker);

        enteredByLabel.setMinWidth(150);
        labelTyp.setMinWidth(150);
        ownerLabel.setMinWidth(150);
        transferLabel.setMinWidth(150);
        labelGrossInterest.setMinWidth(150);
        labelTaxBernd.setMinWidth(150);
        labelInterestMonth.setMinWidth(150);
        labelBeschreibung.setMinWidth(150);
        labelBetrag.setMinWidth(150);
        labelDatum.setMinWidth(150);
        
        double selectionFieldWidth = 180;
        double yearSpinnerWidth = 100;
        double monthYearGap = 8;
        double descriptionWidth = selectionFieldWidth + monthYearGap + yearSpinnerWidth;

        enteredByBox.setMinWidth(selectionFieldWidth);
        typBox.setPrefWidth(selectionFieldWidth);
        ownerBox.setPrefWidth(selectionFieldWidth);
        transferBox.setPrefWidth(selectionFieldWidth);
        grossInterestField.setPrefWidth(selectionFieldWidth);
        taxBerndField.setPrefWidth(selectionFieldWidth);
        datumPicker.setPrefWidth(selectionFieldWidth);
        betrag.setPrefWidth(selectionFieldWidth);
        
        monthBox.setPrefWidth(selectionFieldWidth);
        yearSpinner.setPrefWidth(yearSpinnerWidth);
        
        beschreibung.setPrefWidth(descriptionWidth);
        
        
        beschreibung.setPrefHeight(36);
        datumPicker.setPrefHeight(36);
        monthBox.setPrefHeight(36);
        yearSpinner.setPrefHeight(36);

        Label balanceTitle = new Label("Kontostände");
        formTitle = new Label("Buchung");
        Label tableTitle = new Label("Buchungen");

        HBox buttonRow = new HBox(10, submitButton, editButton, cancelEditButton, clearButton, deleteButton);

        VBox balanceCard = createCard(balanceTitle, balanceContent);
        VBox formCard = createCard(
        		
                formTitle,
                enteredByRow,
                typRow,
                ownerRow,
                transferRow,
                grossInterestRow,
                taxBerndRow,
                interestMonthRow,
                amountRow,
                dateRow,
                descriptionRow,
                statusLabel,
                buttonRow
        );
        VBox tableCard = createCard(tableTitle, filterRow, entriesTable);
        
        entriesTable.getStyleClass().add("entries-table");

        statusLabel.getStyleClass().add("status-label");

        balanceTitle.getStyleClass().add("card-title");
        formTitle.getStyleClass().add("card-title");
        tableTitle.getStyleClass().add("card-title");

        entriesTable.getStyleClass().add("entries-table");

        submitButton.getStyleClass().add("primary-button");
        editButton.getStyleClass().add("secondary-button");
        cancelEditButton.getStyleClass().add("secondary-button");
        deleteButton.getStyleClass().add("danger-button");
        clearButton.getStyleClass().add("secondary-button");

        buttonRow.getStyleClass().add("button-row");
        
        
        balanceCheckBox.getStyleClass().add("konto-check-box");
        

        gesamtLabel.getStyleClass().add("balance-label");
        tobiasLabel.getStyleClass().add("balance-label");
        berndLabel.getStyleClass().add("balance-label");
        tobias.getStyleClass().add("balance-label");
        gesamt.getStyleClass().add("balance-label");
        bernd.getStyleClass().add("balance-label");
        gesamtImpactLabel.getStyleClass().add("balance-impact");
        tobiasImpactLabel.getStyleClass().add("balance-impact");
        berndImpactLabel.getStyleClass().add("balance-impact");


        typRow.getStyleClass().add("input-row");
        ownerRow.getStyleClass().add("input-row");
        transferRow.getStyleClass().add("input-row");
        grossInterestRow.getStyleClass().add("input-row");
        taxBerndRow.getStyleClass().add("input-row");
        interestMonthRow.getStyleClass().add("input-row");
        descriptionRow.getStyleClass().add("input-row");
        amountRow.getStyleClass().add("input-row");
        dateRow.getStyleClass().add("input-row");
        enteredByRow.getStyleClass().add("input-row");
        
        typBox.getStyleClass().add("konto-combo-box");
        ownerBox.getStyleClass().add("konto-combo-box");
        monthBox.getStyleClass().add("konto-combo-box");
        transferBox.getStyleClass().add("konto-combo-box");
        enteredByBox.getStyleClass().add("konto-combo-box");
       
        yearSpinner.getStyleClass().add("konto-spinner");
  
        beschreibung.getStyleClass().add("konto-input-field");
        betrag.getStyleClass().add("konto-input-field");
        grossInterestField.getStyleClass().add("konto-input-field");
        taxBerndField.getStyleClass().add("konto-input-field");
       
        gesamtImpactLabel.getStyleClass().add("balance-impact");
        tobiasImpactLabel.getStyleClass().add("balance-impact");
        berndImpactLabel.getStyleClass().add("balance-impact");
        
        personFilterBox.getStyleClass().add("konto-combo-box");
        typFilterBox.getStyleClass().add("konto-combo-box");
        yearFilterBox.getStyleClass().add("konto-combo-box");
        monthFilterBox.getStyleClass().add("konto-combo-box");
        
        personFilterRow.getStyleClass().add("input-row");
        typFilterRow.getStyleClass().add("input-row");
        yearFilterRow.getStyleClass().add("input-row");
        monthFilterRow.getStyleClass().add("input-row");
        
        entriesTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(UiEntry item, boolean empty) {
                super.updateItem(item, empty);

                getStyleClass().remove("month-summary-row");

                if (empty || item == null) {
                    setPrefHeight(32);
                } else if (item.isMonthSummary()) {
                    getStyleClass().add("month-summary-row");
                    setPrefHeight(56);
                } else {
                    setPrefHeight(32);
                }
            }
        });
        
        
        
        balanceCard.getStyleClass().add("card");
        formCard.getStyleClass().add("card");
        tableCard.getStyleClass().add("card");

        root = new VBox(16, balanceCard, formCard, tableCard);
        root.getStyleClass().add("root-pane");
        root.setFocusTraversable(true);
    }
    public Button getClearButton() {
		return clearButton;
	}
	public void setClearButton(Button clearButton) {
		this.clearButton = clearButton;
	}
	public ComboBox<String> getEnteredByBox() {
		return enteredByBox;
	}
	public Label getEnteredByLabel() {
		return enteredByLabel;
	}
	public HBox getEnteredByRow() {
		return enteredByRow;
	}
	public HBox getPersonFilterRow() {
		return personFilterRow;
	}
	public HBox getTypFilterRow() {
		return typFilterRow;
	}
	public HBox getYearFilterRow() {
		return yearFilterRow;
	}
	public HBox getMonthFilterRow() {
		return monthFilterRow;
	}

	public ComboBox<String> getMonthFilterBox() {
		return monthFilterBox;
	}

	public ComboBox<String> getYearFilterBox() {
		return yearFilterBox;
	}

	public ComboBox<String> getPersonFilterBox() {
		return personFilterBox;
	}
    

	public ComboBox<String> getTypFilterBox() {
		return typFilterBox;
	}

	private HBox createBalanceRow(Label nameLabel, Label valueLabel, Label impactLabel) {
        nameLabel.setMinWidth(80);
        valueLabel.setMinWidth(80);

        HBox row = new HBox(4, nameLabel, valueLabel, impactLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    public Label getGesamtImpactLabel() {
		return gesamtImpactLabel;
	}

	public Label getTobiasImpactLabel() {
		return tobiasImpactLabel;
	}

	public Label getBerndImpactLabel() {
		return berndImpactLabel;
	}

	public Label getFormTitle() {
		return formTitle;
	}

	public VBox getWerteBalanceBox() {
		return werteBalanceBox;
	}

	public VBox getNamenBalanceBox() {
		return namenBalanceBox;
	}

	public Label getTobias() {
		return tobias;
	}

	public Label getBernd() {
		return bernd;
	}

	public Label getGesamt() {
		return gesamt;
	}

	public HBox getTypRow() {
		return typRow;
	}

	public CheckBox getBalanceCheckBox() {
		return balanceCheckBox;
	}

	private VBox createCard(Label title, Node... content) {
        VBox card = new VBox(12);
        card.getChildren().add(title);
        card.getChildren().addAll(content);
        return card;
    }
       


    public HBox getOwnerRow() {
		return ownerRow;
	}

	public HBox getTransferRow() {
		return transferRow;
	}

	public HBox getGrossInterestRow() {
		return grossInterestRow;
	}

	public HBox getTaxBerndRow() {
		return taxBerndRow;
	}

	public HBox getInterestMonthRow() {
		return interestMonthRow;
	}

	public HBox getDescriptionRow() {
		return descriptionRow;
	}

	public HBox getAmountRow() {
		return amountRow;
	}

	public HBox getDateRow() {
		return dateRow;
	}

	public Label getLabelInterestMonth() {
		return labelInterestMonth;
	}

	public ComboBox<Month> getMonthBox() {
		return monthBox;
	}

	public Spinner<Integer> getYearSpinner() {
		return yearSpinner;
	}

	public VBox getRoot() {
        return root;
    }

    public TextField getBeschreibung() {
        return beschreibung;
    }

    public TextField getBetrag() {
        return betrag;
    }

    public TableView<UiEntry> getEntriesTable() {
        return entriesTable;
    }

    public Label getStatusLabel() {
        return statusLabel;
    }

    public Button getSubmitButton() {
        return submitButton;
    }

    public Button getDeleteButton() {
        return deleteButton;
    }

    public ComboBox<String> getTypBox() {
        return typBox;
    }

    public ComboBox<String> getOwnerBox() {
        return ownerBox;
    }

    public ComboBox<String> getTransferBox() {
        return transferBox;
    }

    public DatePicker getDatumPicker() {
        return datumPicker;
    }

    public Label getGesamtLabel() {
        return gesamtLabel;
    }

    public Label getTobiasLabel() {
        return tobiasLabel;
    }

    public Label getBerndLabel() {
        return berndLabel;
    }

	public static String getTransferTobiasBernd() {
		return TRANSFER_TOBIAS_BERND;
	}

	public static String getTransferBerndTobias() {
		return TRANSFER_BERND_TOBIAS;
	}

	public Label getLabelBeschreibung() {
		return labelBeschreibung;
	}

	public Label getLabelBetrag() {
		return labelBetrag;
	}

	public Label getLabelTyp() {
		return labelTyp;
	}

	public Label getOwnerLabel() {
		return ownerLabel;
	}

	public Label getTransferLabel() {
		return transferLabel;
	}

	public Label getLabelDatum() {
		return labelDatum;
	}

	public Label getLabelGrossInterest() {
		return labelGrossInterest;
	}

	public TextField getGrossInterestField() {
		return grossInterestField;
	}

	public Label getLabelTaxBernd() {
		return labelTaxBernd;
	}

	public TextField getTaxBerndField() {
		return taxBerndField;
	}

	public void setBeschreibung(TextField beschreibung) {
		this.beschreibung = beschreibung;
	}

	public void setBetrag(TextField betrag) {
		this.betrag = betrag;
	}

	public void setBerndLabel(Label berndLabel) {
		this.berndLabel = berndLabel;
	}

	public Button getEditButton() {
		return editButton;
	}
	public Button getCancelEditButton() {
	    return cancelEditButton;
	}
	public void setFormTitleText(String text) {
	    formTitle.setText(text);
	}
    
}