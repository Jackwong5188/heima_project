package com.tensquare.encrypt.service;

public interface RsaService {
	
	/***
     * RSA解密
     *
     * @param encryptData
     * @return
     * @throws Exception
     */
	public String rsaDecryptDataPEM(String encryptData, String prvKey) throws Exception;
	
	/***
     * RSA解密
     *
     * @param encryptBytes
     * @return
     * @throws Exception
     */
	public String rsaDecryptDataBytes(byte[] encryptBytes, String prvKey) throws Exception;
    
    /***
     * RSA加密
     *
     * @param data
     * @return
     * @throws Exception
     */
	public String rsaEncryptDataPEM(String data, String pubKey) throws Exception;
	
	public String getRsaAlgorithm();
}
