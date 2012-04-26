package nz.ac.auckland.cs.des;


public class Key_schedule extends Object
{

	static byte	PC1[] = 
	{
	    56,48,40,32,24,16, 8,
	     0,57,49,41,33,25,17,
	     9, 1,58,50,42,34,26,
	    18,10, 2,59,51,43,35,
	    62,54,46,38,30,22,14,
	     6,61,53,45,37,29,21,
	    13, 5,60,52,44,36,28,
	    20,12, 4,27,19,11, 3,
	};
	
	static byte	PC2[] = 
	{
	    13,16,10,23, 0, 4,
	     2,27,14, 5,20, 9,
	    22,18,11, 3,25, 7,
	    15, 6,26,19,12, 1,
	    40,51,30,36,46,54,
	    29,39,50,44,32,47,
	    43,48,38,55,33,52,
	    45,41,49,35,28,31,
	};
	
	static byte	LS[] = {1,1,2,2,2,2,2,2,1,2,2,2,2,2,2,1};
	
	static int	shift_arr[] = 
	{
	    0x00000001, 0x00000002, 0x00000004, 0x00000008,
	    0x00000010, 0x00000020, 0x00000040, 0x00000080,
	    0x00000100, 0x00000200, 0x00000400, 0x00000800,
	    0x00001000, 0x00002000, 0x00004000, 0x00008000,
	    0x00010000, 0x00020000, 0x00040000, 0x00080000,
	    0x00100000, 0x00200000, 0x00400000, 0x00800000,
	    0x01000000, 0x02000000, 0x04000000, 0x08000000,
	    0x10000000, 0x20000000, 0x40000000, 0x80000000,
	};
	
	//  Key schedule calculation could be done with precomputed inline code
	//  similarly to 64-bit permutation calculation, but it would take about
	//  18kbytes of code space 
	
	// The least significant bit of key->data[0] is bit # 1 in  DES-sepcification etc. 

	int	 s_data[]  = new int[32]; // 1024 bits (128 bytes or 16 C_Block's) 
	C_Block key;
	
	public Key_schedule() //create a null filled key_schedule
					      //Not useful until it has been initialized
	{
	int		i;
		for(i = 0; i < 32; i++)
			s_data[i] = 0;
	}
	
	public Key_schedule(String asckey) // Was string_to_key()
	{
	int		i, j;
	C_Block	k1 = new C_Block(); //create 2 tempory C_Blocks k1 & k2
	C_Block	k2 = new C_Block();
	des_hash_init dhi = new des_hash_init(); //get hash keys
				
		key = new C_Block();	//create a C_Block for the key
		for(i = 0, j = 0; j < asckey.length(); j++, i++) 
		{
			i %= 8;
			k1.data[i] |= (byte) asckey.charAt(j);
			k2.data[i] |= (byte) asckey.charAt(j);
			k1 = k1.des_ecb_encrypt(dhi.des_hash_key1, true, false, false);
			k2 = k2.des_ecb_encrypt(dhi.des_hash_key2, true, false, false);
		}
		for(i = 0; i < 8; i++) 
		{
			key.data[i] = (byte) (k1.data[i] ^ k2.data[i]);
		}
		des_set_key(key); //sets up the Schedule data array from the key
	}
	
	public	Key_schedule(C_Block newkey)
	{
	 	des_set_key(newkey); //sets up the Schedule data array from the key
	}
	
	public	C_Block des_get_key()
	{
		return key;
	}
	
	public int get_schedule(int l)
	{
		return s_data[l];
	}
	
	public int[] get_schedule()
	{
		return s_data;
	}
	
	public	void des_set_key(C_Block newkey)
	{
	int	 Result_0;
	int	 Result_1;
	int	 sa = 0;
	int	 Input[];
	int		i;
	int		j;
	int 	kp = 0;
	int		Tmp;
	int		result_bit;
	int		side;
	int		column;
	int		result_side;

		key = newkey;
	    Input = key.int_array();

	    for(i = 0; i < 16; i++) 
		{
			sa += LS[i];
			Result_0 = Result_1 = 0; //start with 0 in the result fields
			result_side = 0;
			for(j = 0; j < 48; j++) 
			{
				Tmp = PC2[j];
				side = (Tmp >= 28) ? 1:0;
				column = (Tmp + sa) % 28;
				column = PC1[column + side * 28];
				if (column >= 32) 
				{
					side = 1;
					column -= 32;
				} 
				else 
				{
					side = 0;
				}
				if (j >= 24) 
				{
					result_side = 1;
					result_bit = j - 24;
				} 
				else 
				{
					result_bit = j;
				}
				result_bit = (result_bit / 6) * 8 + (result_bit % 6);
				if ((Input[side] & shift_arr[column]) != 0)
				{
					if (result_side != 0) 
					{
						Result_1 |= shift_arr[result_bit];
					} 
					else 
					{
						Result_0 |= shift_arr[result_bit];
					}
				}
			}
			s_data[kp++] = Result_0; 
			s_data[kp++] = Result_1;
		}
	}
	

	public String toString()
	{
	int i;
	String text = "";
	
		for(i = 0; i < 32; i++)
			text +=  uHex.toHex(s_data[i]) + " ";
		return text;
	}
}

