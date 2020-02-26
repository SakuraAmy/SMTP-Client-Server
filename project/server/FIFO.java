import java.io.*;
import java.net.*;
import java.util.*;
import java.net.InetAddress;


/**
 *
 * @version 1.0 
 * Updated 4/30/18 at 8:06am
 * FIFO Queue saves the message to its server log,
 * and either relays the message to another server or sends it
 * to the authorized users mailbox.
 *
 */ 
public class FIFO extends Thread
{
   File file = null;
   File log = new File("log.txt");
   PrintWriter logWriter = null; //Write to log
   PrintWriter fileWriter = null; //Write to user file
   PrintWriter relayWriter = null; //Write to Server
   BufferedReader br = null; //Read message
   String email;
   String mailTo;
   String userEmail;
   String server;
   String client;
   BufferedReader dis;
   PrintWriter dos;
   String ipAddr;
   String mailFrom;
   String from ="";
   String line = "";
   String date = "";
   String subject = "";
   String message = "";
   private static final int SERVER_PORT = 42069;
   private static final String END = "\n.";
   
   Socket soc;
   java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
   
   
   // public static void main(String[] args)
//    {  
//       String test = "From: John@127.0.0.1 \nTo: scp1910@192.168.0.104\nDate: 2018/04/18 08:00:35 \nSubject: This is a message.\nBest,\nScp1910\n.";
//       String test1 = "From: scp1910@127.0.0.1 \nTo: chen1@192.168.0.1 \nDate: 2018/04/18 08:00:35 \nSubject: This is a message.\nBest,\nScp1910\n.";
// 
//       new FIFO(test,1);
//    }
   /**
    * Gets called by PlatypusServer 
    * with email and queue number as paramters
    * 
    */
   public FIFO(String msg, int num)
   {
      Date time = new Date();
      date = (dateFormat.format(time));
      int queuePos;
      email = msg;
      queuePos = num;
      
      //Pull out the ip address of message
      try
      {
         br = new BufferedReader(new StringReader(email));
         
         //for each line in message pull To: From: Date: Message
         while((line = br.readLine()) != null)
         {
            if(line.startsWith("From: "))
            {
               mailFrom = line;//takes entire from line "From: "Bob Example" <bob@example.org>" as variable
               from = userEmail;
            }
            if(line.startsWith("To: "))
            {
               //System.out.println(line);
               
               String[] whole = line.split("@",2);
               mailTo = whole[0];
               ipAddr = whole[1];
               
               whole = mailTo.split("To: ");
               mailTo = whole[1];
               
               System.out.println("User: " + mailTo + "\nIP address: " + ipAddr); 
            } 
         }
         br.close();   
      }
      catch(IOException ioe)
      {
      
      }
      /**
       * If it is same as the localhost 
       * Check the user name, open mailbox
       * and write to file.
       * Otherwise, if ip address is different
       * Open up a socket and start an SMTP process to send 
       * message to server of that IP address
       */
      try
      {
         String localIP = InetAddress.getLocalHost().getHostAddress();
         
         if(localIP.equals(ipAddr))
         {
            System.out.println("Message is for user on local host");
            file = new File(mailTo + ".txt");
            
            file.createNewFile();
            fileWriter = new PrintWriter(new FileOutputStream(file,true));
            fileWriter.println(email);
            //fileWriter.println("\n\n.");
            fileWriter.flush();
         }
         else //put relay here
         {
            
            System.out.println("Message is for user on different server, relaying");
            soc = new Socket(ipAddr, SERVER_PORT);
            
            try
            {
            //Open IO reader and writer on soc
               dos = new PrintWriter(new OutputStreamWriter(soc.getOutputStream()));
               dis = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            
            //Send login information from server to other server
               dis.readLine();
               dos.println("server");
               dos.flush();
               dos.println("send");
               dos.flush();
            
            //Wait for server to send 220 smtp.example.com SMTP Postfix
               server = dis.readLine();
               System.out.println("Server sent " + server);
            
               if(server.startsWith("Ok") || server.startsWith("NO"))
               {
                  server = dis.readLine();
                  System.out.println("Server sent " + server);
               }
            
            /**
            * if match than reply HELO relay.example.org
            * else throw an error
            */
               if(server.startsWith("220"))
               {
                  client = ("HELO " + mailFrom);
                  dos.println(client);
                  dos.flush();
                  System.out.println("Client sent " + client);
               }
               else
               {
               //Append to log area
                  System.out.println("220 greeting not recived from server.\n");
                  throw new Exception("220 greeting not recived from server.\n");
               }
            
            //wait for server to send 250 Hello relay.example.org, I am glad to meet you
               server = dis.readLine();
               System.out.println("Server sent " + server);
            
            /**
            * if match than reply MAIL FROM:<address>
            * else throw error
            */
               if(server.startsWith("250"))
               {
                  client = ("MAIL FROM:" + mailFrom);
                  dos.println(client);
                  dos.flush();
                  System.out.println("Client sent " + client);
               }
               else
               {
                  System.out.println("250 reply not received from server.\n");
                  throw new Exception("250 reply not received from server.\n");
               }
            
            //wait for server to send 250 Ok
               server = dis.readLine();
               System.out.println("Server sent " + server);
            
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
                  System.out.println("Client sent " + client);
               
               }
               else
               {
                  System.out.println("250 reply not received from server.\n");
                  throw new Exception("250 reply not received from server.\n");
               }
            
            //wait for server to send 250 Ok
               server = dis.readLine();
               System.out.println("Server sent " + server);
            
            /**
            * if match than send DATA
            * else throw error
            */
               if(server.startsWith("250"))
               {
                  client = ("DATA");
                  dos.println(client);
                  dos.flush();
                  System.out.println("Client sent " + client);
               }
               else
               {
                  System.out.println("250 reply not received from server.\n");
                  throw new Exception("250 reply not received from server.\n");
               }   
            //wait for server to send 354 End data with <CR><LF>.<CR><LF>
               server = dis.readLine();
               System.out.println("Server sent " + server);
            
            //if match than start sending email info
               if(server.startsWith("354"))
               {
               //send From: "name" <address>
               //client = ("From: "+ name + mailFrom);
                  client = ("From: "+ mailFrom);
                  dos.println(client);
                  dos.flush();
                  System.out.println("Client sent " + client);
               
               //send To: name <address>
               //client = ("To: "+ name + mailTo);
                  client = ("To: "+ mailTo);
                  dos.println(client);
                  dos.flush();
                  System.out.println("Client sent " + client);
               
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
                  System.out.println("Client sent " + date);
               
               //send Subject: subject line
                  dos.println("Subject: "+subject);
                  dos.flush();
                  System.out.println("Client sent " + subject);
               
               //send empty line
               // dos.println("\n");
               // dos.flush();
               // System.out.println("\n");
               
               //send email body
                  dos.println("\n" + message);
                  dos.flush();
                  System.out.println("Client sent " + message);
               
               //send . (signifying end of email)
               //dos.println(END);
                  dos.println(".");
                  dos.flush();
                  System.out.println("Client sent " + END);
               }
               else
               {
                  System.out.println("<CR><LF>.<CR><LF> reply not received from server.\n");
                  throw new Exception("<CR><LF>.<CR><LF> reply not received from server.\n");
               }   
            
            //wait for server to send 250 Ok: queued as (number)
               System.out.println("waiting for server to send 250");
               server = dis.readLine();
               System.out.println("Server sent " + server);
            
            /**
            * if match than send QUIT
            * else throw error
            */
               if(server.startsWith("250"))
               {
                  client = "QUIT";
                  dos.println(client);
                  dos.flush();
                  System.out.println("Client sent " + client);
               }
               else
               {
                  System.out.println("250 reply not received by server. \n");
                  throw new Exception("250 reply not received by server. \n");
               }
            
            //wait for server to send 221 Bye
               server = dis.readLine();
               System.out.println("Server sent " + server);
            
            /**
            * if match than close connection
            * else throw error
            */
               if(server.startsWith("221"))
               {
                  soc.close();
                  dos.close();
                  dis.close();
                  System.out.println("Closed connection to server");
               }
               else
               {
                  System.out.println("221 reply not received by server. \n");
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
      catch(ConnectException ce)
      {
         System.out.println("Connection timed out while trying to connect to server: " + ipAddr);
      }
      catch(UnknownHostException ue)
      {
         ue.printStackTrace();
      }
      catch(IOException ioe)
      {
         ioe.printStackTrace();
      } 
      
      /**
       * Write to log file on server
       */
      try
      {
         logWriter = new PrintWriter(new FileOutputStream(log, true));
         logWriter.println(email);
         logWriter.println("\n");
         logWriter.flush();
      }
      catch(FileNotFoundException fne)
      {
         fne.printStackTrace();
      }     
   }
}
