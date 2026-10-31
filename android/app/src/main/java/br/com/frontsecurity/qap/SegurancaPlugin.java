package br.com.frontsecurity.qap;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Ponte entre a página (config.js) e o Android:
 * - assinar(): assina cada pedido ao servidor com o segredo que vai só dentro do app
 *   (HMAC-SHA256 de "<ts>.<n>.<acao>.<posto>.<aparelho>"). O navegador não tem o segredo, então
 *   com "Exigir app" ligado o servidor recusa quem não está no app.
 * - info(): versão do app e código do aparelho (gerado na instalação).
 * - eventos "print" e "gravacao": vindos da MainActivity.
 */
@CapacitorPlugin(name = "Seguranca")
public class SegurancaPlugin extends Plugin {

    private static SegurancaPlugin atual;

    @Override
    public void load() {
        atual = this;
    }

    static void avisar(String tipo, boolean gravando) {
        SegurancaPlugin p = atual;
        if (p == null) return;
        JSObject dados = new JSObject();
        dados.put("gravando", gravando);
        p.notifyListeners(tipo, dados, true);
    }

    private String aparelho() {
        SharedPreferences prefs = getContext().getSharedPreferences("seguranca", Context.MODE_PRIVATE);
        String id = prefs.getString("aparelho", null);
        if (id == null) {
            id = UUID.randomUUID().toString().replace("-", "");
            prefs.edit().putString("aparelho", id).apply();
        }
        return id;
    }

    private static String hmac(String texto) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(BuildConfig.APP_SEGREDO.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] r = mac.doFinal(texto.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : r) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    @PluginMethod
    public void assinar(PluginCall call) {
        try {
            long ts = call.getLong("ts", System.currentTimeMillis());
            String acao = call.getString("acao", "");
            String posto = call.getString("p", "");
            String n = UUID.randomUUID().toString().replace("-", "");
            String disp = aparelho();
            JSObject r = new JSObject();
            r.put("ts", ts);
            r.put("n", n);
            r.put("disp", disp);
            r.put("v", BuildConfig.VERSION_CODE);
            r.put("ass", hmac(ts + "." + n + "." + acao + "." + posto + "." + disp));
            call.resolve(r);
        } catch (Exception e) {
            call.reject("assinatura");
        }
    }

    @PluginMethod
    public void info(PluginCall call) {
        JSObject r = new JSObject();
        r.put("versao", BuildConfig.VERSION_CODE);
        r.put("aparelho", aparelho());
        r.put("android", Build.VERSION.SDK_INT);
        call.resolve(r);
    }
}
