# RADAR

Organizador acadêmico para Android desenvolvido para a disciplina Desenvolvimento para Dispositivos Móveis da UDESC. Centraliza disciplinas, tarefas, trabalhos e provas e ajuda a acompanhar prazos.

## O que o app oferece

- Painel com total de atividades, pendências, atrasos, concluídas e entregas dos próximos sete dias.
- Cadastro, edição e exclusão de disciplinas. A exclusão confirmada também remove suas atividades e fotos.
- Cadastro, edição, exclusão, conclusão e reabertura de tarefas. A lista geral permite filtrar por situação e disciplina.
- Foto opcional por tarefa, com visualização, substituição e remoção. O app abre a câmera do sistema e recebe a foto via `FileProvider`. A imagem fica no armazenamento privado do RADAR.
- Consulta de feriados nacionais por ano via Retrofit e [BrasilAPI](https://brasilapi.com.br/docs#tag/Feriados-Nacionais). A tela mostra carregamento, resultado, lista vazia ou erro com opção de tentar novamente. Disciplinas, tarefas e fotos funcionam offline.
- Atualização do Painel por movimento. Em Ajustes, a opção **Agitar para atualizar o Painel** vem habilitada. O app usa `SensorManager` e `TYPE_ACCELEROMETER` apenas quando o Painel está em primeiro plano. Dois picos de movimento disparam uma nova leitura e exibem **Painel atualizado!**. O intervalo de dois segundos evita disparos seguidos. Em aparelhos sem acelerômetro, Ajustes desabilita a opção. O Room continua atualizando os dados normalmente.

## Abrir no Android Studio

1. Abra a pasta `RADAR`, que contém `settings.gradle.kts`.
2. Instale o SDK Android 35 pelo SDK Manager. Use o JDK 17 ou o JDK compatível fornecido pelo Android Studio como Gradle JDK.
3. Aguarde o Gradle Sync e execute a configuração `app` em um emulador ou aparelho Android 8.0 (API 26) ou superior.
4. Para demonstrar a câmera, use um aparelho com um app de câmera ou habilite a câmera virtual do emulador. Para a BrasilAPI, conecte o aparelho ou emulador à internet. Câmera e acelerômetro dispensam serviços externos.

O projeto inclui o Gradle Wrapper 8.11.1, AGP 8.9.2 e Kotlin 2.1.20 com KSP correspondente.

## Arquitetura e pastas

| Pasta | Responsabilidade |
| --- | --- |
| `app/src/main/java/br/udesc/radar/ui` | Fragments, ViewModels e ListAdapters. As telas observam os estados durante o ciclo de vida da interface. |
| `app/src/main/java/br/udesc/radar/data/local` | Entidades, DAOs e banco Room. Cada atividade pertence a uma disciplina. |
| `app/src/main/java/br/udesc/radar/data/repository` | Operações de cadastro e consulta, transações e gestão dos anexos. |
| `app/src/main/java/br/udesc/radar/data/remote` | Serviço Retrofit e repositório da BrasilAPI. |
| `app/src/main/java/br/udesc/radar/util` | Cálculo de prazos e detecção do gesto de agitar. |
| `app/src/main/res` | XML das telas, componentes visuais, menu inferior e grafo de navegação. |
| `app/src/androidTest` | Testes instrumentados de Room e do reconhecimento do gesto. |

Os Fragments cuidam da interface e da navegação. Os ViewModels reagem às ações do usuário e expõem estados observáveis. Room fornece os dados locais por `Flow`, e o repositório remoto converte as respostas da BrasilAPI para a tela de feriados.

## Verificações e demonstração

No Windows, execute na raiz do RADAR:

```powershell
.\gradlew.bat :app:assembleDebug :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

No macOS ou Linux, use `./gradlew`. O segundo comando requer um aparelho ou emulador conectado. Veja o [roteiro de apresentação e demonstração](APRESENTACAO.md) para percorrer as nove telas e os requisitos do T1.
