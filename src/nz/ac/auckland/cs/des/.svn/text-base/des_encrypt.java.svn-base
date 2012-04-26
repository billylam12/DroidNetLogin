package nz.ac.auckland.cs.des;

import java.lang.*;

public class des_encrypt extends Object
{
	private C_Block			input[];	//Buffer to encrypt or decrypt from
	private C_Block		 	ivec;		//this is could be null
	
	public des_encrypt(String s) 
	{
	int length = (s.length() + 7) / 8;
	byte input_buffer[] = new byte[s.length()];
	
		s.getBytes(0, s.length() - 1, input_buffer, 0);
		init(input_buffer, 0, input_buffer.length, new C_Block());
	}
	
	public des_encrypt(String s, C_Block new_ivec)
	{
	int length = (s.length() + 7) / 8;
	byte input_buffer[] = new byte[s.length()];
	
		s.getBytes(0, s.length() - 1, input_buffer, 0);
		init(input_buffer, 0, input_buffer.length, new_ivec);
	}
	
	public des_encrypt(byte input_buffer[])
	{
		init(input_buffer, 0, input_buffer.length, new C_Block());
	}
	
	public des_encrypt(byte input_buffer[],  C_Block last_ivec)
	{
		init(input_buffer, 0, input_buffer.length, last_ivec);
	}
	
	public des_encrypt(byte input_buffer[], int offset, int length)
	{
		init(input_buffer, offset, length, new C_Block());
	}
	
	public des_encrypt(byte input_buffer[], int offset, int length,  C_Block last_ivec)
	{
		init(input_buffer, offset, length,  last_ivec);
	}
	

	public void init(byte input_buffer[],  int offset, int length, C_Block last_ivec)
	{
	int num_C_Blocks = (((length > input_buffer.length) ? input_buffer.length : length) + 7) / 8;
	int l;
	
		input = new C_Block[num_C_Blocks];
	    for (l = 0; l < num_C_Blocks; l++) 
			input[l] = new C_Block(input_buffer, l * 8 + offset);
		ivec = (C_Block)last_ivec.clone();
	}

	public void set_ivec()
	{
		ivec = new C_Block();
	}

	public void set_ivec(C_Block c)
	{
		ivec = (C_Block) c.clone();
	}

	public C_Block get_ivec()
	{
		return ivec;
	}

	public byte[] get_input()
	{
	byte out[] = new byte[input.length * 8];
		
		get_input(out);
		return out;
	}

	public void get_input(byte here[])
	throws ArrayIndexOutOfBoundsException
	{
	int i, j, k;
	
		if(here.length < input.length * 8)
			throw new ArrayIndexOutOfBoundsException("Not enough space for copy");
		for(k = 0, i = 0; i < input.length; i++)
			for(j = 0; j < 8; j++)
				here[k++] = input[i].data[j];				
	}

	public C_Block[] get_input_C_Block()
	{
		return input;
	}
		
	public String toString()
	{
	int i;
	String text = "";
		for(i = 0; i < input.length; i++)
			text += input[i].toString();
		return text;
	}

	public void des_cbc_encrypt
	(
		Key_schedule	schedule	//Schedule to use.
	)
	{
	int l ;
	  
	  for (l = 0; l < input.length; l++) 
	  {	    
	    input[l].Xor(ivec); //xor onto input the last C_BLOCK we encrypted
		input[l] = input[l].des_ecb_encrypt( schedule, true, false, false); /*work on this C_BLOCK*/
	    ivec = (C_Block)input[l].clone(); //update vbuf to be C_BLOCK we just encrypted*/
	  }
	}

	public void des_cbc_decrypt
	(
		Key_schedule	schedule	//Schedule to use.
	)
	{
	int l ;
	C_Block	V2;
	  
	  for (l = 0; l < input.length; l++) 
	  {	    
	    V2 = (C_Block)input[l].clone(); //save the input buffer for XOR next loop
		input[l] = input[l].des_ecb_encrypt( schedule, false, false, false); /*work on this C_BLOCK*/
	    input[l].Xor(ivec); //XOR, with output, last encrypted input C_BLOCK
	    ivec = V2; //set vbuf to saved input before it was decrypted
	  }
	}

	int des_cbc_cksum
	(
		Key_schedule schedule
	)
	{
	  des_cbc_encrypt(schedule);
	  return input[input.length - 1].second_int();
	}

	int get_last_des_cbc_cksum()
	{
	  return input[input.length - 1].second_int();
	}

	/* This is modified cbc-algorithm as specified in kerberos manual page */

	public void des_pcbc_encrypt(Key_schedule schedule)
	{
	int	V2[];
	int l;

	  for (l = 0; l < input.length; l++) 
	  {
	      V2 = input[l].int_array();
	      input[l].Xor(ivec);
	      input[l] = input[l].des_ecb_encrypt(schedule,true, false, false);
	      ivec.int_to_char(V2[0] ^ input[l].first_int(),
	      				   V2[1] ^ input[l].second_int());
	  }
	}

	public void
	des_pcbc_decrypt(Key_schedule schedule)
	{
	int	V2[];
	int	l;

	  for (l = 0; l < input.length; l++) 
	  {
	      V2 = input[l].int_array();
	  	  input[l] = input[l].des_ecb_encrypt(schedule,false, false, false);
	      input[l].Xor(ivec);
	      ivec.int_to_char((V2[0] ^ input[l].first_int()),
	      				   (V2[1] ^ input[l].second_int()) );
	  }
	}

	/*
	 * epc_encrypt is an "error propagation chaining" encrypt operation
	 * for DES, similar to CBC.  However, CBC limits errors to 2 blocks,
	 * while "epc" will propagate a single bit error anywhere in the chain
	 * all the way through to the end. 
	 * Not good encryption but good for checksum.
	 */

	public void epc_encrypt( Key_schedule schedule )	/* precomputed key schedule */
	{
	    int i;
	    C_Block t_input;
	    C_Block xor;

		xor =  (C_Block)ivec.clone();

		for (i = 0; i < input.length; i++) 
		{
		    t_input = (C_Block)input[i].clone();
		    input[i].Xor(xor);
		    input[i] = input[i].des_ecb_encrypt(schedule,true, false, false);
		    xor = t_input;
		}
	}

	public void epc_decrypt( Key_schedule schedule )	/* precomputed key schedule */
	{
	    int i;
	    C_Block xor;

		xor =  (C_Block)ivec.clone();

		for (i = 0; i < input.length; i++) 
		{
		    input[i] = input[i].des_ecb_encrypt(schedule,false, false, false);
		    input[i].Xor(xor) ;
		    xor = (C_Block)input[i].clone();
		}
	}
}