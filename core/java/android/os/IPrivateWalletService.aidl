/*
* aidl file :
* frameworks/base/core/java/android/os/IPrivateWalletService.aidl
* This file contains definitions of functions which are
* exposed by service.
*/
package android.os;
/**{@hide}*/
interface IPrivateWalletService {
    void createWallet();
    void pushDecision(String requestId, String response);
    void sendTransaction(String requestId, String to, String value, String data, String nonce, String gasPrice, String gasAmount, int chainId);
    void signMessage(String requestId, String message);
    void getAddress(String requestId);
}
