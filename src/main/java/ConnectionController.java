import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ConnectionController implements Initializable {
  private int portNumber;
  public TextField ipAddress;
  public TextField username;
  public TextField port;
  Connection connection = Connection.getInstance();
  public Label connectionCheckLabel;
  public Button butConnect;


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {}

  public void actionConnect(ActionEvent actionEvent) throws IOException {
    System.out.println("Connect");
    checkUsername();
    connection.setIpAddress(ipAddress.getText());
    connection.setPort(portNumber);
    connection.setConnected(true);
// get a handle to the stage
    Stage windowConnect = (Stage) butConnect.getScene().getWindow();
    // do what you have to do
    windowConnect.close();

//    Stage chatWindow = new Stage();
//    chatWindow.setTitle("Connect Stage");
//    Parent rootChat = FXMLLoader.load(getClass().getResource("chatApp.fxml"));
//    chatWindow.setScene(new Scene(rootChat, 600, 500));
//    chatWindow.setMinHeight(400);
//    chatWindow.setMinWidth(600);
    // Specifies the modality for new window.
//    chatWindow.initModality(Modality.NONE);
    // Specifies the owner Window (parent) for new window
    // chatWindow.initOwner(primaryStage);

    // Set position of second window, related to primary window.
    // chatWindow.setX(primaryStage.getX() + 200);
    // chatWindow.setY(primaryStage.getY() + 100);

//    Controller controller = new Controller(); // !!!!// FIXME: 5/7/20
//    controller.runReadMsgTread();
//
//    chatWindow.show();
//    chatWindow.toFront();
  }

  private boolean checkUsername() {
    System.out.println("validation Username");
    // TODO checkUsername()
    return true;
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

  /*
  * try (Socket clientSocket = new Socket(ipAddress.getText(), Integer.parseInt(port.getText()));
          PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
          BufferedReader br =
              new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
        System.out.println("Connected to server");
        out.println("#@!" + username.getText());
        String response = br.readLine();
        System.out.println("Server response: " + response);
        if (response.equals("valid")) {
          // connected=true;
        }

      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  * */

  public void actionClose(ActionEvent actionEvent) {
    closeConnetion();
    System.exit(1);
  }

  private void closeConnetion() {
    // TODO
  }

  public void actionCheckConnection(ActionEvent actionEvent) {
    if (validIp(ipAddress.getText())) {
      if (validPort(port.getText())) {
        connectionCheckLabel.setText("Wait 10s to check if Host is reachable");
        try {
          if (isReacharble()) {

            try {

              Socket clientSocket = new Socket(ipAddress.getText(), portNumber);
              connection.setSocket(clientSocket);
            } catch (ConnectException e) {
              System.out.println("Unable to connect to the server");
              connectionCheckLabel.setText("Unable to connect to the server");
              return;
            }

            connectionCheckLabel.setText("Able to connect to the server");
            butConnect.setDisable(false);
            System.out.println(connection);
            Controller.initialize();
          } else {
            connectionCheckLabel.setText("Host is NOT reachable ... ");
            butConnect.setDisable(true);
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
}
