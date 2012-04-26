package nz.ac.auckland.cs.des;

import java.lang.*;
import java.io.*;

public class desArrayInputStream extends ByteArrayInputStream 
{

	//Note unlike ByteArrayInputStream. A copy of the buffer is made.
	
	public desArrayInputStream(byte buffer[], Key_schedule	schedule)
	{
		super(buffer);
		des_decrypt(schedule,  0,  buffer.length);
	}

	public desArrayInputStream(byte buffer[], int offset, int length, Key_schedule	schedule)
	{
		super(buffer);
		des_decrypt(schedule,  offset,  length);
	}
 	
	void des_decrypt(Key_schedule	schedule, int offset, int length)
	{	//returns an encrypted copy of the data stream;
	des_encrypt x;
	
		x = new des_encrypt(buf, offset, length); 	//set up
		x.des_cbc_decrypt(schedule);   					//decrypt
		buf = x.get_input();							//return this as an array
		pos = 0;
		count = buf.length;
	}
}

