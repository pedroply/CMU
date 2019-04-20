package pt.ulisboa.tecnico.cmov.notp2photo;

import android.content.Context;
import android.util.Base64;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

import javax.crypto.Cipher;

public class RSAGenerator {

    private static String keysFileName = "Keys";

    public static KeyPair generateNReturn() throws GeneralSecurityException {


        System.out.println("Generating RSA key ..." );
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair keys = keyGen.generateKeyPair();
        System.out.println("Finish generating RSA keys");

        return keys;
    }

    public static void writeKeysToFiles(KeyPair keys, String user, Context c) throws IOException {
        FileOutputStream fos = c.openFileOutput(user+keysFileName, Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(keys);
        os.close();
        fos.close();
    }

    public static KeyPair readKeysFromFiles(String user, Context c) throws IOException, ClassNotFoundException {
        FileInputStream fis = c.openFileInput(user+keysFileName);
        ObjectInputStream is = new ObjectInputStream(fis);
        KeyPair keyPair = (KeyPair) is.readObject();
        is.close();
        fis.close();
        return keyPair;
    }

    public static PublicKey readPublicKeyBase64(String keyBase64) throws Exception {
        X509EncodedKeySpec key = new X509EncodedKeySpec(Base64.decode(keyBase64, Base64.DEFAULT));
        return KeyFactory.getInstance("RSA").generatePublic(key);
    }

    public static String getBase64PubKey(KeyPair keyPair) {
        return Base64.encodeToString(keyPair.getPublic().getEncoded(), Base64.DEFAULT);
    }

    public static byte[] encrypt(PublicKey publicKey, byte[] obj) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return cipher.doFinal(obj);
    }

    public static byte[] decrypt(PrivateKey privateKey, byte[] encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        return cipher.doFinal(encrypted);
    }

}
