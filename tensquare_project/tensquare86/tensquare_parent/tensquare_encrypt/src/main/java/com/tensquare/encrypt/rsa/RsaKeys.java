package com.tensquare.encrypt.rsa;

/**
 * rsa加解密用的公钥和私钥
 * @author Administrator
 *
 */
public class RsaKeys {

	//生成秘钥对的方法可以参考这篇帖子
	//https://www.cnblogs.com/yucy/p/8962823.html

//	//服务器公钥
//	private static final String serverPubKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA6Dw9nwjBmDD/Ca1QnRGy"
//											 + "GjtLbF4CX2EGGS7iqwPToV2UUtTDDemq69P8E+WJ4n5W7Iln3pgK+32y19B4oT5q"
//											 + "iUwXbbEaAXPPZFmT5svPH6XxiQgsiaeZtwQjY61qDga6UH2mYGp0GbrP3i9TjPNt"
//											 + "IeSwUSaH2YZfwNgFWqj+y/0jjl8DUsN2tIFVSNpNTZNQ/VX4Dln0Z5DBPK1mSskd"
//											 + "N6uPUj9Ga/IKnwUIv+wL1VWjLNlUjcEHsUE+YE2FN03VnWDJ/VHs7UdHha4d/nUH"
//											 + "rZrJsKkauqnwJsYbijQU+a0HubwXB7BYMlKovikwNpdMS3+lBzjS5KIu6mRv1GoE"
//											 + "vQIDAQAB";
//
//	//服务器私钥(经过pkcs8格式处理)
//	private static final String serverPrvKeyPkcs8 = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDoPD2fCMGYMP8J"
//				 								 + "rVCdEbIaO0tsXgJfYQYZLuKrA9OhXZRS1MMN6arr0/wT5YniflbsiWfemAr7fbLX"
//				 								 + "0HihPmqJTBdtsRoBc89kWZPmy88fpfGJCCyJp5m3BCNjrWoOBrpQfaZganQZus/e"
//				 								 + "L1OM820h5LBRJofZhl/A2AVaqP7L/SOOXwNSw3a0gVVI2k1Nk1D9VfgOWfRnkME8"
//				 								 + "rWZKyR03q49SP0Zr8gqfBQi/7AvVVaMs2VSNwQexQT5gTYU3TdWdYMn9UeztR0eF"
//				 								 + "rh3+dQetmsmwqRq6qfAmxhuKNBT5rQe5vBcHsFgyUqi+KTA2l0xLf6UHONLkoi7q"
//				 								 + "ZG/UagS9AgMBAAECggEBANP72QvIBF8Vqld8+q7FLlu/cDN1BJlniReHsqQEFDOh"
//				 								 + "pfiN+ZZDix9FGz5WMiyqwlGbg1KuWqgBrzRMOTCGNt0oteIM3P4iZlblZZoww9nR"
//				 								 + "sc4xxeXJNQjYIC2mZ75x6bP7Xdl4ko3B9miLrqpksWNUypTopOysOc9f4FNHG326"
//				 								 + "0EMazVaXRCAIapTlcUpcwuRB1HT4N6iKL5Mzk3bzafLxfxbGCgTYiRQNeRyhXOnD"
//				 								 + "eJox64b5QkFjKn2G66B5RFZIQ+V+rOGsQElAMbW95jl0VoxUs6p5aNEe6jTgRzAT"
//				 								 + "kqM2v8As0GWi6yogQlsnR0WBn1ztggXTghQs2iDZ0YkCgYEA/LzC5Q8T15K2bM/N"
//				 								 + "K3ghIDBclB++Lw/xK1eONTXN+pBBqVQETtF3wxy6PiLV6PpJT/JIP27Q9VbtM9UF"
//				 								 + "3lepW6Z03VLqEVZo0fdVVyp8oHqv3I8Vo4JFPBDVxFiezygca/drtGMoce0wLWqu"
//				 								 + "bXlUmQlj+PTbXJMz4VTXuPl1cesCgYEA6zu5k1DsfPolcr3y7K9XpzkwBrT/L7LE"
//				 								 + "EiUGYIvgAkiIta2NDO/BIPdsq6OfkMdycAwkWFiGrJ7/VgU+hffIZwjZesr4HQuC"
//				 								 + "0APsqtUrk2yx+f33ZbrS39gcm/STDkVepeo1dsk2DMp7iCaxttYtMuqz3BNEwfRS"
//				 								 + "kIyKujP5kfcCgYEA1N2vUPm3/pNFLrR+26PcUp4o+2EY785/k7+0uMBOckFZ7GIl"
//				 								 + "FrV6J01k17zDaeyUHs+zZinRuTGzqzo6LSCsNdMnDtos5tleg6nLqRTRzuBGin/A"
//				 								 + "++xWn9aWFT+G0ne4KH9FqbLyd7IMJ9R4gR/1zseH+kFRGNGqmpi48MS61G0CgYBc"
//				 								 + "PBniwotH4cmHOSWkWohTAGBtcNDSghTRTIU4m//kxU4ddoRk+ylN5NZOYqTxXtLn"
//				 								 + "Tkt9/JAp5VoW/41peCOzCsxDkoxAzz+mkrNctKMWdjs+268Cy4Nd09475GU45khb"
//				 								 + "Y/88qV6xGz/evdVW7JniahbGByQhrMwm84R9yF1mNwKBgCIJZOFp9xV2997IY83S"
//				 								 + "habB/YSFbfZyojV+VFBRl4uc6OCpXdtSYzmsaRcMjN6Ikn7Mb9zgRHR8mPmtbVfj"
//				 								 + "B8W6V1H2KOPfn/LAM7Z0qw0MW4jimBhfhn4HY30AQ6GeImb2OqOuh3RBbeuuD+7m"
//				 								 + "LpFZC9zGggf9RK3PfqKeq30q";

