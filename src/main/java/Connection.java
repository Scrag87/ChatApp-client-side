import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Connection extends Socket { // singleton

  private static Connection INSTANCE;

  private DataInputStream inputStream;
  private DataOutputStream outputStream;

  private String ipAddress;
  private int port;
  private Socket clientSocket;
  private boolean isConnected;

  public DataInputStream getInputStream() {
    return inputStream;
  }

  public void setInputStream(DataInputStream inputStream) {
    this.inputStream = inputStream;
  }

  public DataOutputStream getOutputStream() {
    return outputStream;
  }

  public void setOutputStream(DataOutputStream outputStream) {
    this.outputStream = outputStream;
  }

  public boolean isConnected() {
    return isConnected;
  }

  public void setConnected(boolean connected) {
    isConnected = connected;
  }

  private Connection() {}

  public static Connection getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new Connection();
    }
    return INSTANCE;
  }

  public boolean connect(String ipAddress, int portNumber) {
    try  {
      Socket clientSocket = new Socket(ipAddress, portNumber);

      INSTANCE.setInputStream(new DataInputStream(clientSocket.getInputStream()));
      INSTANCE.setOutputStream(new DataOutputStream(clientSocket.getOutputStream()));
      INSTANCE.setClientSocket(clientSocket);
      INSTANCE.setIpAddress(ipAddress);
      INSTANCE.setPort(portNumber);
      return true;
    } catch (IOException e) {

      return false;
    }
    finally{

    }
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public Socket getClientSocket() {
    return clientSocket;
  }

  public void setClientSocket(Socket clientSocket) {
    this.clientSocket = clientSocket;
  }

  @Override
  public String toString() {
    return "Connection{" +
        "inputStream=" + inputStream +
        ", outputStream=" + outputStream +
        ", ipAddress='" + ipAddress + '\'' +
        ", port=" + port +
        ", clientSocket=" + clientSocket +
        ", isConnected=" + isConnected +
        '}';
  }
}
