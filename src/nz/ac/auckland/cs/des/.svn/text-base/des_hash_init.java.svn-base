package nz.ac.auckland.cs.des;

//  des_hash_init prepares two key schedules to be used with
//  hash-function calculation by des algorithm 
//  Only one instance of this class is needed
import java.lang.*;

//package java.des;

public class des_hash_init extends Object
{
	public static Key_schedule	des_hash_key1;
	public static Key_schedule	des_hash_key2;
	
	static boolean	des_hash_inited = false; //Says we have inited at least one instance
	
     public des_hash_init()
	{	
	 byte hash_key1_data[] =
	{
		(byte)0x9a, (byte)0xd3, (byte)0xbc, (byte)0x24, 
		(byte)0x10, (byte)0xe2, (byte)0x8f, (byte)0x0e 
	};
	 byte hash_key2_data[] =
	{
		(byte)0xe2, (byte)0x95, (byte)0x14, (byte)0x33, 
		(byte)0x59, (byte)0xc3, (byte)0xec, (byte)0xa8 
	};
	
		if (des_hash_inited == false)
		{
			des_hash_key1 = new Key_schedule(new C_Block(hash_key1_data));
			des_hash_key2 = new Key_schedule(new C_Block(hash_key2_data));
			des_hash_inited = true;
		}
	}
	
	public Key_schedule get_Key_schedule1()
	{
		return des_hash_key1;
	}
	
	public Key_schedule get_Key_schedule2()
	{
		return des_hash_key2;
	}
	
	public String toString()
	{
		return des_hash_key1.toString() + " " + des_hash_key2.toString();
	}
}
