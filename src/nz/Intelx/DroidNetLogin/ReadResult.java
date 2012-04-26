package nz.Intelx.DroidNetLogin;

class ReadResult
{
	int Result;
	int Packet_type;
	int Packet_Version;
	
	public ReadResult(int The_Result, int The_Packet_Type, int The_Packet_Version)
	{
		Result = The_Result;
		Packet_type = The_Packet_Type;
		Result = The_Packet_Version;
	}
};