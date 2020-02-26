import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

/*
 * 
 * Updated 04/27/18 at 8:50am - Sarah Pascall
 */
public class PlatypusClient extends JFrame implements ActionListener{

   //global variables
   private JButton jbCompose = new JButton("Compose");
   private JButton jbRetrieve = new JButton("Retrieve");
   private JButton jbConnect = new JButton("Connect"); 
   private JButton jbDecrypt = new JButton("Decrypt");
   private JTextArea jtaBody = new JTextArea(20, 40);
   private JScrollPane jspMsgBody;
   public JTextArea jtaLog = new JTextArea(10,10);
   private JLabel jlSender;
   private JLabel jlDT;
   private Socket soc;
   private String userName = "";
   private JPanel jpMsgThread;
   private int startNum = 0;
   ClientMailBox cmb;
   //PanelCreator pc;
  
   // connect button multiptle input dialog parts
   private JTextField jtfUserName = new JTextField(10);
   private JTextField jtfIP = new JTextField(10);
   JPanel inputPanel = new JPanel(new GridLayout(2,2));

   public static void main (String args []){
      new PlatypusClient();
   }//end main method

   public PlatypusClient(){
      
   //top(NORTH)panel for buttons
      JPanel jpNorth = new JPanel(); 
      jpNorth.add(jbConnect);
      jpNorth.add(jbRetrieve);
      jpNorth.add(jbCompose);
      jpNorth.add(jbDecrypt);
      jbRetrieve.setEnabled(false);
      jbCompose.setEnabled(false);
      jbDecrypt.setEnabled(false);
      add(jpNorth, BorderLayout.NORTH);
      
   //Right(EAST) panel for header and message body
      JPanel jpEast = new JPanel(new BorderLayout()); //will hold the header and message body
      
      //header panel will hold name and date/time of message
      JPanel jpHeader = new JPanel(new GridLayout(2,0)); //panel to hold header information (sender name / date and time)     
      jpHeader.setBorder(BorderFactory.createLineBorder(Color.black));
      jlSender = new JLabel();
      jlSender.setFont(new Font("Ariel", Font.BOLD, 14));
      jlDT = new JLabel();
      jpHeader.add(jlSender);
      jpHeader.add(jlDT);
     
      //scroll pane for message body 
      jspMsgBody = new JScrollPane(jtaBody);
      jtaBody.setEditable(false);
      
      //add panels to frame
      jpEast.add(jpHeader, BorderLayout.NORTH);
      jpEast.add(jspMsgBody, BorderLayout.CENTER);
      add(jpEast,BorderLayout.EAST);
      jtaBody.setLineWrap(true);
      jtaBody.setWrapStyleWord(true);
   
   //Left (WEST) panel for message threads/ client log screen
      JPanel jpWest = new JPanel(new BorderLayout());
      jpMsgThread = new JPanel(new GridLayout(0,1));
      JPanel jpLog = new JPanel();
      
      JScrollPane jspMsgThread = new JScrollPane(jpMsgThread);
      jspMsgThread.setViewportView(jpMsgThread);
      
      JScrollPane jspLog = new JScrollPane(jtaLog);
      jtaLog.setEditable(false);
      jpWest.add(jspMsgThread, BorderLayout.CENTER);
      jpWest.add(jspLog, BorderLayout.SOUTH);
      add(jpWest);
      
      jbDecrypt.addActionListener(this);
      jbConnect.addActionListener(this);
      jbCompose.addActionListener(this);
      jbRetrieve.addActionListener(this);
      
      
      //Creates a new panel for the connect button JOptionPane 
      inputPanel.add(new JLabel("UserName:"));
      inputPanel.add(jtfUserName);
      inputPanel.add(new JLabel("IP address:"));
      inputPanel.add(jtfIP);  
      
      setSize(800, 600);
      setTitle("Platypus Mail");
      setLocationRelativeTo(null);
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);
      setVisible(true);
   }//end constructor
   
   public void actionPerformed(ActionEvent ae){
      switch(ae.getActionCommand()){
         case "Compose" :
            Compose cTest = new Compose(soc);
            break;
            
         case "Connect" :
            doConnect();
            break;
            
         case "Decrypt" :
            CaesarCipher decrypt = new CaesarCipher(jtaBody, "Decrypt");
            break;
            
         case "Retrieve" :
            ArrayList<String> emails = new ArrayList<String>();
            try
            { 
               PrintWriter pw = new PrintWriter(new OutputStreamWriter(soc.getOutputStream()));
               pw.println("Retrieve");
               pw.flush();
               
               BufferedReader fileReader = new BufferedReader(new InputStreamReader(soc.getInputStream()));
               String line = "";
               String msg = "";
               
               System.out.println("Getting ready to read lines from server");
               //Read entire file until end
               while(true)
               {
                  line = fileReader.readLine();
                  System.out.println("Read line: " + line);
                  //Send line by line 
                  if(line == null)
                  {
                     break;
                  }
                  else if(line.startsWith("."))
                  {
                     msg = msg.concat(line);
                  }
                  else
                  {
                     msg = msg.concat(line + "\n");
                  }
               }
               System.out.println(msg);
               
               //System.out.println("Entire Message: " + msg);
               
               //System.out.println("Closing IO stuff");
               
               //****Testing closed, Open if broken****//
               //fileReader.close();
               //pw.close();
               
               //Open user mailbox, returns array of emails
               //System.out.println("Sending user and message to ClientMailBox");
               
               cmb = new ClientMailBox(userName, msg);
               //System.out.println("Getting email array from ClientMailBox method");
               
               emails = cmb.getEmails(userName);
               //System.out.println(emails.size());
              // System.out.println("Starting at array element: " + startNum);
               
               //Based on array size 
               //Have a email count that updates each time retrieve is clicked
               //So it only pulls new emails
               //If the number doesn't increase, tell user there is no new messages. 
               ///else create new panels for new emails.
               if(startNum == emails.size())
               {
                  JOptionPane.showMessageDialog(null, "No new messages");
               }
               else
               {
                  for(int i = startNum ; i < emails.size() ; i++)
                  {
                     PanelCreator pc  = new PanelCreator(jlDT, jlSender, jtaBody, emails.get(i));
                     jpMsgThread.add(pc);
                  }
               }   
               startNum = emails.size();
            }
            catch(IOException ioe)
            {
               System.out.println("Getting exception IO");
            }   
            break;
           
      }//end switch
      
   }//end actionPerformed
   
   public void doConnect(){
   
      JOptionPane.showMessageDialog(null, inputPanel);
      jtfUserName.requestFocus(); //not working.. should put focus in Username JTF
      String connectIP = jtfIP.getText();
      userName = jtfUserName.getText();
      
      if((userName.equals("") || connectIP.equals("")))
      {
         JOptionPane.showMessageDialog(null, "Please enter all information", "Can't connect",0);
      }
      else
      {
         BufferedReader dis = null;
         PrintWriter dos = null;
         
         try{
            soc = new Socket(connectIP, 42069);
         }//end try   
         catch(UnknownHostException uhe){
            System.out.println("Unknown host");
         }//end catch
         catch(NullPointerException npe){
            System.out.println("Null pointer");
         }//end catch
         catch(BindException be){
            System.out.println("Binder exception");
         }//end catch
         catch(IOException ioe){
            System.out.println("IOException");
            jtaLog.append("Connection failed\n");
         }//end catch
         catch(Exception e){
            System.out.println("General exception");
         }//end catch
         
         
         try{
            dis = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            dos = new PrintWriter(new OutputStreamWriter(soc.getOutputStream()));
         
            // to accept message from server
            String msgFromServer = dis.readLine();
            //System.out.println(msgFromServer);
            String name = jtfUserName.getText();
            
            if(msgFromServer.equals("Login Required") || msgFromServer.equals("Login required") || msgFromServer.equals("login required")){
               jtaLog.append(msgFromServer + "\n");
               dos.println(name);
               dos.flush();
               jtaLog.append("Sent " + name + " to server\n");
            }//end if
            
            jbRetrieve.setEnabled(true);
            jbCompose.setEnabled(true);
            jbDecrypt.setEnabled(true);
            jtaLog.append("Connection Successful\n");
         }//end try
         catch(IOException ioe){
            System.out.println("ioException");
         }//end catch
         catch(NullPointerException npe){
            System.out.println("null pointer exception");
         }//end catch
        
      }//end else   
      
   }//end doConnect
  
   public class Compose extends JFrame implements ActionListener{
   
     
      JTextField jtfTo = new JTextField(10);
      JTextField jtfFrom = new JTextField(10);
      JTextArea jtaMsgBody = new JTextArea(10, 10);
      JLabel jlTo = new JLabel("To:    ",JLabel.RIGHT);
      JLabel jlFrom = new JLabel("From:", JLabel.RIGHT);
      JButton jbSend = new JButton("Send");
      JButton jbEncrypt = new JButton("Encrypt");
      JButton jbDecrypt = new JButton("Decrypt");
      Socket soc;
      Object object;
      
      String toField = "";
      String fromField = "";
      String msgBody = "";
      
      //when the compose button is pressed a new window will popup that provides TO/FROM/MESSAGE BODY areas
      public Compose(Socket soc){
      
      //North panel holds TO/FROM
         JPanel jpNorth = new JPanel(new GridLayout(2, 0));
         JPanel jpTo = new JPanel(new BorderLayout());
         JPanel jpFrom = new JPanel(new BorderLayout());
         jpTo.add(jlTo, BorderLayout.WEST);
         jpTo.add(jtfTo);
         jpFrom.add(jlFrom, BorderLayout.WEST);
         jpFrom.add(jtfFrom);
         jpNorth.add(jpTo,BorderLayout.NORTH);
         jpNorth.add(jpFrom, BorderLayout.SOUTH);
        
         jtfFrom.setText(userName + "@" + soc.getInetAddress().getHostAddress());
         
         this.add(jpNorth, BorderLayout.NORTH);
         
         jbSend.addActionListener(this);
         jbEncrypt.addActionListener(this);
         jbDecrypt.addActionListener(this);
      
      //Center panel holds message text area
         JScrollPane jspScroll = new JScrollPane(jtaMsgBody);
         this.add(jspScroll, BorderLayout.CENTER);
         jtaMsgBody.setLineWrap(true);
         jtaMsgBody.setWrapStyleWord(true);
         
      //for send button
         JPanel jpSouth = new JPanel();
         jpSouth.add(jbEncrypt);
         jpSouth.add(jbDecrypt);
         jpSouth.add(jbSend);
         this.add(jpSouth, BorderLayout.SOUTH);
         
      //Save socket
         this.soc = soc;
         
         setSize(400, 300);
         setTitle("Compose message");
         setLocationRelativeTo(null);
         setDefaultCloseOperation(DISPOSE_ON_CLOSE);
         setVisible(true);
      }//end constructor
      
      public void actionPerformed(ActionEvent ae){
         switch(ae.getActionCommand()){
                  
            case "Send" :
               try{
                  PrintWriter dos = new PrintWriter(new OutputStreamWriter(soc.getOutputStream()));
                  dos.println("Send");
                  dos.flush();
               }
               catch(IOException ioe)
               {
               
               }
               toField = jtfTo.getText();
               fromField = jtfFrom.getText();
               msgBody = jtaMsgBody.getText();
            //                
            //                String messageTest = (toField + " " + fromField + " " + msgBody);
            //                
            //                System.out.println(messageTest);
            
               if(msgBody.isEmpty())
               {
                  JOptionPane.showMessageDialog(jtaMsgBody, "Please enter message", "Error - No Message entered", 0);
                  return;
               }
               else
               {
                  SMTPClient send = new SMTPClient(PlatypusClient.this, soc, toField, fromField, msgBody);
                  this.dispose();
               }
               jbCompose.setEnabled(false);
               break;
            
            case "Encrypt" :
               jtaMsgBody.setEditable(false);
               CaesarCipher encrypt = new CaesarCipher(jtaMsgBody, "Encrypt");
               break;
               
            case "Decrypt" :
               jtaMsgBody.setEditable(true);
               CaesarCipher decrypt = new CaesarCipher(jtaMsgBody, "Decrypt");
               break;
         }//end actionPerformed
         
      }//end anon actionlistener
      
   }//end Compose class
   
}//end PlatypusClient class

/*
Authorized users:
jwn3840
scp1910
axm6392
saz9001
IF TIME PERMITS:
add icons to buttons 
*/

//Server needs to understand: login, send msg, rcv msg, retreive inbox
