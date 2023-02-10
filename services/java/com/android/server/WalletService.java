package com.android.server;

import android.content.Context;
import android.os.Environment;
import android.os.IWalletService;
import android.util.Log;
import com.android.server.SystemService;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;
import org.web3j.crypto.WalletUtils;
import android.content.Intent;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.EOFException;

public class WalletService extends IWalletService.Stub {

  private static final String TAG = "WalletService";
  private static WalletService instance;
  private Context context;
  private String dataDir;
  private SharedState sharedState;


  public WalletService(Context con, SharedState sharedState) {
    super();
    Log.v(TAG, "WalletService, onCreate");
    dataDir = Environment.getDataDirectory().getAbsolutePath();
    context = con;
    this.sharedState = sharedState;
  }

  public String createSession() {
    UUID uuid = UUID.randomUUID();
    sharedState.addSession(uuid.toString());
    return uuid.toString();
  }

  public boolean isWalletConnected(String session) {
    // To implement
    Log.v(TAG, "isWalletConnected, " + session);
    return sharedState.hasSession(session);
  }

  public void connectToWallet(String session) {
    Log.v(TAG, "connectToWallet, " + session);
  }

  public String getChainId(String session) {
    Log.v(TAG, "getChainId, " + session);
    if (sharedState.hasSession(session)) {
      UUID uuid = UUID.randomUUID();
      sharedState.addRequest(uuid.toString());

      Intent in = new Intent("requestToSystemUI");
      in.putExtra("method", "getChainId");
      in.putExtra("requestID", uuid.toString());
      context.sendBroadcast(in);
      return uuid.toString();
    }
    return "no session";
  }

  public String sendTransaction(
      String session,
      String to,
      String value,
      String data,
      String nonce,
      String gasPrice,
      String gasAmount,
      int chainId) {
    Log.v(TAG, "sendTransaction, " + session);
    if (sharedState.hasSession(session)) {
      UUID uuid = UUID.randomUUID();
      sharedState.addRequest(uuid.toString());

      Intent in = new Intent("requestToSystemUI");
      in.putExtra("method", "sendTransaction");
      in.putExtra("requestID", uuid.toString());
      in.putExtra("to", to);
      in.putExtra("value", value);
      in.putExtra("data", data);
      in.putExtra("nonce", nonce);
      in.putExtra("gasPrice", gasPrice);
      in.putExtra("gasAmount", gasAmount);
      in.putExtra("chainId", chainId);
      context.sendBroadcast(in);
      return uuid.toString();
    }
    return "no session";
  }

  public String signMessage(String session, String message, String type) {
    Log.v(TAG, "signMessage, " + session + ": " + message);
    if (sharedState.hasSession(session)) {
      UUID uuid = UUID.randomUUID();
      sharedState.addRequest(uuid.toString());

      Intent in = new Intent("requestToSystemUI");
      in.putExtra("method", "signMessage");
      in.putExtra("requestID", uuid.toString());
      in.putExtra("message", message);
      in.putExtra("type", type);
      context.sendBroadcast(in);
      return uuid.toString();
    }
    return "no session";
  }

  public String hasBeenFulfilled(String requestID) {
    return sharedState.hasBeenFulfilled(requestID);
  }

  public String getUserDecision() {
    UUID uuid = UUID.randomUUID();
    Intent in = new Intent("requestToSystemUI");
    sharedState.addRequest(uuid.toString());
    in.putExtra("method", "getDecision");
    in.putExtra("requestID", uuid.toString());
    context.sendBroadcast(in);
    return uuid.toString();
  }

  public String getAddress(String session) {
    UUID uuid = UUID.randomUUID();
    Intent in = new Intent("requestToSystemUI");
    sharedState.addRequest(uuid.toString());
    in.putExtra("method", "getAddress");
    in.putExtra("requestID", uuid.toString());
    context.sendBroadcast(in);
    return uuid.toString();
  }

  public String changeChainId(String session, int chainId) {
    if (sharedState.hasSession(session)) {
      UUID uuid = UUID.randomUUID();
      Intent in = new Intent("requestToSystemUI");
      sharedState.addRequest(uuid.toString());
      in.putExtra("method", "changeChainId");
      in.putExtra("requestID", uuid.toString());
      in.putExtra("chainId", chainId);
      context.sendBroadcast(in);
      return uuid.toString();
    } else {
      return "no session";
    }
  }

}
