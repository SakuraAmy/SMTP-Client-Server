import java.io.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;

/**
 *
 * Updated 04/23/18 - SP
 */
public class ClientMailBox
{
   File file;
   PrintWriter pw;
   BufferedReader br;
   String user;
   String message;
   
   //Constructor called by the server to add mail to client mailbox
   //When called, parameters are authorized user name and message
   public ClientMailBox(String name, String msg)
   {
      user = name;
      message = msg;
      System.out.println("Mailbox called for " + user + " Message being saved is: " + message);
      file = new File(user + ".txt");
      try
      {
         /**
          * Check if file exists for authorized user 
          * Create file if it doesn't exist
          * append message to file 
          */
         if(file.exists() == true)
         {
            pw = new PrintWriter(new FileOutputStream(file));
            pw.println(msg);
            pw.flush();
         }
         else
         {
            file.createNewFile();
            pw = new PrintWriter(new FileOutputStream(file));
            pw.println(msg);
            pw.flush();
         }
         pw.close();
      }
      catch(FileNotFoundException fne)
      {
      
      }
      catch(IOException ioe)
      {
      
      }  
   }//end class
   
   //----Maybe-----//
   //Create a getter that returns the correct message info
   //For panel creator class to use
   public ArrayList <String> getEmails(String name)
   {
      String n = (name + ".txt");
      File file = new File(n);
      //Read in file contents for user
      //Read users file and split into individual emails at "."
      //Write into array
      ArrayList<String> emails = new ArrayList<String>();
      try
      {
         br = new BufferedReader(new FileReader(file));
         String email = "";
         String line = "";
         
         //for each line in message pull To: From: Date: Message
         while((line = br.readLine()) != null)
         {
            if(line.startsWith("From: "))
            {
               line = (line + "\n\n");
               email = email + line;
            }
            else if(line.startsWith("Date: "))
            {
               line = (line + "\n\n");
               email = email + line;  
            }
            else if(line.startsWith("Subject: "))
            {
               //Read line after subject (start of message)
               //line = br.readLine(); 
               while(!(line.equals(".")))
               {
                  email = email + line;
                  line = br.readLine();  
               }
               emails.add(email);
               email = ""; 
            }    
            //System.out.println(email);
         }
         br.close();
      }
      catch(IOException ioe)
      {
      
      }
      return emails;   
   }
}//end class
