import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import java.util.Vector;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.jcip.annotations.GuardedBy;

public class Controller implements Initializable {


  private static Vector<String> tmpList = new Vector<>();
  private static Connection connection = Connection.getInstance();
  private static DataInputStream dis;
  private static DataOutputStream dos;
  private String nickname;
  public TextArea textArea;
  public Label labelStatus;
  public MenuItem menuConnect;
  public TextField messageInput;
  public Button sendMessageButton;

  private final Object listViewLock = new Object();
  @GuardedBy("listViewLock")
  public ListView listView;

  public static EventHandler<WindowEvent> getCloseEventHandler() {
    return closeEventHandler;
  }

  private static EventHandler<WindowEvent> closeEventHandler =
      event -> {
        System.out.println("DO SMTH ON EXIT");
        System.exit(1);
      };

  public void send1Message(ActionEvent actionEvent) {
    sendMessage();
    messageInput.requestFocus();
  }

  private void sendMessage() {
    if (messageInput.getText().equals("")) {
      return;
    }
    addToListView(messageInput.getText());
    sendMessageToServer(messageInput.getText());
    messageInput.clear();
  }

  public static void initialize() {
    try {

      //    listView.setBorder(Border.EMPTY);
      //    sendMessageButton.setBorder(Border.EMPTY);
      //    messageInput.setBorder(Border.EMPTY);

      Socket s = connection.getSocket();
      dis = new DataInputStream(s.getInputStream());
      dos = new DataOutputStream(s.getOutputStream());

    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

    sendMessageButton.setStyle(
        "-fx-focus-color: transparent;"
            + " -fx-background-insets: 0, -0, 0, 0;"
            + "-fx-faint-focus-color: transparent;");

    messageInput.setStyle("-fx-focus-color: transparent;" + "-fx-faint-focus-color: transparent;");

    listView.setStyle(
        "-fx-focus-color: transparent;"
            + "-fx-faint-focus-color: transparent;"
            + " -fx-background-insets: 0, -0, 0, 0;");
  }

  public void sendMessage(KeyEvent keyEvent) {
    if (keyEvent.getCode() == KeyCode.ENTER) {
      sendMessage();
    }
  }

  public void runReadMsgTread() {
    // readMessage thread
    Thread readMessage =
        new Thread(
            () -> {
              while (true) {
                String msg = "";
                try {
                  // read the message sent to this client
                  msg = dis.readUTF();

                } catch (IOException e) {
                  System.out.println(" Wrong Command");
                 e.printStackTrace();
                }
                addToListView(msg);
                System.out.println(msg);
              }
            });
    readMessage.start();
  }

  public synchronized void addToListView(String string) {
    listView.getItems().addAll(string);
  }

  public void sendMessageToServer(String msg, String to) {

    try {
      // write on the output stream
      dos.writeUTF(msg);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void sendMessageToServer(String msg) {
    try {
      // write on the output stream
      dos.writeUTF(msg);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void menuConnectAction(ActionEvent actionEvent) throws IOException {
    Stage connectWindow = new Stage();
    connectWindow.setTitle("Connect Stage");
    Parent rootConnect = FXMLLoader.load(getClass().getResource("ConnectWindow.fxml"));
    connectWindow.setScene(new Scene(rootConnect, 400, 300));
    connectWindow.setMinHeight(300);
    connectWindow.setMinWidth(400);
    // Specifies the modality for new window.

    connectWindow.initModality(Modality.APPLICATION_MODAL);
    // Specifies the owner Window (parent) for new window
    // chatWindow.initOwner(primaryStage);

    //     Set position of second window, related to primary window.
    //    connectWindow.setX(primaryStage.getX() + 200);
    //    connectWindow.setY(primaryStage.getY() + 100);

    connectWindow.show();
    connectWindow.toFront();
  }

  public void menuCloseAction(ActionEvent actionEvent) {

    runReadMsgTread();
  }
}
