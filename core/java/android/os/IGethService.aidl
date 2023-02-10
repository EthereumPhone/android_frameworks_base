/*
* aidl file :
* frameworks/base/core/java/android/os/IGethService.aidl
* This file contains definitions of functions which are
* exposed by service.
*/
package android.os;
/**{@hide}*/
interface IGethService {
    String getEnodeURL();
    void shutdownGeth();
    void startGeth();
    void shutdownWithoutPreference();
    void changeClient(String client);
    String getCurrentClient();
    boolean isRunning();
}
