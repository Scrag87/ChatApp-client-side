package TermClient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client
{
  final static int ServerPort = 8087;
 static volatile boolean isRunning = true;
  public static void main(String args[]) throws UnknownHostException, IOException
  {
    Scanner scn = new Scanner(System.in);

    // getting localhost ip
    InetAddress ip = InetAddress.getByName("localhost");

    // establish the connection
    Socket s = new Socket(ip, ServerPort);

    // obtaining input and out streams
    DataInputStream dis = new DataInputStream(s.getInputStream());
    DataOutputStream dos = new DataOutputStream(s.getOutputStream());

    // sendMessage thread
    Thread sendMessage = new Thread(new Runnable()
    {
      @Override
      public void run() {
        while (true) {

          // read the message to dever.
          String msg = scn.nextLine();
          if(msg.equals("/end")){
            isRunning =false;
          }
          try {
            // write on the output stream
            dos.writeUTF(msg);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    });


    // readMessage thread
    Thread readMessage =
        new Thread(
            () -> {

              while (isRunning) {
                try {
                  // read the message sent to this client
                  String msg = dis.readUTF();
                  if(msg.equals("<@#> Connection closed")){

                  }
                  System.out.println(msg);
                } catch (IOException e) {
                  System.out.println(" Wrong Command");
                  //e.printStackTrace();
                }
              }
            });


    sendMessage.start();
    readMessage.start();

  }
}