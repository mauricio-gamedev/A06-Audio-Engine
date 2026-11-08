# A06 Audio Engine

Aplicativo Android de processamento de áudio orientado ao alto-falante do Samsung Galaxy A06 (SM-A065M), sem root.

## Primeira versão técnica

- Player local usando Android Storage Access Framework + Media3 1.11.1.
- Cadeia de processamento PCM própria: EQ paramétrico de cinco bandas (biquads RBJ), compressor suave por envelope e limitador de pico.
- Seis perfis: Natural, Voice Clarity, Balanced, Bass Control, Dynamic e Gaming / Cinema.
- Controle de intensidade e bypass do EQ/compressor (o limitador de segurança permanece ativo).
- Diagnóstico de saídas e efeitos anunciados pelo Android.
- Atalho para as configurações de som do sistema.

## Limite importante do Android sem root

O processamento do A06 Audio Engine atua **somente no áudio reproduzido dentro do player do próprio aplicativo**. Um app comum não pode inserir seu DSP no áudio do YouTube, Roblox, jogos ou outros aplicativos. O atalho de configurações abre os controles de som fornecidos pelo Android/One UI; não altera configurações ocultas nem garante que opções específicas existam no firmware.

## Segurança sonora

- Ganhos de EQ intencionalmente moderados.
- Sem ganho automático de volume acima de 1x.
- Limitador de pico com teto de 0,94 (-0,54 dBFS).
- Subgrave não é forçado: o alto-falante pequeno do A06 tem limites físicos.

## Compatibilidade e build

- Galaxy A06 SM-A065M · Android 16 / API 36 (alvo de validação).
- minSdk 28, targetSdk 36.
- Kotlin 2.3.20 · AGP 8.13.2 · Gradle 8.13.
- Media3 1.11.1.
- Pacote: `io.github.astromg01.a06audioengine`.

O workflow GitHub Actions roda testes unitários, lint e gera o APK debug. A primeira compilação e os testes físicos no SM-A065M ainda precisam ser confirmados pelo CI e pelo aparelho.

## Instruções

1. Abra o app e selecione um arquivo de áudio local.
2. Direcione a saída para o alto-falante do celular (desconecte fones/Bluetooth).
3. Escolha um perfil e ajuste a intensidade.
4. Compare os perfis no mesmo trecho e em volume semelhante.
5. Use o diagnóstico para verificar as saídas/efeitos que o firmware anuncia.

Consulte `docs/ARCHITECTURE.md` e `docs/TESTING_ON_DEVICE.md`.
