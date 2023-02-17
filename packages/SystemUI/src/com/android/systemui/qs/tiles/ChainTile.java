package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.SecureSetting;
import com.android.systemui.qs.tiles.dialog.ChainIdDialogFactory;
import android.os.Handler;
import com.android.systemui.dagger.qualifiers.Background;
import com.android.systemui.dagger.qualifiers.Main;
import android.os.Looper;
import com.android.systemui.plugins.qs.QSTile.BooleanState;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.qs.logging.QSLogger;
import android.view.View;
import androidx.annotation.Nullable;
import java.lang.reflect.*;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import android.provider.Settings;

import javax.inject.Inject;


public class ChainTile extends QSTileImpl<BooleanState> {
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("changeChain")) {
                refreshState();
            }
        }
    };
    private final ChainIdDialogFactory mChainIdDialogFactory;
    private final Handler mHandler;

    @Inject
    public ChainTile(
        QSHost host, 
        @Background Looper backgroundLooper,
        @Main Handler mainHandler,
        FalsingManager falsingManager,
        MetricsLogger metricsLogger,
        StatusBarStateController statusBarStateController,
        ActivityStarter activityStarter,
        QSLogger qsLogger,
        ChainIdDialogFactory chainIdDialogFactory) {
        super(host, backgroundLooper, mainHandler, falsingManager, metricsLogger,
            statusBarStateController, activityStarter, qsLogger);
        mHandler = mainHandler;
        mChainIdDialogFactory = chainIdDialogFactory;
        mContext.registerReceiver(mBroadcastReceiver, new IntentFilter("changeChain"));
    }

    @Override
    public BooleanState newTileState() {
        BooleanState state = new BooleanState();
        state.value = true;
        state.icon = ResourceIcon.get(R.drawable.ic_chain);
        state.label = mContext.getString(R.string.chain_tile_label);
        // Get chainId string
        int chainId = getChainId();
        if (chainId == 1){
            state.secondaryLabel = "Ethereum Mainnet";
        } else if (chainId == 10){
            state.secondaryLabel = "Optimism";
        } else if (chainId == 42161){
            state.secondaryLabel = "Arbitrum One";
        } else if (chainId == 5){
            state.secondaryLabel = "Goerli Testnet";
        } else {
            state.secondaryLabel = "Chain: " + chainId;
        }
        
        state.expandedAccessibilityClassName = QSTileImpl.class.getName();
        state.contentDescription = mContext.getString(R.string.chain_tile_label);
        return state;
    }

    @Override
    public Intent getLongClickIntent() {
        return new Intent(Settings.ACTION_WIFI_SETTINGS);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.QS_WIFI;
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.chain_tile_label);
    }

    @Override
    public void handleClick(@Nullable View view) {
        System.out.println("SystemUI: Clicked chain tile");
        mHandler.post(() -> mChainIdDialogFactory.create(true, view));
    }

    @Override
    public void handleUpdateState(BooleanState state, Object arg) {
        state.value = true;
        state.icon = ResourceIcon.get(R.drawable.ic_chain);
        state.label = mContext.getString(R.string.chain_tile_label);
        // Get chainId string
        int chainId = getChainId();
        if (chainId == 1){
            state.secondaryLabel = "Ethereum Mainnet";
        } else if (chainId == 10){
            state.secondaryLabel = "Optimism";
        } else if (chainId == 42161){
            state.secondaryLabel = "Arbitrum One";
        } else if (chainId == 5){
            state.secondaryLabel = "Goerli Testnet";
        } else {
            state.secondaryLabel = "Chain: " + chainId;
        }
        
        state.expandedAccessibilityClassName = QSTileImpl.class.getName();
        state.contentDescription = mContext.getString(R.string.chain_tile_label);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private CharSequence getSecondaryLabel(boolean isTransient, String statusLabel) {
        int chainId = getChainId();
        if (chainId == 1){
            return "Ethereum Mainnet";
        } else if (chainId == 10){
            return "Optimism";
        } else if (chainId == 42161){
            return "Arbitrum One";
        } else if (chainId == 5){
            return "Goerli Testnet";
        } else {
            return "Chain: " + chainId;
        }
    }
    
    public int getChainId() {
        try {
            System.out.println("Getting chain id");
            Object privateWalletManager = mContext.getSystemService("privatewallet");
            Class privateWalletProxy = Class.forName("android.os.PrivateWalletProxy");
            Method directGetChainId = privateWalletProxy.getDeclaredMethod("directGetChainId");
            Object out = directGetChainId.invoke(privateWalletManager);
            return (int) out;
        }catch (Exception e) {
            System.out.println("Failed to get chain id");
            e.printStackTrace();
            return 0;
        }
    }
      
}
