import java.awt.*;
import javax.swing.*;

public class PanelCreator extends JPanel
{
   JLabel date = null;
   JLabel sender = null; 
   
   public static void main(String[] args)
   {
      
   }
   
   public PanelCreator(String _date, String _sender)
   {
      date = new JLabel();
      sender = new JLabel();
      date.setText(_date);
      sender.setText(_sender);
      
      this.add(date);
      this.add(sender);
   }

}