package Card.Control;

import Card.Model.*;
import java.io.ByteArrayInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.opencv.core.*;
import org.opencv.imgcodecs.*;
import org.opencv.videoio.*;
import org.opencv.core.Mat;

//import org.opencv.imgproc.Imgproc;
//import javafx.application.Platform;
//import javafx.animation.Animation;
//import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.control.TextArea;

public class WritePaneController {
	
  @FXML
  private TextField TFName, TFSurname, TFIDNum, TFLoadFunds;
  
  @FXML
  private Button BtnWrite, BtnRead, BtnClear; 
  
  @FXML
  private ComboBox<String> CmbPeriod;
  
  @FXML
  private CheckBox ChkVIP;
  
  @FXML
  private ImageView ProPic;
  private Image image = null;
  private static String IPAddr,	LoggedOn, VIP = null;
  private ScheduledExecutorService timer;
  private VideoCapture videoCapture = new VideoCapture();
  private boolean camerActive;

  
  private Image grabFrame() {
    Mat frame = new Mat();
    if(videoCapture.isOpened()) {
    try {
        videoCapture.read(frame);
        if (!frame.empty()) {
          image = mat2Image(frame);
        }
      }
      catch(Exception e) {
        System.err.println(e);
      }
    }
    return image;
  }
	
  private Image mat2Image(Mat frame) {
    MatOfByte buffer = new MatOfByte();
    Imgcodecs.imencode(".BMP", frame, buffer);
    //Imgcodecs.imwrite("/home/zeratul/java/workspace/" +TFName.getText()+".BMP", frame);
    videoCapture.read(frame);
    return new Image(new ByteArrayInputStream(buffer.toArray()));		
  }
  
  @FXML
  private void HandleActions() {
    image = new Image("file:/home/zeratul/Documents/Programming/Java/workspace/Administrator/src/Card/Deh.jpg");
    ProPic.setImage(image);
    ProPic.setSmooth(true);
  }
  
  @FXML
  private void BtnWrite() {
    try {
      IPAddr = InetAddress.getLocalHost().getHostAddress();
      LoggedOn = InetAddress.getLocalHost().getHostName();
    }
    catch (UnknownHostException e) {
    e.printStackTrace();
    }
    if (ChkVIP.isSelected()) VIP = "Yes"; else VIP = "No";	
    Alert alert = new Alert(AlertType.WARNING);
    if ((TFName.getText() == null || TFName.getText().trim().isEmpty()) || (TFSurname.getText() == null || TFSurname.getText().trim().isEmpty())) {
      alert.setTitle("Error");
      alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
      alert.setHeaderText("Fill Required Fields");
      alert.setContentText("You are receiving this error message because you did not complete all the required fields.");
      alert.showAndWait();
    }
    else {
  //    TAStatus.setText("Name" +": "+ TFName.getText() +"\nSurname: "+ TFSurname.getText() +"\nID Number: "+ TFIDNum.getText() +"\nBalance: "+ TFLoadFunds.getText() +"\nVIP: " + VIP);
      sendtoModel(TFName.getText(), TFSurname.getText(), TFIDNum.getText(), TFLoadFunds.getText(), VIP, LoggedOn, IPAddr, ProPic);
    }
  }
  
  @FXML
  private void CmbPeriod(ActionEvent event) {
    System.out.println(CmbPeriod.getValue());
  }
  
  @FXML
  private void BtnRead() {
    if (this.camerActive == false) {
    	videoCapture.open(0);
    	if (videoCapture.isOpened()) {
    	this.camerActive = true;
        Runnable frameGrabber = new Runnable() {
          @Override
          public void run() {
            image = grabFrame();
            ProPic.setImage(image);
          }
        };
        this.timer = Executors.newSingleThreadScheduledExecutor();
        this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
        this.BtnRead.setText("Stop Camera");
      }
    }
    else if (this.camerActive == true) {
      try {
        this.timer.shutdown();
        videoCapture.release();
        System.out.println("Camera has been switched off");
        this.camerActive = false;
        this.BtnRead.setText("Start Camera");
      }
      catch(java.lang.Exception ex) {
        System.err.println(ex.toString());
      }
    }
    else {
      System.err.println("Failed to open the camera");
      this.camerActive = false;
    }
  }
  
  @FXML
  private void BtnClear() {
    TFName.clear();
    TFSurname.clear();
    TFIDNum.clear();
    TFLoadFunds.clear();
    HandleActions();
    //image = new Image("NoImage.png");
    //ProPic.setImage(image);
    //ProPic.setSmooth(true);
  }
  
  public void sendtoModel(String Name, String Surname, String CellNum, String Amount, String VIP, String loggedOn, String IPAddr, ImageView proPic) {
    dalModel Rest = new dalModel();
    Rest.Post(Name, Surname, CellNum, Amount, VIP, LoggedOn, IPAddr, ProPic);
  }
}