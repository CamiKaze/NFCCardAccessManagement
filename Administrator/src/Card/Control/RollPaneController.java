package Card.Control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class RollPaneController {
  @FXML
  private TextField TFSearch;
  
  @FXML
  private ListView<String> LVSearch = new ListView<String>();
  
  @FXML
  private Button BtnClear, BtnFind;
  
  @FXML
  private ComboBox<String> CmbReport;
  
  @FXML
  private void CmbReport(ActionEvent event) {
    System.out.println(CmbReport.getValue());
  }
  
  @FXML
  private void BtnFind() {
    System.out.println("ToDo");
  }
  
  @FXML
  private void BtnClear() {
    System.out.println("Cleared");
  }
}