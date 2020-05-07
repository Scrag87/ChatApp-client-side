import java.net.Socket;

public class Connection {  // singleton
private static Connection INSTANCE;

private String ipAddress;
private int port;
private Socket socket;
private boolean isConnected;

  public boolean isConnected() {
    return isConnected;
  }

  public void setConnected(boolean connected) {
    isConnected = connected;
  }

  private Connection() {
  }

  public static Connection getInstance() {
    if(INSTANCE == null) {
      INSTANCE = new Connection();
    }

    return INSTANCE;
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

  public Socket getSocket() {
    return socket;
  }

  public void setSocket(Socket socket) {
    this.socket = socket;
  }

  @Override
  public String
  toString() {
    return "Connection{" +
        "ipAddress='" + ipAddress + '\'' +
        ", port=" + port +
        ", socket=" + socket +
        '}';
  }
}
