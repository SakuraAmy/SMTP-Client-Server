import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;


public class CaesarCipher
{
   static final int SHIFT = 3;
   static final String START = "--Begin encrypted message--";
   static final String END = "--End encrypted message--";
   private byte[] cipher = null;      //Holds the message from client
   private JTextArea msgBody;
   private String message;
   private String choice;
   
   public CaesarCipher(JTextArea jta, String _choice)
   {
      //Pulls message from Text Area
      msgBody = jta;
      message = msgBody.getText();
      this.choice = _choice;
      int position;
      String encrypt;
      String decrypt;
      
      if(message.isEmpty())
      {
         JOptionPane.showMessageDialog(msgBody, "Please enter text", "Error - No Text Entered",0);
         return;
      }
      
      //if Encrypt is hit 
      if(choice.equals("Encrypt") == true)
      {
         /**
          * Check that it is unwrapped
          * if it is shift letters by shift value
          * else return an error message to user
          */
         if(!(message.startsWith(START)))
         {
            /**
             * Convert message to lower case and store in array. 
             * For each character (a-z) in the array, 
             * change its byte value using shift amount 
             */
            cipher = message.toLowerCase().getBytes();
            for (int i = 0; i < cipher.length; i++)
            {
              position = cipher[i];
              if ((position >= 97) && (position <= 122)) 
              {
                 //If character is in the range of a-z, shift position.
                 cipher[i] = ((byte)((position - 97 + SHIFT) % 26 + 97));
              }
            }
            //and wrap mesage with START ENCRYPT/END ENCRYPT
            encrypt = new String(cipher);
            msgBody.setText(START);
            msgBody.append(encrypt);
            msgBody.append(END);
         }
         else
         {
            JOptionPane.showMessageDialog(msgBody, "Please decrypt to change message", "Error - Message already encrypted",0);    
         }
      }
      
      if(choice.equals("Decrypt") == true)
      {
         /**
          * Check that it is wrapped
          * if it is shift letters by shift value
          * else return an error message to user
          */
         if(message.startsWith(START))
         {
            //Unwrap message/remove START ENCRYPT/END ENCRYPT 
            String[] one = message.split(START);
            message = one[1];
            one = message.split(END);
            message = one[0];
            
            /**
             * Convert message to lower case and store in array. 
             * For each character (a-z) in the array, 
             * change its byte value using shift amount 
             */
            cipher = message.toLowerCase().getBytes();
            for (int i = 0; i < cipher.length; i++)
            {
               position = cipher[i];
               
               //If character is in the range of a-z, shift position.
               if((position >= 97) && (position <= 122)) 
               {
                  cipher[i] = ((byte)((position - 97 - SHIFT + 26) % 26 + 97));
               }
            } 
            
            //Return the decrypted string 
            decrypt = new String(cipher);
            msgBody.setText(decrypt);
         }
         else
         {
            JOptionPane.showMessageDialog(msgBody, "Message is decrypted already", "Error - Message not encrypted",0);
         }
      }
      
      //return message back to client GUI text Area
   }
}