	//服务器公钥
	private static final String serverPubKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqbF9rtKdKqZqbvT6+5Dg\n" +
			"hdO7cwtO5IKRv+i5E5sQa7/HT6yxueGDOh/AcUuzGgrL1FWoVZmrC20EsUgC2hUw\n" +
			"UC21NqR4dI1zK5KUmn3CgqweLNpttVsRtQoWvE2UL/lpDhAXG2W35VS74Na9qhY2\n" +
			"1JGUrXl1a7VPhUfGfpq+b4OImae72v+tD3aygY2BZvvrgWzUCMCnYSN3oly0eITB\n" +
			"FwMCSoCAdc6vdLuBA+OL0J613KcdSh4457KQcjpWND33fBgAWksNBq/Njv3+TJjk\n" +
			"AWsdJLwja7DRP9iuavgiaoaX7wD0GBsPZsJ8v4jp+prrvU6xfWtpqiH8HqSfkX6q\n" +
			"2QIDAQAB";

	//服务器私钥(经过pkcs8格式处理)
	private static final String serverPrvKeyPkcs8 = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCpsX2u0p0qpmpu\n" +
			"9Pr7kOCF07tzC07kgpG/6LkTmxBrv8dPrLG54YM6H8BxS7MaCsvUVahVmasLbQSx\n" +
			"SALaFTBQLbU2pHh0jXMrkpSafcKCrB4s2m21WxG1Cha8TZQv+WkOEBcbZbflVLvg\n" +
			"1r2qFjbUkZSteXVrtU+FR8Z+mr5vg4iZp7va/60PdrKBjYFm++uBbNQIwKdhI3ei\n" +
			"XLR4hMEXAwJKgIB1zq90u4ED44vQnrXcpx1KHjjnspByOlY0Pfd8GABaSw0Gr82O\n" +
			"/f5MmOQBax0kvCNrsNE/2K5q+CJqhpfvAPQYGw9mwny/iOn6muu9TrF9a2mqIfwe\n" +
			"pJ+RfqrZAgMBAAECggEBAJXulf7OZkFV0nqBB/UYF9kGr/vBBQ7P8tleVZBmFMcv\n" +
			"JnpWOp3jDCBiB5XS7cdXvKq6mEYxxTgONfQxjRuFVy+wgwlpnmhUxmq8U4KBFUiB\n" +
			"rROyVvbDJU0RQe9UmFcfBlx/iPsTD72X9Xx+IAxVHlMI4hW4WDA3fbBkL5STQymg\n" +
			"3EGLRkJJCtmdYeacwVxSu3Q4oSBzwDru8gUOMtDVOfVPpFnrRKKsuNerSJ9O3jqA\n" +
			"C9aKVhT4JN8Xvv+g+mygq/KAwo5+jFXG/zVkIli/lbQJPzTmxb5/8gaNJmivKp5s\n" +
			"udKX83/aqOi4+49g1C/so89MqJWXwIXXb22bsgLuZKUCgYEA3FmjIGLPzlLoviH4\n" +
			"hXAgbNoEX3KYwHIaESQQte3EMqk2uUGzkmgzsuEvfsl8aYaD6iyE49fn+TuN/TwM\n" +
			"1vpwPgHCEjolZ3NhGfGNWEeOysyz6q1CnWlwOMLUXBMmmVYw50AdU3ODB5IGX1ia\n" +
			"QCQ7s5ga1T8C+lvO4u785XQoR/MCgYEAxSXFu3LihOBIxsCzSmE8ZV2my32OyjCH\n" +
			"dkI9lRG55jKDgNfRL1Wa1GDqqosLd0F12Gng1aT0noHKgmNP6hxHjo2jok7hoCCU\n" +
			"goNmDEvh5WAZdT3I4xRHO9Wj4aJIqvEjKTwJPuUNReaPmAJP7rofT11ntfKaeLc5\n" +
			"ARY/OEiioQMCgYBjmkEaio/Ue7Wo/EISTzTY/1OqsWpK00jlJqBHHErre4G5C/HX\n" +
			"5DI6UX/el8UdDiji2WzYcOfyyPDN3y8OnfXOkYzgIFh4AreJup5hoTbyhMVu9xJ9\n" +
			"hQOtJKRJWn7AIS3J2jlnV3eLHH606qAPDlHORJsceXcnJAjIm67FKVRr8QKBgQCQ\n" +
			"EW5R8T5/g7crJx7eN/h+ytVc10BFmPkk/nsqOG31PuzCdvQy9wfSM5DJWBrk5z1L\n" +
			"DPR0q7rnKknmxtRbbJcOIYt2zNYswiJEzekBC2Lk0DlKaAApkWQM4a6E8KbR5K2h\n" +
			"MHJBN/lfH4HHv8jR18mRodxsJgGkPaC2np9MbigKKQKBgAcHLJBTZgaMeoxcCXCd\n" +
			"2jsTnW8agiSsES3YsRBE7bpCvBE/fEgDRCLgO3EXELslYwjmkA3OXW2kxfGQSuc9\n" +
			"jbnOanb/fzK6W1ot3AcJilU9VJKBHWpiRwY1Uko1Xs2TUdnZI8hNIq9DI+y2kUfK\n" +
			"kgmpU1WyTm9W7X6FBYk8ITa7";

	public static String getServerPubKey() {
		return serverPubKey;
	}

	public static String getServerPrvKeyPkcs8() {
		return serverPrvKeyPkcs8;
	}
	
}
