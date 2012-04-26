package nz.ac.auckland.cs.des;

import java.lang.*;
import java.io.*;

public class betterArrayOutputStream extends ByteArrayOutputStream
{
	   
	    public betterArrayOutputStream() 
		{
			super();
		}	    
		
		
		public betterArrayOutputStream(int size) 
		{
			super(size);
		}
		
		public void resize(int nBytes)
		{
			if(nBytes < 0)
			{
				count += nBytes; //reduce the count by nBytes
				if(count < 0)
					count = 0;	 //Sanity check
			}
			else if(nBytes > 0)
				while(nBytes-- > 0)
					 write(0) ;  //null pad to fill in space
			// else if(nBytes == 0) then we don't need to do anything.
		}
}

				