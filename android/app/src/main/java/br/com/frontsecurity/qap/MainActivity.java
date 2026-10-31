package br.com.frontsecurity.qap;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import com.getcapacitor.BridgeActivity;

import java.util.function.Consumer;

/**
 * Tela única do app. FLAG_SECURE: print e gravação de tela saem pretos (e o app some da lista de
 * recentes). Android 14+: avisa quando alguém tira print. Android 15+: avisa quando a tela do app
 * está sendo gravada. Os avisos vão para o plugin Seguranca, que repassa para a página.
 */
public class MainActivity extends BridgeActivity {

    private Activity.ScreenCaptureCallback avisoPrint;
    private Consumer<Integer> avisoGravacao;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(SegurancaPlugin.class);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= 34) {
            avisoPrint = () -> SegurancaPlugin.avisar("print", false);
            registerScreenCaptureCallback(getMainExecutor(), avisoPrint);
        }
        if (Build.VERSION.SDK_INT >= 35) {
            avisoGravacao = estado -> SegurancaPlugin.avisar("gravacao", estado == WindowManager.SCREEN_RECORDING_STATE_VISIBLE);
            int agora = getWindowManager().addScreenRecordingCallback(getMainExecutor(), avisoGravacao);
            if (agora == WindowManager.SCREEN_RECORDING_STATE_VISIBLE) SegurancaPlugin.avisar("gravacao", true);
        }
    }

    @Override
    public void onStop() {
        if (Build.VERSION.SDK_INT >= 34 && avisoPrint != null) {
            unregisterScreenCaptureCallback(avisoPrint);
            avisoPrint = null;
        }
        if (Build.VERSION.SDK_INT >= 35 && avisoGravacao != null) {
            getWindowManager().removeScreenRecordingCallback(avisoGravacao);
            avisoGravacao = null;
        }
        super.onStop();
    }
}
