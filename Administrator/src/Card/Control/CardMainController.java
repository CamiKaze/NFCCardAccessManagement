package Card.Control;

import Card.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class CardMainController {
  @FXML
  private Button BtnReadPane, BtnWritePane, BtnRollPane;
  
  private MainApp mainApp;
  
  @FXML
  private void BtnReadPane() {
    BtnReadPane.setDisable(true);
    BtnWritePane.setDisable(false);
    BtnRollPane.setDisable(false);
    this.mainApp.SwitchView("View/ReadPane.fxml");
  }	
  
  @FXML
  private void BtnWritePane() {
    BtnWritePane.setDisable(true);
    BtnReadPane.setDisable(false);
    BtnRollPane.setDisable(false);
    this.mainApp.SwitchView("View/WritePane.fxml");
  }
  
  @FXML
  private void BtnRollPane() {
    BtnRollPane.setDisable(true);
    BtnReadPane.setDisable(false);
    BtnWritePane.setDisable(false);
    this.mainApp.SwitchView("View/RollPane.fxml");
  }
  
  public void setMainApp(MainApp mainApp) {
    this.mainApp = mainApp;
  }
}