import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;
import javafx.application.Platform;
import javafx.concurrent.Task;
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
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.jcip.annotations.GuardedBy;

public class Controller implements Initializable {
  private static volatile boolean isReadThreadRunning;
  private static Vector<String> tmpList = new Vector<>();
  private static Vector<String> messages = new Vector<>();
  private static Vector<String> clientList = new Vector<>();

  private static Connection connection = Connection.getInstance();
  private static DataInputStream inputStream;
  private static DataOutputStream outputStream;

  private String nickname;
  public TextArea textArea;
  public Label labelStatus;
  public MenuItem menuConnect;
  public TextField messageInput;
  public Button sendMessageButton;

  private final Object listChatViewLock = new Object();

  @GuardedBy("listChatViewLock")
  public ListView chatMemberListview;

  private final Object listUserViewLock = new Object();

  @GuardedBy("listUserViewLock")
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
    String message = messageInput.getText();
    if (message.equals("")) {
      return;
    }

    if (message.equals("/end")) {
      isReadThreadRunning = false;
      sendMessageToServer(message);
      return;
    }
    addToChatBoxListView(message);
    sendMessageToServer(message);
    messageInput.clear();
  }

  public static boolean initialize(String ipAddress, int portNumber) {
    if (connection.connect(ipAddress, portNumber)) {
      outputStream = connection.getOutputStream();
      inputStream = connection.getInputStream();

      return true;
    } else {
      return false;
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

  public  synchronized void runReadMsgTread() {
    isReadThreadRunning = true;

    Thread readMessage =
        new Thread(
            () -> {
              while (isReadThreadRunning) {
                String msg = "";
                try {
                  // read the message sent to this client
                  msg = inputStream.readUTF();

                } catch (IOException e) {
                  System.out.println(" Wrong Command");
                  isReadThreadRunning = false;
                  System.out.println(connection);
                  e.printStackTrace();
                }

                if (msg.equals("/end")) {
                  continue;
                }
                if (msg.equals("<@#> Busy")) {
                  clearMessages();
                  msg = "Name is taken. Choose another one";
                }

                if (msg.equals("<@#> Connection closed")) {
                  break;
                }

                if (msg.equals("<@#> Connection closed")) {
                  break;
                }


                if (msg.startsWith("<@#> ")) {
                  String clientName = getClientName(msg);
                  String message = tokenize(msg)[2];
                  if (msg.startsWith("<@#> ") && msg.contains("join us")) {
                    chatMemberAddToListView(clientName);
                    msg = clientName + " join us";
                  }
                  if (msg.startsWith("<@#> ") && msg.contains("left us")) {
                    chatMemberRemoveFromListView(clientName);
                    msg = clientName + " left us";
                  }
                }

                addToMessages(msg);
                System.out.println(msg);
                System.out.println(messages);

                // Update the text of label here
                Platform.runLater(
                    () -> {
                      listView.getItems().removeAll();
                      listView.getItems().addAll(messages);
                      chatMemberListview.getItems().removeAll();
                      chatMemberListview.getItems().addAll(clientList);
                    });
              }
            });
    readMessage.start();
  }

  private synchronized void chatMemberAddToListView(String clientName) {
    clientList.add(clientName);
    //    chatMemberListview.getItems().removeAll();
    //    chatMemberListview.getItems().addAll(clientList);
  }

  private synchronized void chatMemberRemoveFromListView(String clientName) {
    clientList.remove(clientName);
    //    chatMemberListview.getItems().removeAll();
    //    chatMemberListview.getItems().addAll(clientList);
  }

  public synchronized void addToChatBoxListView(String string) {
    listView.getItems().addAll(string);
  }

  public synchronized void addToMessages(String string) {
    messages.add(string);
  }
  public synchronized void clearMessages() {
    messages.clear();
    System.out.println("clearMessages()");
    listView.getItems().removeAll();
  }

  public void sendMessageToServer(String msg) {
    try {
      // write on the output stream
      outputStream.writeUTF(msg);
      outputStream.flush();
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

  private String getClientName(String input) {
    return tokenize(input)[1];
  }

  private String[] tokenize(String received) { // /command recipient message
    String[] clientMessage = {"", "", ""};
    String clientName = "";
    StringTokenizer st = new StringTokenizer(received, " ");
    String command = st.nextToken();
    String recipient = st.nextToken();
    StringBuilder str = new StringBuilder();
    while (st.hasMoreTokens()) {
      str.append(st.nextToken()).append(" ");
    }

    clientMessage[0] = command;
    clientMessage[1] = recipient;
    clientMessage[2] = str.toString();
    System.out.println("tokens " + Arrays.toString(clientMessage));
    return clientMessage;
  }
  //---------------TEST*-----------
  private void taskThread(){
    Task task = new Task<String>() {
      @Override public String call() {
        String msg = "";
        try {
          // read the message sent to this client
          msg = inputStream.readUTF();

        } catch (IOException e) {
          System.out.println(" Wrong Command");
          e.printStackTrace();
        }
        return msg;
      }
    };

    ProgressBar bar = new ProgressBar();
    listView.getItems().add(task.getValue());
    bar.progressProperty().bind(task.progressProperty());
    new Thread(task).start();
  }
}
