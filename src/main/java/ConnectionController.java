import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ConnectionController implements Initializable {

  private int portNumber;

  private volatile boolean isReadThreadConnectionRun;
  private static Connection connection = Connection.getInstance();
  private static DataInputStream inputStream;
  private static DataOutputStream outputStream;

  @FXML
  private Button butLogin;
  @FXML
  private Button butRegisration;
  @FXML
  private Button butCheckConnection;
  @FXML
  private TextField passwordTextField;
  @FXML
  private TextField loginTextField;
  @FXML
  private TextField ipAddress;
  @FXML
  private TextField port;
  @FXML
  private Label connectionCheckLabel;
  @FXML
  private Button butConnect;
  private volatile boolean isLoggedIn = false;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

    // TODO: 5/13/20  passwordTextField show *** instead of letters
  }

  public void actionConnect(ActionEvent actionEvent) throws IOException {
    System.out.println("Connect");
    System.out.println("isLoggedIn " + isLoggedIn);

    isReadThreadConnectionRun = false;
    Global.getParentController().runReadMsgTread();
    Connection.getInstance().setConnected(true);
    Global.getParentController().setLabelStatusText("Connected");
    Global.getParentController().clearMessages();

    // get a handle to the stage
    Stage windowConnect = (Stage) butConnect.getScene().getWindow();
    // do what you have to do
    windowConnect.close();

  }

  private boolean validPort(String text) {
    System.out.println("Port validation");
    int port = 0;
    try {
      port = Integer.parseInt(text);
    } catch (NumberFormatException e) {
      return false;
    }
    if ((port >= 0) && (port < 65535)) {
      portNumber = port;
      return true;
    }
    return false;
  }

  private boolean validIp(String address) {
    System.out.println("validation IP");
    final String zeroTo255 = "([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])";
    final String IP_REGEXP = zeroTo255 + "\\." + zeroTo255 + "\\." + zeroTo255 + "\\." + zeroTo255;
    final Pattern IP_PATTERN = Pattern.compile(IP_REGEXP);
    return IP_PATTERN.matcher(address).matches();
  }

  public void actionClose(ActionEvent actionEvent) {
    Stage windowConnect = (Stage) butConnect.getScene().getWindow();
    windowConnect.close();
  }

  public void actionCheckConnection(ActionEvent actionEvent) {
    if (validIp(ipAddress.getText())) {
      if (validPort(port.getText())) {
        connectionCheckLabel.setText("We are checking if Host is reachable ...");
        try {
          if (isReacharble()) {
            if (Controller.initialize(ipAddress.getText(), portNumber)) {
              connectionCheckLabel.setText("Able to connect to the server");
              butLogin.setDisable(false);
              butRegisration.setDisable(false);
              runReadMsgTread();

            } else {
              System.out.println("Unable to connect to the server");
              connectionCheckLabel.setText("Unable to connect to the server");
              return;
            }
          } else {
            System.out.println("Unable to connect to the server");
            connectionCheckLabel.setText("Unable to connect to the server");
            return;
          }

        } catch (IOException e) {
          e.printStackTrace();
        }
        return;
      }
    }
    System.out.println("Recheck server IP:port ");
    connectionCheckLabel.setText("Recheck server IP:port ");
    butConnect.setDisable(true);
  }

  private boolean isReacharble() throws IOException {
    InetAddress address = InetAddress.getByName(ipAddress.getText());
    boolean reachable = address.isReachable(10000);
    System.out.println("Is host reachable? " + reachable);
    return reachable;
  }

  public void actionLogin(ActionEvent actionEvent) {
    Global.getParentController()
        .sendMessageToServer(
            "<@#>/uc " + loginTextField.getText() + " " + passwordTextField.getText());
  }

  public synchronized void runReadMsgTread() {
    outputStream = connection.getOutputStream();
    inputStream = connection.getInputStream();
    isReadThreadConnectionRun = true;
    Thread readMessage =
        new Thread(
            () -> {
              while (isReadThreadConnectionRun) {
                System.out.println("runReadMsgTread()-Connection " + isReadThreadConnectionRun);
                String msg = "";
                try {
                  // read the message sent to this client
                  msg = connection.getInputStream().readUTF();
                  System.out.println(msg);

                } catch (IOException e) {
                  System.out.println(" Wrong Command");
                  isReadThreadConnectionRun = false;
                  e.printStackTrace();
                }

                if (msg.equals("/end")) {
                  continue;
                }

                if (!msg.startsWith("<@#>")) {
                  continue;
                }

                if (msg.startsWith("<@#> ") && msg.contains(" Busy")) {
                  System.out.println(" Busy");
                  Platform.runLater(
                      () -> {
                        connectionCheckLabel.setText(
                            "Name "
                                + loginTextField.getText()
                                + " is taken. Choose"
                                + " another one");
                        loginTextField.clear();
                        butConnect.setDisable(true);
                      });
                  continue;
                }
                if (msg.startsWith("<@#> ") && msg.contains("not registered")) {
                  System.out.println("User does not exist.");
                  isLoggedIn = true;
                  Platform.runLater(
                      () -> {
                        connectionCheckLabel.setText(
                            "Name"
                                + loginTextField.getText()
                                + " not registered"
                                + " .Fire up Reg button!");
                        loginTextField.clear();
                        butConnect.setDisable(true);
                      });
                  Platform.runLater(() -> System.out.println("not registered!"));
                }

                if (msg.startsWith("<@#> ") && msg.contains("password wrong!")) {
                  System.out.println("User password wrong!");
                  isLoggedIn = true;
                  Platform.runLater(
                      () -> {
                        connectionCheckLabel.setText(
                            "Name "
                                + loginTextField.getText()
                                + " password is wrong!");
                        passwordTextField.clear();
                        butConnect.setDisable(true);
                      });
                  Platform.runLater(() -> System.out.println("password wrong!"));
                }

                if (msg.startsWith("<@#> ") && msg.contains("successfully registered!")) {
                  System.out.println("successfully registered!");
                  isLoggedIn = true;
                  Platform.runLater(
                      () -> {
                        butConnect.setDisable(false);
                      });

                  Platform.runLater(() -> System.out.println("successfully registered!"));
                }
                if (msg.startsWith("<@#> ") && msg.contains("successfully auth!")) {
                  System.out.println("successfully auth!");
                  isReadThreadConnectionRun = false;
                  isLoggedIn = true;
                  Platform.runLater(
                      () -> {
                        butConnect.setDisable(false);
                      });

                  Platform.runLater(() -> System.out.println("successfully registered!"));
                }

                if (msg.equals("<@#> Connection closed")) {
                  Platform.runLater(
                      () -> {
                        Connection.getInstance().setConnected(false);
                      });
                  break;
                }
              }
            });
    readMessage.start();
  }

  public void actionRegistration(ActionEvent actionEvent) {
    Global.getParentController()
        .sendMessageToServer(
            "<@#>/reg " + loginTextField.getText() + " " + passwordTextField.getText());
  }
}
