package nz.ac.auckland.cs.des;

import java.lang.*;

class uHex extends Object
{

	public static String toHex(byte n)
	{ //note the "" to force the expr to a string expr.
	  //doesn't really belong here
		return "" + Character.forDigit((n >>> 4 ) & 0xF,16)
		      + Character.forDigit(n & 0xF,16);
	}

	public static String toHex(byte h[])
	{
	int i;
		String text = "";
		for(i = 0; i < h.length; i++)
			text += toHex(h[i]);
		return text;
	}


	public static String toHex(short n)
	{ //does an unsigned little endian dump of the short
	  //doesn't really belong here
	return "" + Character.forDigit((n >>> 12) & 0xf,16) +
				Character.forDigit((n >>> 8) & 0xf,16) +
				Character.forDigit((n >>> 4) & 0xf,16) +
				Character.forDigit(n & 0xf,16);
	}
	  
	public static String toHex(int n)
	{ //does an unsigned little endian dump of the int
	//doesn't really belong here
	return "" + Character.forDigit((n >>> 28) & 0xf,16) + 
				Character.forDigit((n >>> 24) & 0xf,16) +
				Character.forDigit((n >>> 20) & 0xf,16) +
				Character.forDigit((n >>> 16) & 0xf,16) +
				Character.forDigit((n >>> 12) & 0xf,16) +
				Character.forDigit((n >>> 8) & 0xf,16) +
				Character.forDigit((n >>> 4) & 0xf,16) +
				Character.forDigit(n & 0xf,16);
	}
	  
  
}