package nz.ac.auckland.cs.des;

import java.lang.*;
import java.io.*;

public class desDataOutputStream extends DataOutputStream 
{
	betterArrayOutputStream d_out;

	public desDataOutputStream()
	{
		super(new betterArrayOutputStream(1024));
		d_out = (betterArrayOutputStream) out ;
	}
	
	public desDataOutputStream(int size)
	{
		super(new betterArrayOutputStream(size));
		d_out = (betterArrayOutputStream) out ;
	}
	
	public byte[] des_encrypt(Key_schedule	schedule)
	{	//returns an encrypted copy of the data stream;
	des_encrypt x;
	
		x = new des_encrypt(d_out.toByteArray()); 	//set up
		x.des_cbc_encrypt(schedule);   				//encrypt
		return x.get_input();						//return this as an array
	}
	
	public byte[] toByteArray()
	{	//returns copy of the unencrypted buffer
		return d_out.toByteArray();
	}	
	
    public void writeBytes(String s, int fieldlength) throws IOException 
    {
    	writeBytes(s);	//could throw an IOException
    	d_out.resize(fieldlength - s.length()); //pass in length to make up
    	//this will pad with nulls if the string is short. or truncate it if too long.    
    }    

	public void writeC_Block(C_Block c)
	throws IOException
	{
	byte d[] = c.getDataRef();
 		write(d, 0, d.length);
 	}
 	
 	public void reset()
 	{
 		d_out.reset();
 	}
 
 	public int length()
 	{
 		return d_out.size();
 	}
}

