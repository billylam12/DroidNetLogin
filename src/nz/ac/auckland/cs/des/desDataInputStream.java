package nz.ac.auckland.cs.des;

import java.lang.*;
import java.io.*;

public class desDataInputStream extends DataInputStream 
{
	desArrayInputStream d_in;

	public desDataInputStream(byte buffer[],  Key_schedule	schedule)
	{
		super(new desArrayInputStream(buffer, schedule));
		d_in = (desArrayInputStream)in;
	}

	public desDataInputStream(byte buf[], int offset, int length,  Key_schedule	schedule)
	{
		super(new desArrayInputStream(buf, offset, length, schedule));
		d_in = (desArrayInputStream)in;
	}
	
	public C_Block readC_Block()
	throws IOException
	{
	byte t[] = new byte[C_Block.size()];
	
		read(t, 0, C_Block.size());
 		return new C_Block(t);
 	}
}

