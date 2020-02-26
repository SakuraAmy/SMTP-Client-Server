import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;

/**
 *
 * Updated 04/23/18 at 9:47am - SP
 */
public class PanelCreator extends JPanel implements MouseListener
{
   JLabel date = null;
   JLabel sender = null; 
   JTextArea jtaMsg = null;
   String time;      //Holds date of email
   String email;     //hold entire email
   String name;      //Holds the name of sender
   String msg;       //Holds the message
   
   //Pull in all info and array element from platypusclient
   public PanelCreator(JLabel date, JLabel from, JTextArea jta, String message)
   {
      this.date = date; //Header from JFrame
      this.sender = from; //Header from JFrame
      this.jtaMsg = jta; //Message body from JFrame
      this.email = message; //Message
      
      //Creating individual panel
      this.setPreferredSize(new Dimension(75,75));
      this.setBorder(BorderFactory.createLineBorder(Color.black));
      this.setLayout(new GridLayout(2, 0)); //added so sender/date are stacked instead of side by side
      sender.setFont(new Font("Ariel", Font.BOLD, 14)); 
      addMouseListener(this);  

      //Spliting message
      String[] string = email.split("\n",4);
      name = string[0];
      time = string[2];
      msg = string[3];

      string = msg.split("Subject: ");
      msg = string[1];
      
      //Setting text for panel 
      JLabel jlTime = new JLabel(time);
      JLabel jlFrom = new JLabel(name);
      this.add(jlFrom);
      this.add(jlTime);
      
   }
   
   public void mouseExited(MouseEvent me)
   {
      setBackground(Color.decode("#e5e5e5")); 
   }
   public void mousePressed(MouseEvent me)
   {
     setBackground(Color.decode("#009292"));
     this.setBorder(BorderFactory.createLoweredBevelBorder());
     this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
   }
   public void mouseReleased(MouseEvent me)
   {
      setBackground(Color.decode("#03a9a9"));
      this.setBorder(BorderFactory.createLineBorder(Color.black));
   }
   public void mouseEntered(MouseEvent me)
   {
      setBackground(Color.decode("#03a9a9"));
   }
   public void mouseClicked(MouseEvent me)
   {
      //Set info using message info passed in by PlatypusClient 
      //Pull Date, Subject, Message and set appropriate GUI fields
      date.setText(time); //Header
      sender.setText(name); //Header 
      jtaMsg.setText(msg); //Message body
   }
}
