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
    void sendTransaction(String session, String from, String to, String data);
    String signMessage(String session, String message);
}
