# Front Security QAP — app Android

App que abre o site do QAP (`https://danielschiavolin.github.io/qap/app.html`) com:
- **Print e gravação de tela bloqueados** (`FLAG_SECURE`: a imagem sai preta). O app também some da tela de apps recentes.
- **Aviso de tentativa**: Android 14+ avisa o print, Android 15+ avisa a gravação. O app mostra a tela vermelha e manda para o painel (🛡️ Segurança).
- **Pedidos assinados**: cada pedido ao servidor sai com uma assinatura feita com o segredo `APP_SEGREDO`, que só existe dentro do app. Com **Exigir o app** ligado no painel, o servidor recusa quem não está no app (navegador, script, etc.).

O conteúdo vem do site, então mudança nas telas **não** precisa de APK novo. APK novo só quando mudar algo daqui (Android).

## Gerar o APK
É automático: cada `git push` na `main` faz o GitHub gerar e publicar o APK em **Releases**.
Link fixo para baixar (é o que vai para os QRAs): `https://github.com/DanielSchiavolin/qap-app/releases/latest/download/front-security-qap.apk`

Segredos (GitHub > repositório `qap-app` > Settings > Secrets and variables > Actions):
| Nome | De onde vem |
|---|---|
| `APP_SEGREDO` | Painel do QAP > 🛡️ Segurança > 🔑 Código do app |
| `CHAVE_BASE64` | arquivo `chave-base64.txt` da pasta `qap-app-chaves` (no computador, fora do Git) |
| `CHAVE_SENHA` | arquivo `senha.txt` da mesma pasta |

**Guarde a pasta `qap-app-chaves`** (pen drive / Drive pessoal). Sem essa chave, um APK novo não instala por cima do antigo: cada QRA teria que desinstalar e instalar de novo.

## Avisar para atualizar
`../qap/app-versao.json` → `minima`: quando um APK novo for obrigatório, ponha aqui o número da versão (o número do build no GitHub). O app mais velho mostra "Tem versão nova do app".

## Arquivos
- `capacitor.config.json`: endereço do site, só esse domínio pode abrir dentro do app.
- `android/app/src/main/java/br/com/frontsecurity/qap/MainActivity.java`: bloqueio e avisos de print/gravação.
- `android/app/src/main/java/br/com/frontsecurity/qap/SegurancaPlugin.java`: assinatura dos pedidos e código do aparelho.
- `.github/workflows/apk.yml`: gera e publica o APK.
