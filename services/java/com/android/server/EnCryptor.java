package com.android.server;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import android.annotation.NonNull;
import android.os.Environment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import android.util.Base64;
import java.io.FileWriter;
import java.io.File;
import java.util.Scanner;

public class EnCryptor {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";

    private String dataDir;

    private byte[] encryption;
    private byte[] iv;

    EnCryptor() {
        dataDir = Environment.getDataDirectory().getAbsolutePath();
    }

    byte[] encryptText(final String alias, final String textToEncrypt)
            throws Exception {

        final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(alias));

        iv = cipher.getIV();
        saveIV(iv);

        return (encryption = cipher.doFinal(textToEncrypt.getBytes(StandardCharsets.UTF_8)));
    }

    private SecretKey getSecretKey(final String alias) throws Exception {

        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);

        KeyStore.SecretKeyEntry existingKey = (KeyStore.SecretKeyEntry) keyStore.getEntry(alias, null);
        return existingKey != null ? existingKey.getSecretKey() : createKey(alias);
    }

    private SecretKey createKey(final String alias) throws Exception {

        System.out.println("PrivateWalletService: Creating key");

        final KeyGenerator keyGenerator = KeyGenerator
                .getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);

        keyGenerator.init(new KeyGenParameterSpec.Builder(alias,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build());

        return keyGenerator.generateKey();
    }

    public void saveIV(byte[] ivToSave) {
        try {
            FileWriter myWriter = new FileWriter(dataDir + "/savedIV.txt");
            myWriter.write(Base64.encodeToString(ivToSave, Base64.DEFAULT));
            myWriter.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    byte[] getEncryption() {
        return encryption;
    }

    byte[] getIv() throws Exception{
        if (iv != null) {
            return iv;
        } else {
            File file = new File(dataDir, "savedIV.txt");
            Scanner scanner = new Scanner(file);
            String stringIv = scanner.useDelimiter("\\Z").next();
            scanner.close();
            iv = Base64.decode(stringIv, Base64.DEFAULT);
            return iv;
        }
    }
}