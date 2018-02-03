package Card;

import java.util.*;
import java.math.BigInteger;
import javax.smartcardio.*;

public class CardOperator {	 
	 private TerminalFactory factory = null;
	 private CardTerminals terminals = null;
	 private List<CardTerminal> terminalList = null;
	 private CardTerminal terminal = null;	 
	 private Card card = null;
	 private CardChannel channel = null;
	 private boolean checkExclusive = false;
	 
	 CardOperator() throws CardException {
		 this.factory = TerminalFactory.getDefault();
		 this.terminals = this.factory.terminals();
	     this.terminalList = this.terminals.list();
	     this.terminal = this.terminalList.get(0);
	 }
	 
	 /********Utility Methods*********/
	 private static String byteArrayToHexString(byte[] b) {
		 StringBuffer sb = new StringBuffer(b.length * 2);
		 for (int i = 0; i < b.length; i++) {
			 int v = b[i] & 0xff;
			 if (v < 16) {
				 sb.append('0');
			 }
			 sb.append(Integer.toHexString(v));
		 }
		 return sb.toString().toUpperCase();
	 }
	 
	 private static String hexToASCII(String hex) {
		 StringBuilder ascii = new StringBuilder();
		 for (int i = 0; i < hex.length(); i+=2) {
			 String str = hex.substring(i, i+2);
			 if (str != "00")
				 ascii.append((char)Integer.parseInt(str, 16));
		 }
		 return ascii.toString();
	 }
	 
	 private static String asciiToHex(String ascii) {
		 char[] chars = ascii.toCharArray();
		 StringBuffer hex = new StringBuffer();
		 for (int i = 0; i < chars.length; i++) {
			 hex.append(Integer.toHexString((int) chars[i]));
		 }
		 return hex.toString();
	 }
	 
	 /********Card Control Methods*********/
	 public boolean ConnectCard() throws CardException {
		 boolean available = false;
		 if (this.terminal.waitForCardPresent(5000)) {
			 this.card = this.terminal.connect("*");
			 this.channel = this.card.getBasicChannel();
			 available = true;
		 }
		 return available;
	 }
	 
	 public void DisconnectCard() throws CardException {
		 this.card.disconnect(true);
		 if (this.checkExclusive) {
			 this.revokeExclusivity();
			 this.checkExclusive = false;
		 }
		 this.card = null;
		 this.channel = null;
	 }
	 
	 public boolean checkExclusivity() {
		 return this.checkExclusive;
	 }
	 
	 public void setExclusivity() throws CardException {
		 if (!checkExclusivity()) {
			 //try
			 //{
				 //this.card.beginExclusive();
				 this.checkExclusive = true;
			 //} catch (CardException cardErr) {
			//	 System.out.println(cardErr.toString());
			 //}
		 }
	 }
	 
	 public void revokeExclusivity() throws CardException {
		 if (checkExclusivity()) {
			//try
			//{
				//this.card.endExclusive();
				this.checkExclusive = false;
			//} catch (IllegalStateException cardErr) {
				//if (!cardErr.toString().contains("Card has been disconnected"))
				//	throw cardErr;
			//} 
		 }
	 }
	 
	 protected boolean AuthBlock(int blockPos) throws CardException {
	        /*---------------------------------------------
	            Authenticates the block for reading/writing
	            (otherwise no reading nor writing)
	        ---------------------------------------------*/

		 boolean reply = false;
		 byte[] ApduCommand = {
			(byte) 0xff,
            (byte) 0x86,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x05,
            (byte) 0x01,
            (byte) 0x00,
            (byte) blockPos,
            (byte) 0x60,
            (byte) 0x00,
		 };

		 CommandAPDU SetAuth = new CommandAPDU(ApduCommand);
		 ResponseAPDU AuthReply = this.channel.transmit(SetAuth);
		 if (AuthReply.getSW1() == 144 && AuthReply.getSW2() == 0)
			 reply = true;
		 return reply;
	 }
	 
	 /************Reading/Writing Methods************/
	 public String GetCardID() throws CardException {
		 String response = "";
		 byte[] apduId = {
			 (byte) 0xFF,
			 (byte) 0xCA,
			 0x00,
			 0x00,
			 0x00
		 };
		 this.channel = this.card.getBasicChannel();
		 CommandAPDU GetDataUID = new CommandAPDU(apduId);
		 ResponseAPDU CardUID = this.channel.transmit(GetDataUID);
		 byte[] uidLong = CardUID.getBytes();
		 byte[] realUid = new byte[uidLong.length - 2];
		 for (int i=0; i<realUid.length; i++) {
			 realUid[i] = uidLong[i];
		 }
		 response = byteArrayToHexString(realUid);
		 return response;
	 }
	 
