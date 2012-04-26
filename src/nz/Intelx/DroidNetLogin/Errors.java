package nz.Intelx.DroidNetLogin;

public class Errors
{

	//result codes from command requests
	final  static int CMD_RSLT_OK					=	1;	//Command successful.
	final  static int CMD_RSLT_INCOMPLETE			=	2;	//Command still in progress. (a hack)
	final  static int CMD_RSLT_WOULD_BLOCK			=	10;
	final  static int CMD_RSLT_NOT_AUTHORISED 		=	11;
	final  static int CMD_RSLT_UNKNOWN_COMMAND 		=	12;
	final  static int CMD_RSLT_UNKNOWN_CLIENT 		=	13;
	final  static int CMD_RSLT_USER_BANNED			=	14;
	final  static int CMD_RSLT_USER_WAS_BANNED		=	15;
	final  static int CMD_RSLT_AUTH_FAILURE 		=	16;
	final  static int CMD_RSLT_COMMUNICATION_FAILUE =	17;
	final  static int CMD_RSLT_MALLOC_ERROR 		=	18;
	final  static int CMD_RSLT_IDENTD_FAILUE 		=	19;
	final  static int CMD_RSLT_USER_INVALID			=	20;
	final  static int CMD_RSLT_RLOCK_FAILURE		=	21;
	final  static int CMD_RSLT_WLOCK_FAILURE		=	22;
	final  static int CMD_RSLT_IPAAddEntry_FAILED	=	23;

	final static String error_messages[] = 
	{
		"",								//0
		"OK", 							//Command successful.
		"INCOMPLETE", 					//Command still in progress. (a hack)
		"",								//3
		"",								//4
		"",								//5
		"",								//6
		"",								//7
		"",								//8
		"",								//9
		"WOULD_BLOCK", 					//
		"NOT_AUTHORISED", 				//
		"UNKNOWN_COMMAND", 				//
		"UNKNOWN_CLIENT", 				//
		"USER_BANNED", 					//
		"USER_WAS_BANNED", 				//
		"AUTH_FAILURE", 				//
		"COMMUNICATION_FAILUE", 		//
		"MALLOC_ERROR", 				//
		"IDENTD_FAILUE", 				//
		"USER_INVALID", 				//
		"RLOCK_FAILURE", 				//
		"WLOCK_FAILURE", 				//
		"IPAAddEntry_FAILED" 			//
	};
}
