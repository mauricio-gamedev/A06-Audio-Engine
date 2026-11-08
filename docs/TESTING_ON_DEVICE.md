# Teste físico — Galaxy A06 SM-A065M

## Preparação

- Instale o APK debug gerado pelo workflow.
- Desconecte fones P2, USB e Bluetooth para testar o alto-falante interno.
- Comece com volume físico entre 40% e 60%.
- Use o mesmo trecho de áudio para comparar perfis.

## Matriz manual

| Teste | Procedimento | Resultado esperado |
|---|---|---|
| Seleção de arquivo | Abrir MP3/M4A/WAV pelo seletor do Android | Arquivo carrega sem permissão ampla de armazenamento |
| Reprodução | Reproduzir e pausar | Áudio sai pelo alto-falante padrão |
| Voice Clarity | Trecho com fala e ruído de fundo | Voz ligeiramente mais presente, sem aspereza |
| Balanced | Música/voz variada | Timbre equilibrado, sem ganho brusco |
| Bass Control | Música com baixo | Grave moderado, sem vibração ou distorção excessiva |
| Dynamic | Áudio com grandes diferenças de volume | Picos suavizados sem bombeamento evidente |
| DSP bypass | Desativar DSP | EQ e compressão deixam de atuar; limitador de segurança permanece |
| Troca de perfil | Trocar durante reprodução | Novo perfil aplicado sem reiniciar o player |
| Configurações | Abrir configurações de som | Tela de som disponibilizada pelo firmware abre ou mostra fallback |
| Diagnóstico | Atualizar diagnóstico | Modelo, versão, saídas e catálogo de efeitos são exibidos |

## Critérios de aprovação

- Nenhum crash ou ANR.
- Reprodução estável por pelo menos 10 minutos.
- Nenhum estalo persistente ao trocar perfil.
- Nenhum clipping audível nos perfis em volume moderado.
- Voice Clarity melhora a inteligibilidade sem sibilância incômoda.
- Registrar resultados específicos do alto-falante do SM-A065M.

A análise do aparelho real é indispensável: o DSP não consegue superar os limites físicos de frequência, excursão e potência do alto-falante.
