package de.tobias.kontoapp.ui;

import java.math.RoundingMode;
import java.time.Month;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.Node;

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

    private Button submitButton;
    private Button deleteButton;
    private Button editButton;
    private Button cancelEditButton;

    private ComboBox<String> typBox;
    private Label labelTyp;

    private ComboBox<String> ownerBox;
    private Label ownerLabel;

    private ComboBox<String> transferBox;
    private Label transferLabel;

    private Label labelDatum;
    private DatePicker datumPicker;
    
    private Label labelGrossInterest;
    private TextField grossInterestField;
    
    private Label labelTaxBernd;
    private TextField taxBerndField;
    
    private Label labelInterestMonth;
    private ComboBox <Month> monthBox;
    private Spinner<Integer> yearSpinner;
    
    

    private Label gesamtLabel;
    private Label tobiasLabel;
    private Label berndLabel;
    
    private HBox ownerRow;
    private HBox typRow;
    private HBox transferRow;
    private HBox grossInterestRow;
    private HBox taxBerndRow;
    private HBox interestMonthRow;
    private HBox descriptionRow;
    private HBox amountRow;
    private HBox dateRow;
   

    public AccountView() {
        buildUi();
    }

    private void buildUi() {
        entriesTable = new TableView<>();

        TableColumn<UiEntry, String> typeCol = new TableColumn<>("Typ");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(110);
        
        TableColumn<UiEntry, String> descriptionCol = new TableColumn<>("Beschreibung");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionCol.setPrefWidth(220);
        
        TableColumn<UiEntry, String> amountCol = new TableColumn<>("Betrag");
        amountCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString()
                )
        );
        amountCol.setPrefWidth(100);

        TableColumn<UiEntry, String> dateCol = new TableColumn<>("Datum");
        dateCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDate().toString())
        );
        dateCol.setPrefWidth(110);

        TableColumn<UiEntry, String> ownerCol = new TableColumn<>("Aufteilung");
        ownerCol.setCellValueFactory(new PropertyValueFactory<>("owner"));
        ownerCol.setPrefWidth(150);
        
        TableColumn<UiEntry, String> detailsCol = new TableColumn<>("Details");
        detailsCol.setCellValueFactory(new PropertyValueFactory<>("details"));
        detailsCol.setPrefWidth(420);
        
        entriesTable.getColumns().addAll(dateCol, typeCol, amountCol, ownerCol,detailsCol, descriptionCol);

        entriesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        entriesTable.setPrefHeight(320);
        entriesTable.setPlaceholder(new Label("Noch keine Buchungen vorhanden"));
        
        gesamtLabel = new Label();
        tobiasLabel = new Label();
        berndLabel = new Label();
        VBox balanceBox = new VBox(5, gesamtLabel, tobiasLabel, berndLabel);

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
        
        labelGrossInterest = new Label("Erhaltene Brutto Zinsen: ");
        grossInterestField = new TextField();
        grossInterestField.setPromptText("z.B. 150 €");
        
        labelTaxBernd = new Label("Steuern Bernd: ");
        taxBerndField = new TextField();
        taxBerndField.setPromptText("z.B. 50 €");
        
        labelInterestMonth = new Label("Monat an dem Zinsen angefallen sind: ");
        monthBox = new ComboBox<>();
        monthBox.getItems().addAll(Month.values());
        monthBox.setPromptText("Bitte Wählen");
        yearSpinner = new Spinner<>(1900, 2100, 2026);
        yearSpinner.setEditable(true);
        
        
        

        labelDatum = new Label("Datum: ");
        datumPicker = new DatePicker();
        datumPicker.setPromptText("Bitte wählen");

        statusLabel = new Label();
        statusLabel.setTextFill(Color.RED);

        submitButton = new Button("Abschicken");
        submitButton.setDefaultButton(true);

        deleteButton = new Button("Ausgewählter Eintrag Löschen");
        
        editButton = new Button("Ausgewählte Buchung bearbeiten");
        
        cancelEditButton = new Button("Bearbeiten abbrechen");
        cancelEditButton.setDisable(true);
        
        ownerRow = new HBox(15, ownerLabel, ownerBox);
        transferRow = new HBox(15, transferLabel, transferBox);
        grossInterestRow = new HBox(15, labelGrossInterest, grossInterestField);
        taxBerndRow = new HBox(15, labelTaxBernd, taxBerndField);
        interestMonthRow = new HBox(15, labelInterestMonth, monthBox, yearSpinner);
        descriptionRow = new HBox(15, labelBeschreibung, beschreibung);
        amountRow = new HBox(15, labelBetrag, betrag);
        dateRow = new HBox(15, labelDatum, datumPicker);
        typRow = new HBox(15,labelTyp, typBox);
        
        
        labelBeschreibung.setMinWidth(180);
        labelBetrag.setMinWidth(180);
        labelTyp.setMinWidth(180);
        ownerLabel.setMinWidth(180);
        transferLabel.setMinWidth(180);
        labelDatum.setMinWidth(180);
        labelGrossInterest.setMinWidth(180);
        labelTaxBernd.setMinWidth(180);
        labelInterestMonth.setMinWidth(180);
        
        beschreibung.setPrefWidth(260);
        betrag.setPrefWidth(160);
        grossInterestField.setPrefWidth(160);
        taxBerndField.setPrefWidth(160);
        typBox.setPrefWidth(180);
        ownerBox.setPrefWidth(180);
        transferBox.setPrefWidth(180);
        monthBox.setPrefWidth(180);
        yearSpinner.setPrefWidth(100);
        
        statusLabel.setStyle("-fx-font-weight: bold;");
        
        Label balanceTitle = new Label("Kontostände");
        balanceTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label formTitle = new Label("Buchung");
        formTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label tableTitle = new Label("Buchungen");
        tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        
        cancelEditButton.setDisable(true);
        VBox balanceCard = createCard(balanceTitle, balanceBox);

        HBox buttonRow = new HBox(10, submitButton, editButton, cancelEditButton, deleteButton);

        VBox formCard = createCard(
            formTitle,
            typRow,
            ownerRow,
            transferRow,
            grossInterestRow,
            taxBerndRow,
            interestMonthRow,
            descriptionRow,
            amountRow,
            dateRow,
            statusLabel,
            buttonRow
        );

        VBox tableCard = createCard(tableTitle, entriesTable);
       
        root = new VBox(18, balanceCard, formCard, tableCard);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f4f6f8;");
        
        balanceBox.setStyle(
        	    "-fx-background-color: white;" +
        	    "-fx-padding: 12;" +
        	    "-fx-border-color: #d9dee3;" +
        	    "-fx-border-radius: 8;" +
        	    "-fx-background-radius: 8;"
        	);
    }
    private VBox createCard(Node... children) {
        VBox box = new VBox(12, children);
        box.setPadding(new Insets(15));
        box.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #d9dee3;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;"
        );
        return box;
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
    
}