# Jogo da Velha com IA — Jetpack Compose

Jogo da Velha (Tic-Tac-Toe) para Android, feito com **Kotlin** e **Jetpack Compose**. Você pode jogar contra um oponente controlado por **Inteligência Artificial**, com três níveis de dificuldade, ou no modo clássico de **dois jogadores locais**.

## Funcionalidades

- **Tabuleiro 3x3** feito com composables (`Row`/`Column` de `Cell`s clicáveis).
- **Painel de status** que mostra de quem é a vez, avisa quando a IA está pensando (com indicador de progresso) e mostra o resultado (vitória/empate).
- **Validação completa**: vitória em linhas, colunas e diagonais e empate ("deu velha") são verificados após cada jogada. A linha vencedora fica destacada.
- **Células ocupadas ficam bloqueadas**, e o tabuleiro inteiro é desativado quando a partida termina e enquanto a IA está jogando.
- **Botão "Reiniciar partida"**: limpa o tabuleiro e mantém o placar. Quem começa alterna a cada rodada, então a IA também abre partidas.
- **Desafio 1: níveis de dificuldade da IA** — Fácil, Médio e Difícil.
- **Desafio 2: placar e seletor de modo**:
  - O placar de vitórias do jogador, vitórias da IA e empates é mantido durante toda a sessão. Ele fica guardado em um `ViewModel`, então continua mesmo se a tela girar.
  - Cada modo (Jogador vs IA e Jogador vs Jogador) tem seu próprio placar.
  - Um seletor permite trocar entre **Jogador vs IA** e **Jogador vs Jogador**.
  - O botão "Zerar placar" zera o placar do modo atual.

## Estratégia da Inteligência Artificial

A IA fica em [`TicTacToeAI.kt`](app/src/main/java/com/example/jvwithia/game/TicTacToeAI.kt). O jogador humano é sempre **X** e a IA é sempre **O**.

### Fácil — jogadas aleatórias
A IA escolhe uma célula livre qualquer, de forma aleatória. Ela não segue nenhuma estratégia e é fácil de vencer.

### Médio — heurística baseada em regras
A IA segue estas regras, nesta ordem de prioridade:
1. **Vencer**: se ela consegue completar uma linha nesta jogada, ela joga ali.
2. **Bloquear**: se o adversário pode vencer na próxima jogada, ela ocupa essa célula.
3. **Erro proposital**: em 20% das vezes ela faz uma jogada aleatória. Isso deixa o nível com cara de "médio" e possível de vencer.
4. **Posição**: se nada acima se aplica, ela escolhe o **centro**, depois um **canto** e por último uma **lateral**.

### Difícil — Minimax com poda Alfa-Beta
A IA usa o algoritmo **Minimax**, que testa todas as continuações possíveis da partida a partir do tabuleiro atual:
- A IA é o jogador **maximizador** e o humano é o **minimizador**.
- Cada estado final recebe uma pontuação:
  - `+10 − profundidade` se a IA vence. Vencer em menos jogadas vale mais.
  - `profundidade − 10` se a IA perde. Perder mais tarde é "menos ruim".
  - `0` em caso de empate.
- A **poda Alfa-Beta** ignora ramos que não podem mudar a decisão final, o que reduz bastante o número de estados avaliados.
- Quando várias jogadas têm a mesma pontuação ótima, a IA **sorteia** uma delas. Assim as partidas não ficam sempre iguais e a IA continua jogando de forma perfeita.

O Jogo da Velha é um jogo pequeno: tem no máximo 9! sequências de jogadas. Por isso a busca completa termina instantaneamente. Com isso, **a IA difícil nunca perde**: o melhor resultado possível contra ela é o empate. O teste `TicTacToeAITest.hardNeverLoses` prova isso jogando **todas** as sequências possíveis de jogadas humanas contra a IA, tanto com a IA começando quanto com o humano começando.

A jogada da IA roda em uma coroutine (`viewModelScope`) com um atraso de 500 ms. Assim a jogada fica visível para o usuário e a interface não trava.

## Estrutura do projeto

```
app/src/main/java/com/example/jvwithia/
├── MainActivity.kt            # Ponto de entrada; obtém o ViewModel
├── game/
│   ├── GameLogic.kt           # Regras: tabuleiro, linhas vencedoras, vitória/empate
│   ├── TicTacToeAI.kt         # IA: Fácil (aleatória), Médio (heurística), Difícil (Minimax)
│   └── GameViewModel.kt       # Estado da partida, turnos, placar por modo, disparo da IA
└── ui/
    ├── TicTacToeScreen.kt     # Composables: seletores, placar, status, tabuleiro, botões
    └── theme/                 # Tema Material 3
app/src/test/java/com/example/jvwithia/
└── GameTests.kt               # Testes unitários da lógica e da IA
```

## Como compilar e executar

### Requisitos
- **Android Studio** recente, com suporte ao Android Gradle Plugin 9.x
- **Android SDK Platform 37** (compileSdk 37). Ele pode ser instalado pelo SDK Manager.
- JDK 17 ou superior. O JDK que vem com o Android Studio já serve.
- Um emulador ou um aparelho com **Android 7.0 (API 24)** ou superior

### Pelo Android Studio
1. Clone o repositório:
   ```bash
   git clone <URL-deste-repositório>
   ```
2. No Android Studio, use **File → Open** e selecione a pasta do projeto.
3. Espere o Gradle sincronizar.
4. Escolha um emulador ou um aparelho conectado e clique em **Run ▶** (`Shift+F10`).

### Pela linha de comando
```bash
# Windows
gradlew.bat assembleDebug
# Linux/macOS
./gradlew assembleDebug
```
O APK é gerado em `app/build/outputs/apk/debug/app-debug.apk`. Para instalar em um aparelho conectado:
```bash
./gradlew installDebug
```

### Executar os testes
```bash
./gradlew testDebugUnitTest
```

## Como jogar
1. Escolha o modo no topo: **Jogador vs IA** ou **Jogador vs Jogador**.
2. No modo contra a IA, escolha a dificuldade: **Fácil**, **Médio** ou **Difícil**. Se você mudar o modo ou a dificuldade, o tabuleiro é reiniciado.
3. Toque em uma célula vazia para jogar. O painel de status mostra de quem é a vez.
4. Quando a partida terminar, toque em **Reiniciar partida** para jogar de novo. O placar continua guardado.
