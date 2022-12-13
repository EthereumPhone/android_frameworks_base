/*
* aidl file :
* frameworks/base/core/java/android/os/IWalletService.aidl
* This file contains definitions of functions which are
* exposed by service.
*/
package android.os;
/**{@hide}*/
interface IWalletService {
    String createSession();
    boolean isWalletConnected(String session);
    void connectToWallet(String session);
    String sendTransaction(String session, String to, String value, String data, String nonce, String gasPrice, String gasAmount, int chainId);
    String signMessage(String session, String message, boolean type);
    String hasBeenFulfilled(String requestID);
    String getUserDecision();
    String getAddress(String session);
}
