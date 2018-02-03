package Card;

import java.io.IOException;
import org.opencv.core.Core;
import Card.Control.CardMainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainApp extends Application {
  private Stage primaryStage;
  private BorderPane MainView;
  private VBox CenterPane;
    
  public void start(Stage primaryStage) {
    this.primaryStage = primaryStage;
    this.primaryStage.setTitle("Access Tools");    
    ShowMainView();
    }     
    /** Initializes the root layout and default view (WritePane) */     
    public void ShowMainView() {     
      try {
        FXMLLoader RootLayout = new FXMLLoader();
        RootLayout.setLocation(MainApp.class.getResource("View/CardMain.fxml"));
        MainView = (BorderPane) RootLayout.load(); // Load RootLayout from FXML file.
        CenterPane = (VBox) FXMLLoader.load(getClass().getResource("View/WritePane.fxml"));
        MainView.setCenter(CenterPane); // Set default view (ReadPane)
                        
        CardMainController controller = RootLayout.getController();
        controller.setMainApp(this); // Set Controller for RootLayout 
            
        Scene scene = new Scene(MainView);
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show(); // Show the scene. 
        } catch (IOException e) {
        e.printStackTrace();
      }
    }
    /** Sets the selected view. */
    public void SwitchView(String CenterView) {
      try {
        CenterPane = (VBox) FXMLLoader.load(getClass().getResource(CenterView));
        MainView.setCenter(CenterPane); // Set to selected pane	
        primaryStage.getScene().setRoot(MainView);
        } catch (IOException e) {
          e.printStackTrace();
      }
    }
    /** Returns the main stage. */
    public Stage getPrimaryStage() {
      return primaryStage;
    }

    public static void main(String[] args) {
      System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
      launch(args);
    }
}