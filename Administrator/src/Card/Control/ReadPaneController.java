package Card.Control;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class ReadPaneController {
  @FXML
  public TextArea TAReadCard;
 
  @FXML
  private Button BtnRead, BtnClear;
  
  @FXML
  private void BtnRead() {
    System.out.println("Read");
  }
  
  @FXML
  private void BtnClear() {
    System.out.println("Clear");
  }
}