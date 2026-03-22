package de.tobias.kontoapp.ui;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import Konto.TransactionManager;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import java.nio.file.Path;
import java.io.IOException;
import java.net.URL;

import Konto.Transaction;

import de.tobias.kontoapp.persistence.TransactionFileService;
    
	public class AccountApp extends Application{
	
		@Override
		public void start(Stage stage) {

		    TransactionFileService fileService = new TransactionFileService();
		    Path savePath = Path.of("transactions.csv");

		    AccountView view = new AccountView();
		    ObservableList<UiEntry> entries = FXCollections.observableArrayList();
		    TransactionManager manager = new TransactionManager();
		    TransactionBuilder transactionBuilder = new TransactionBuilder();
		    UiEntryMapper uiEntryMapper = new UiEntryMapper(
		            AccountView.TRANSFER_TOBIAS_BERND,
		            AccountView.TRANSFER_BERND_TOBIAS
		    );
		    MoneyUtil moneyUtil = new MoneyUtil();
		    InterestTransactionBuilder interestTransactionBuilder = new InterestTransactionBuilder(manager);

		    try {
		        for (Transaction tx : fileService.loadTransactions(savePath)) {
		            manager.addTransaction(tx);
		        }
		    } catch (IOException e) {
		        e.printStackTrace();
		    }

		    new AccountController(
		            view,
		            entries,
		            manager,
		            transactionBuilder,
		            interestTransactionBuilder,
		            uiEntryMapper,
		            moneyUtil,
		            fileService,
		            savePath
		    );

		    Scene scene = new Scene(view.getRoot(), 1200, 800);
		    URL cssUrl = AccountApp.class.getResource("account.css");
		    if (cssUrl == null) {
		    	throw new IllegalArgumentException("Fehler bei dem Aufruf des css Files");
		    }else {
		    	scene.getStylesheets().add(cssUrl.toExternalForm());
		    }
		    stage.setScene(scene);
		    stage.setTitle("Konto-App");
		    stage.show();
		    Platform.runLater(() -> view.getRoot().requestFocus());
		}
       
    public static void main(String[] args) {
        launch(args);
    }
}