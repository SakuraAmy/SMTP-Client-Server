import java.io.*;
import java.net.*;
import java.util.*;
//import java.util.Date;


//SMTP server side
//Must pass in socket, JTextFields for email 
/**
 *
 *
 * @version 1.4
 * Updated 05/03/18 at 12:49pm - SP
 *
 */
public class SMTPClient
{ 
   Socket socket;
   Object object;
   BufferedReader dis;
   PrintWriter dos;
   private String server;
   private String client;
   private String userEmail = "";
   private String mailFrom = "";
   private String mailTo = "";
   private String message = "";
   private String name = "";
   private String subject = "";
   private String date = "";
   private static final String END = "\n.";
   private int size = 0; //will hold the number of message receipients
   
   //Maybe have string array for multple emails
   public SMTPClient(PlatypusClient pc, Socket soc, String to, String from, String msg)
   {
      String[] string = to.split("@");
      String[] string2 = from.split("@");
      object = pc;
      socket = soc;
      mailTo = to;
      userEmail = from;
      name = string2[0];
      mailFrom = from;
      message = msg;
      
      //Clock setup
      java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
      Date time = new Date();
      date = (dateFormat.format(time));
      
      //Add JFrame log appended to each step
      try
      {
         //Open IO reader and writer on socket
         dos = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
         dis = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         
         //Wait for server to send 220 smtp.example.com SMTP Postfix
         server = dis.readLine();
         pc.jtaLog.append("Server sent " + server);
         pc.jtaLog.append("\n");
         
         if(server.startsWith("Ok") || server.startsWith("NO"))
         {
            server = dis.readLine();
            pc.jtaLog.append("Server sent " + server);
            pc.jtaLog.append("\n");
         }
         
         /**
          * if match than reply HELO relay.example.org
          * else throw an error
          */
         else if(server.startsWith("220"))
         {
            client = ("HELO " + mailFrom);
            dos.println(client);
            dos.flush();
            pc.jtaLog.append("Client sent " + client);
            pc.jtaLog.append("\n");
         }
         else
         {
            //Append to log area
            pc.jtaLog.append("220 greeting not recived from server.\n");
            throw new Exception("220 greeting not recived from server.\n");
         }
            
         //wait for server to send 250 Hello relay.example.org, I am glad to meet you
         server = dis.readLine();
         pc.jtaLog.append("Server sent " + server);
         pc.jtaLog.append("\n");
         
         /**
          * if match than reply MAIL FROM:<address>
          * else throw error
          */
         if(server.startsWith("250"))
         {
            client = ("MAIL FROM:" + mailFrom);
            dos.println(client);
            dos.flush();
            pc.jtaLog.append("Client sent " + client);
            pc.jtaLog.append("\n");
         }
         else
         {
            pc.jtaLog.append("250 reply not recieved from server.\n");
            throw new Exception("250 reply not recieved from server.\n");
         }
         
         //wait for server to send 250 Ok
         server = dis.readLine();
         pc.jtaLog.append("Server sent " + server);
         pc.jtaLog.append("\n");
         
         /**
          * if match than send RCPT TO:<address> (repeat for each recipient)
          * else throw error
          */
          if(server.startsWith("250"))
          {
            //If there are more than 1 recipant start loop
            
            //else send over single recipent 
            client = ("RCPT TO: " + mailTo);
            dos.println(client);
            dos.flush();  
            pc.jtaLog.append("Client sent " + client);
            pc.jtaLog.append("\n");

          }
          else
          {
            pc.jtaLog.append("250 reply not recieved from server.\n");
            throw new Exception("250 reply not recieved from server.\n");
          }
           
         //wait for server to send 250 Ok
         server = dis.readLine();
         pc.jtaLog.append("Server sent " + server);
         pc.jtaLog.append("\n");
         
         /**
          * if match than send DATA
          * else throw error
          */
         if(server.startsWith("250"))
         {
            client = ("DATA");
            dos.println(client);
            dos.flush();
            pc.jtaLog.append("Client sent " + client);
            pc.jtaLog.append("\n");
         }
         else
         {
            pc.jtaLog.append("250 reply not recieved from server.\n");
            throw new Exception("250 reply not recieved from server.\n");
         }   
         //wait for server to send 354 End data with <CR><LF>.<CR><LF>
         server = dis.readLine();
         pc.jtaLog.append("Server sent " + server);
         pc.jtaLog.append("\n");
         
         //if match than start sending email info
         if(server.startsWith("354"))
         {
            //send From: "name" <address>
            //client = ("From: "+ name + mailFrom);
            client = ("From: "+ mailFrom);
            dos.println(client);
            dos.flush();
            pc.jtaLog.append("Client sent " + client);
            pc.jtaLog.append("\n");
            
            //send To: name <address>
            //client = ("To: "+ name + mailTo);
            client = ("To: "+ mailTo);
            dos.println(client);
            dos.flush();
            pc.jtaLog.append("Client sent " + client);
            pc.jtaLog.append("\n");
            
            //send Cc: address (repeat for every additional recipient)
          //   for(int i = 0; i < size; i++)
//             {
//                dos.println("Cc: "+ mailTo);
//                dos.flush();
//                System.out.println("client sent " + client);
//             }
            
            //send Date: full date
            dos.println("Date: " + date);
            dos.flush();
            pc.jtaLog.append("Client sent " + date);
            pc.jtaLog.append("\n");
            
            //send Subject: subject line
            dos.println("Subject: "+subject);
            dos.flush();
            pc.jtaLog.append("Client sent " + subject);
            pc.jtaLog.append("\n");
            
            //send empty line
            // dos.println("\n");
//             dos.flush();
//             pc.jtaLog.append("\n");
            
            //send email body
            dos.println("\n" + message);
            dos.flush();
            pc.jtaLog.append("Client sent " + message);
            pc.jtaLog.append("\n");
            
            //send . (signifying end of email)
            //dos.println(END);
            dos.println(".");
            dos.flush();
            pc.jtaLog.append("Client sent " + END);
            pc.jtaLog.append("\n");
         }
         else
         {
            pc.jtaLog.append("<CR><LF>.<CR><LF> reply not received from server.\n");
            throw new Exception("<CR><LF>.<CR><LF> reply not received from server.\n");
         }   
         
         //wait for server to send 250 Ok: queued as (number)
         System.out.println("waiting for server to send 250");
         server = dis.readLine();
         pc.jtaLog.append("Server sent " + server);
         pc.jtaLog.append("\n");
         
         /**
          * if match than send QUIT
          * else throw error
          */
         if(server.startsWith("250"))
         {
            client = "QUIT";
            dos.println(client);
            dos.flush();
            pc.jtaLog.append("Client sent " + client);
            pc.jtaLog.append("\n");
         }
         else
         {
            pc.jtaLog.append("250 reply not received by server. \n");
            throw new Exception("250 reply not received by server. \n");
         }
         
         //wait for server to send 221 Bye
         server = dis.readLine();
         pc.jtaLog.append("Server sent " + server);
         pc.jtaLog.append("\n");
         
         /**
          * if match than close connection
          * else throw error
          */
         if(server.startsWith("221"))
         {
            socket.close();
            dos.close();
            dis.close();
            System.out.println("Closed connection to server");
         }
         else
         {
            pc.jtaLog.append("221 reply not received by server. \n");
            throw new Exception("221 reply not received by server. \n");
         }
      }
      catch(BindException be)
      {
         be.printStackTrace();
      }
      catch(IOException ioe)
      {
         ioe.printStackTrace();
      }
      catch(Exception e)
      {
         e.printStackTrace();
      } 
         
   }

}
