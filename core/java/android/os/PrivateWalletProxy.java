package android.os;
/**
 * /framework/base/core/java/android/os/PrivateWalletProxy.java
 * This will be the private proxy only available to the system apps.
 * Use this Singleton class to call the private functionality of PrivateWalletService
 * @author mhaas.eth
 */
import android.annotation.NonNull;
import android.util.Log;
public class PrivateWalletProxy {
    private static final String TAG = "PrivateWalletProxy";
    private static PrivateWalletProxy myProxy;
    private final IPrivateWalletService mIMyService;
    /**
     * Use {@link #getmyProxy} to get the myProxy instance.
     */
    PrivateWalletProxy(IPrivateWalletService myService) {
        if (myService == null) {
            throw new IllegalArgumentException("my service is null");
        }
        mIMyService = myService;
    }
    /** Get a handle to the MyService.
     * @return the MyService, or null.
     */
    @NonNull
    public static PrivateWalletProxy getWalletProxy() {
        Object mThingLock = new Object();
        synchronized (mThingLock) {
            if (myProxy == null) {
                IBinder binder = android.os.ServiceManager.getService("privatewallet");
                if (binder != null) {
                    IPrivateWalletService managerService = IPrivateWalletService.Stub.asInterface(binder);
                    myProxy = new PrivateWalletProxy(managerService);
                } else {
                    Log.e(TAG, "Service binder is null");
                }
            }
            return myProxy;
        }
        
    }

    @NonNull
    public void createWallet() {
        try {
            mIMyService.createWallet();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    public void pushDecision(@NonNull String requestId, @NonNull String response) {
        try {
            mIMyService.pushDecision(requestId, response);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    public void sendTransaction(@NonNull String requestId, @NonNull String to, @NonNull String value, @NonNull String data, @NonNull String nonce, @NonNull String gasPrice, @NonNull String gasAmount, @NonNull int chainId) {
        try {
            mIMyService.sendTransaction(requestId, to, value, data, nonce, gasPrice, gasAmount, chainId);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    public void signMessage(@NonNull String requestId, @NonNull String message)  {
        try {
            mIMyService.signMessage(requestId, message);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    public void getAddress(@NonNull String requestId) {
        try {
            mIMyService.getAddress(requestId);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
   
}