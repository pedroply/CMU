package pt.ulisboa.tecnico.cmov.notp2photo;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class SymmetricCrypto {

    private static final String CIPHER_ALGO_KEY = "AES";
    private static final String CIPHER_ALGO = "AES/CTS/NoPadding";
    private static final int CIPHER_KEY_SIZE = 256;

    private static final String KDF_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int KDF_ITERATIONS = 2048;

    private static final String DEFAULT_SALT = "F2BdmZEp8q";

    private static IvParameterSpec ivparams;

    // keys ------------------------------------------------------------------

    public static Key generateRandomKey()
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        return generateKeyFromPassword(UUID.randomUUID().toString(), DEFAULT_SALT);
    }

    /** Generates a Key from a Password and a Salt using a Key Derivation Function.
     * @param password The Password to use.
     * @param salt The Salt to use. */
    private static Key generateKeyFromPassword(String password, String salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        char[] passwordCharArray = password.toCharArray();
        byte[] saltByteArray = salt.getBytes();
        PBEKeySpec spec = new PBEKeySpec(passwordCharArray, saltByteArray, KDF_ITERATIONS, CIPHER_KEY_SIZE);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(KDF_ALGORITHM);
        SecretKey tmp = skf.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), CIPHER_ALGO_KEY);
        return secret;
    }

    // ciphers ---------------------------------------------------------------

    private static Cipher initCipher(int opmode, Key key, byte[] iv)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
        if(iv == null){
            ivparams = generateIV(cipher);
        } else{
            ivparams = new IvParameterSpec(iv);
        }
        cipher.init(opmode, key, ivparams);

        return cipher;
    }

    private static Cipher initCipher(Key key, byte[] iv)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        return initCipher(Cipher.ENCRYPT_MODE, key, iv);
    }

    private static Cipher initDecipher(Key key, byte[] iv)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        return initCipher(Cipher.DECRYPT_MODE, key, iv);
    }


    public static byte[] encrypt(Key key, byte[] plainBytes, byte[] iv) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        byte[] encryptedBytes = initCipher(key, iv).doFinal(plainBytes);
        return encryptedBytes;
    }

    public static byte[] decrypt(Key key, byte[] encryptedBytes, byte[] iv) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        byte[] plainBytes = initDecipher(key, iv).doFinal(encryptedBytes);
        return plainBytes;
    }

    private static IvParameterSpec generateIV(Cipher cipher) throws NoSuchAlgorithmException {
        SecureRandom randomSecureRandom = new SecureRandom();
        byte[] iv = new byte[cipher.getBlockSize()];
        randomSecureRandom.nextBytes(iv);
        IvParameterSpec ivParams = new IvParameterSpec(iv);
        return ivParams;
    }

    public static IvParameterSpec generateNewIv() throws NoSuchAlgorithmException, NoSuchPaddingException {
        Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
        return generateIV(cipher);
    }


}
