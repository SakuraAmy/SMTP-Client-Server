import java.net.*;
import java.util.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;

/**
 *
 * @version 1.7 
 * Updated 4/30/18 at 8:06am
 *
 */
public class PlatypusServer{

   public static final int SERVER_PORT = 42069;
   private ServerSocket soc = null;
   private String server;
   private String client;
   private String userName;
   private String userEmail = "";
   private String mailFrom = "";
   private int num = 4;
   private int num2 = 2;
   private int queueNum = 0;
   private ArrayList<String> userCollection = new ArrayList<String>();
   
   public static void main(String[] args)
   {
      new PlatypusServer();
   }
   
   private PlatypusServer()
   {    
      userCollection.add("scp1910");
      userCollection.add("axm6392");
      userCollection.add("jwn3840");
      userCollection.add("saz9001");
      userCollection.add("guest");
      userCollection.add("server");
      
      try
      {
         soc = new ServerSocket(SERVER_PORT);
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
      
      ServerThread serverThread = new ServerThread(soc);
      serverThread.start();
      System.out.println("Platypus Server started! Waiting for connection..... ");
   }
   
   class ServerThread extends Thread
   {
      ServerSocket soc;
      
      private ServerThread(ServerSocket _soc)
      {
         this.soc = _soc;
      }
      
      public void run()
      {
         while(true)
         {
            try
            {
               Socket ClientSocket = soc.accept();
               ClientThread client = new ClientThread(ClientSocket);
               client.start();
            }
            catch(Exception e)
            {
               e.printStackTrace();
               break;
            }
         }
      }
   }
   
   class ClientThread extends Thread
   {
      Socket soc;
      BufferedReader dis;
      PrintWriter dos;
      private int currNum = 220;
      boolean flag = false;
      
      private ClientThread(Socket _soc)
      {
         this.soc = _soc;
      }
      
      public void run()
      {
         try
         {
            String hostName = soc.getInetAddress().getHostAddress();//Stores client hostname
            System.out.println("PlatypusClient " + hostName + " connected!");
                    
            dos = new PrintWriter(new OutputStreamWriter(soc.getOutputStream()));//Sets up our data output stream
            dis = new BufferedReader(new InputStreamReader(soc.getInputStream()));//Sets up our data input stream
            
            String line2 = "Login Required";
            dos.println(line2);
            dos.flush();
            System.out.println(line2);
            String msgFromClient = dis.readLine();
            System.out.println("Username: " + msgFromClient);           
            
            for(int i = 0; i < userCollection.size(); i ++)
            {
               String curr = userCollection.get(i);
               if(msgFromClient.equals(curr))
               {
                  flag = true;
                  System.out.println("Authorized user");
                  
                  if(curr.equals("guest"))
                  {
                     System.out.println("Guest user");
                  
                     /**
                      * Print ok to guest
                      * --Testing with team 6--
                      */
                     dos.println("Ok");
                     dos.flush();
                     System.out.println("sent Ok to guest");   
                  }
                  if(curr.equals("server"))
                  {
                     System.out.println("Server Relay connected");
                  }
                  userName = curr;
                  break;
               }
            }//end for
            if(flag == false)
            {
               flag = false;
               System.out.println("Unauthorized user");
               dos.close();
               dis.close();
               soc.close();

            }
            
            /***Maybe add while(true) block***/
            while(true)
            {
               //Read initial client command   
               msgFromClient = dis.readLine();
               System.out.println("Initial Message Recieved: " + msgFromClient);
            
            /**
             * If send process SMTP
             */
            if(msgFromClient.equals("Send") || msgFromClient.equals("send"))
            {
               while(flag == true)
               {
                  if(currNum == 220)
                  {
                     //System.out.println("Inside first step: send 220");
                     
                     String line = currNum + " " + userEmail + " ESMTP Postix"; 
                     currNum = 250;
                     dos.println(line);
                     dos.flush();
                  }
                  else if(currNum == 250)
                  {
                     String line = dis.readLine();
                     //System.out.println("Inside send step: client sent " + line + ", send 250");
                     
                     if(line.startsWith("HELO"))
                     {                   
                        String message = currNum + " " + line.substring(num) + ", I am glad to meet you!";
                        dos.println(message);
                        dos.flush();
                     }
                     else if(line.startsWith("MAIL"))
                     {
                        // int find = line.indexOf(":");
                        // String mail = line.subString(find + 1);
                        String message = currNum + " Ok";
                        dos.println(message);
                        dos.flush();
                     }
                     else if(line.startsWith("RCPT"))
                     {
                        String message = currNum + " Ok";
                        dos.println(message);
                        dos.flush();
                     }
                     else if(line.startsWith("Cc:"))
                     {
                        String message = currNum + " Ok";
                        dos.println(message);
                        dos.flush();   
                     }
                     else if(line.equals("DATA"))
                     {
                        //Ends connection
                        currNum = 354;
                        String message = currNum + " End data with <CR><LF>.<CR><LF>";
                        dos.println(message);
                        dos.flush();
                     }              
                     else if(line.equals("QUIT"))
                     {
                        //Ends connection
                        currNum = 221;
                        String message = currNum + " Bye";
                        dos.println(message + "\n{The server closes the connection}");
                        dos.flush();
                        soc.close();
                        dis.close(); 
                        flag = false; 
                     }
                  }//end else if
                  //Check for single . (period)
                  else if(currNum == 354)
                  {
                     String entireMessage = "";
                     String line = ""; 
                     while(true)
                     {  
                        line = dis.readLine();
                        entireMessage = (entireMessage + line + "\n");

                        if(line.startsWith("."))
                        {
                           //System.out.print(entireMessage);
                           queueNum ++;
                           
                           //**CALL FIFO QUEUE**//
                           //Place message, and queue number into FIFO queue
                           FIFO queue = new FIFO(entireMessage,queueNum);
                           System.out.println("breaking out of while loop");
                           break;
                        }
                     }
                     currNum = 250;
                     String message = (currNum + " OK: queued as " + queueNum);
                     System.out.println("Server sent : " + message);
                     dos.println(message);
                     dos.flush();    
                  }//end sending message
               } 
            }//end if send check 
            
            /**
             * If client sends Retrieve
             * Open file and send to client
             */
            if(msgFromClient.equals("Retrieve"))
            {
               //**THIS IS FOR LOCAL MAILBOX RETRIEVAL**/
                  //Client wants their emails
                  //Check username against exisiting files
                  //Open users file
                  
                  File userFile = new File(userName + ".txt");
                  if(userFile.exists())
                  {
                     System.out.println("Opening File: " + userFile);
                  }
                  else
                  {
                     userFile.createNewFile();
                     System.out.println("Creating File");
                  }
                  
                  PrintWriter fileWriter = new PrintWriter(new OutputStreamWriter(soc.getOutputStream()));
                  BufferedReader fileReader = new BufferedReader(new FileReader(userFile));
                 
                  String line = "";
                  System.out.println("Going to read in file");
                  
                  while(true)
                  {
                     line = fileReader.readLine();
                     System.out.println("Sending line: " + line);
                     
                     if(line == null)
                     {
                        break;
                     }
                     else
                     {
                        //Send line by line
                        fileWriter.println(line);
                        fileWriter.flush();  
                     }
                  }
                  fileWriter.flush(); 
                  //****Testing closed, Open if broken****//
                  fileWriter.close();
                  fileReader.close();    
            }//end if check for "Retrieve"  
            }//end while
         }//end try
         catch(SocketException se)
         {
            System.out.println("Connection closed");
         }
         catch(FileNotFoundException fe)
         {
            System.out.println("No such file or directory exists");
         }
         catch(Exception e)
         {
            e.printStackTrace(); 
         }//end try/catch block
      }//end run method
   }//end clientThread 
}//end PlatypusServer class