	 public String[] ReadData(boolean waitChange) throws CardException {
	        /*-------------------------------------------------------------
	            Reads entire card, formats response and returns String Array

	            Returns as firstname, surname, username and "other"
	        -------------------------------------------------------------*/
	        StringBuilder prepString = new StringBuilder();
	        String[] reply = new String[4];
	        byte blockPos = 1;
            boolean auth = false;
            
            for (int i=0; i<13; i++) {
                if (blockPos == 1 || blockPos%4 == 0)
                    //We only need authentication for the sector to manipulate all its blocks
                    auth = AuthBlock(blockPos);
                else if ((blockPos+1)%4 == 0)
                    //Remove Authentication for 4n-1 block. It contains the security protocols for NXP cards
                    auth = false;

                if (auth) {
                   byte[] ApduCommand = {
                        (byte) 0xff,
                        (byte) 0xb0,
                        (byte) 0x00,
                        (byte) blockPos,
                        (byte) 0x10
                    };
                    CommandAPDU ReadBlock = new CommandAPDU(ApduCommand);
                    ResponseAPDU ReadReply = this.channel.transmit(ReadBlock);
                    if (ReadReply.getSW1() == 144 && ReadReply.getSW2() == 0) {//Returns 0x9000 if successful, 0x63XX otherwise
                        byte[] HexReply = ReadReply.getBytes();
                        prepString.append(byteArrayToHexString(HexReply).substring(0,32));
                    }
                }
                blockPos++;
            }
            
            String cardData = hexToASCII(prepString.toString());
            int step = 0;
            while (cardData != "" && step<4) {
                //Works, but control string manipulation better
                int delimiterPos = cardData.indexOf("%%");
                if (delimiterPos>-1) {
                    String attrib = cardData.substring(0, delimiterPos);
                    cardData = cardData.substring(delimiterPos + 2);
                    reply[step] = attrib;
                }
                step++;
            }
	        return reply;
	    }
	    
	    public String[] ReadData() throws CardException {
	    	return ReadData(false);
	    }
	    
	    public static boolean ClearCard() throws Exception {
	        return true;
	    }
	    
	    public boolean WriteData(String[] data) throws CardException {
	        //Writes to Card
	        /*---------------------------------------------------------------------------
	            TODO: either better way to go about this, or with current implementation,
	            1. Read Card
	            2. Replace attribute in response string
	            3. Write modified response string

	            Wondering... return boolean array indicating error sectors?
	        ---------------------------------------------------------------------------*/
	        boolean reply = false;
	        StringBuilder cardData = new StringBuilder();
	        
	        for (int i=0; i<data.length; i++) {
	            cardData.append(data[i] + "%%");
	        }
	        
	        System.out.println("writing: " + cardData.toString());
	        String hex = asciiToHex(cardData.toString());
	        //compress and encode? LZW's maybe out, ECC to be implemented
	        if (this.terminal.waitForCardPresent(10000)) {
	            int blockPos = 0x01;
	            boolean auth = false;
	            while (!hex.equals("") && blockPos < 9) {//maybe !hex.equals("")?
	            //with each loop, we're slicing off 32bytes from hex, writing that slice to a block, until hex is empty	            
	                String slice = "";
	                if (hex.length()>32) {
	                    slice = hex.substring(0, 32);
	                    hex = hex.substring(32);
	                }
	                else {
	                    slice = hex;
	                    hex = "";
	                }
	                
	                byte[] arr = new BigInteger(slice, 16).toByteArray();
	                if (blockPos == 1 || blockPos%4 == 0) {
	                    auth = AuthBlock(blockPos);			//attempt authentication of sector of number blockPos
	                    while (!auth && blockPos<13) {
	                        //Loop in the event card sectors become unreadable
	                        if (blockPos == 1)
	                            blockPos = 4;
	                        else
	                            blockPos += 4;
	                        auth = AuthBlock(blockPos);
	                    }
	                }
	                else if ((blockPos+1)%4 == 0)
	                    auth = false;

	                if (auth) { //Latter half of condition is redundant, but I'd rather be safe :x
	                    byte[] WriteCommand = {
	                        (byte) 0xff,
	                        (byte) 0xd6,
	                        (byte) 0x00,
	                        (byte) blockPos,
	                        (byte) 0x10
	                    };
	                    byte[] ApduCommand = new byte[21];
	                    
	                    for (int k = 0; k < ApduCommand.length; k++) {
	                    	//construct command with...
	                        if (k < WriteCommand.length)
	                        //..command to write to whichever block...
	                            ApduCommand[k] = WriteCommand[k];
	                        else if (k - WriteCommand.length < arr.length)
	                        //..actual data to write to block...
	                            ApduCommand[k] = arr[k - WriteCommand.length];
	                        else
	                        //..complete block with null string
	                            ApduCommand[k] = (byte) 0x00;
	                    }
	                    
	                    CommandAPDU WriteBlock = new CommandAPDU(ApduCommand);
	                    ResponseAPDU WriteReply = this.channel.transmit(WriteBlock);
	                    
	                    if (WriteReply.getSW1() == 144 && WriteReply.getSW2() == 0) {
	                        reply = true; //Somehow return error blocks if they arise?
	                    }
	                    //else
	                    //throw new CardException("Error writing to block " + blockPos);
	                    //currently writing in spite of exceptions, meaning...
	                    //faulty cards can possibly be written to. do we want to stop this?
	                }
	                blockPos++;
	            }
	        }
	        return reply;
	    }
}