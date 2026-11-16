# DroidMentor — Plano de Projeto (v3)

**Grupo:** Dhruva Adão · João Sousa · Paulo Vaz
**Período:** 19/10/2026 → 12/12/2026

## Princípio da divisão

Cada membro é dono de uma
**funcionalidade de ponta a ponta** (ecrã → ViewModel → repositório → Room / Ktor / DataStore), em vez de ficar com uma camada inteira.
Assim todos trabalham com quase todas as tecnologias da cadeira, e as dependências entre membros reduzem-se a contratos (interfaces) bem definidos. Ninguém edita o ecrã, o ViewModel ou a implementação de outro membro. Quem precisa de algo de outra vertical usa o contrato ou o componente que esse membro publica.

**Quem implementa um contrato também o define.** Quem o usa diz o que precisa e aprova as assinaturas antes do fecho dos contratos.

**Única exceção:** cada membro acrescenta a linha do seu repositório ou serviço ao `DependenciesContainer` e à `DroidMentorApplication`.

**Entregas** (vídeos, README e tags `mentor_3` / `mentor_f`) são feitas por todos e não contam para a carga.

| Membro | Funcionalidade (vertical) | Contratos de que é dono                                                                                                                   |
|---|---|-------------------------------------------------------------------------------------------------------------------------------------------|
| Dhruva Adão | Histórico de conversas, persistência das conversas e mensagens (Room), tema, modelo de falhas, imagens | `ConversationRepository`, `MessageRepository`, falhas (`sealed class`), `AppColorsRepository`                                             |
| João Sousa | Conversa ativa: envio, reescrita, cliente Gemini | `LlmProviderService`                                                                                                                      |
| Paulo Vaz | Arranque, sessão, definições, API key, conectividade e comportamento offline, mensagens de erro | `SessionRepository`, `SettingsRepository`, `ApiKeyRepository`, `ApiKeyValidator`, `ConnectivityObserver`, `AppDatabase` / service locator |

## Estrutura e contratos

Segue o padrão do exemplo do professor (GameVault):

- **Contratos:** `core/.../domain/<feature>/`. Interfaces em Kotlin puro, sem Android. Os erros de I/O são traduzidos em exceções ou falhas de domínio.
- **Implementações:** `app/.../data/datastore`, `data/room`, `data/remote`. Recebem as dependências (`DataStore`, DAO, `HttpClient`) no construtor e nunca as criam.
- **Fakes:** `app/.../data/fakes/`. Em memória, com `delay()` para simular latência. São também usados nos testes dos ViewModels.
- **Service locator:** `DependenciesContainer` (interface) implementado pela `DroidMentorApplication`. Os ViewModels recebem as interfaces pelo construtor, e a factory recebe o `DependenciesContainer`.
- Os placeholders `core/.../repo`, `core/.../service`, `domain/test.kt`, `testdatastorefile.kt` e `testroomfile.kt` são removidos.

| Contrato | Pacote (`core`) | Dono | Usado por |
|---|---|---|---|
| `ConversationRepository` | `domain/history` | Dhruva | Chat History, Active Chat |
| `MessageRepository` | `domain/chat` | Dhruva | Active Chat |
| Falhas (`sealed class`) | `domain/chat` | Dhruva | `LlmProviderService`, `ApiKeyValidator`, mapeador falha → mensagem |
| `LlmProviderService` | `domain/chat` | João | Active Chat |
| `SessionRepository` (último chat ativo) | `domain/session` | Paulo | Arranque, navegação, Active Chat |
| `SettingsRepository` | `domain/settings` | Paulo | Settings, Active Chat (persona e modelo) |
| `ApiKeyRepository` | `domain/settings` | Paulo | Settings, `LlmProviderService` |
| `ApiKeyValidator` | `domain/settings` | Paulo | Settings |
| `ConnectivityObserver` | `domain/connectivity` | Paulo | Active Chat, `LlmProviderService` |
| `AppColorsRepository` | `app/.../ui/theme` (usa `Color` do Compose) | Dhruva | Tema, Settings |

| Ficheiro DataStore | Dono | Conteúdo |
|---|---|---|
| `theme` | Dhruva | Cores escolhidas (o atual ficheiro `settings` das cores muda de nome) |
| `session` | Paulo | Id do último chat ativo |
| `settings` | Paulo | Definições da app (nível do mentor, modelo, …) |
| `credentials` | Paulo | API key, **excluída** em `backup_rules.xml` e `data_extraction_rules.xml` |

## Quem toca em quê

| Tecnologia | Dhruva Adão | João Sousa | Paulo Vaz |
|---|---|---|---|
| Compose / UI | About, Chat History, Tema, galeria e câmara | Active Chat (incluindo modo de edição) | Title, Settings, Navegação, `OfflineBanner`, `ErrorBanner`, componente offline do Active Chat |
| ViewModel | `ChatHistoryScreenViewModel` | `ActiveChatScreenViewModel` | `TitleViewModel`, `SettingsViewModel` |
| Room | Entidades `Conversation` e `Message` (FK com cascata), DAOs, pesquisa, query de reescrita; coluna de imagem (URI) em `Message` e respetiva migração | — (usa o `MessageRepository` através da interface) | `AppDatabase`, `TypeConverters`, query de arranque |
| Ktor + Serialization | Modelo de falhas (mapeamento de erros HTTP, timeouts, logging); `system_instruction` com a persona; imagens no pedido (`inline_data`); todos os testes com `MockEngine` | Cliente base, DTOs, conversão DTO ↔ domínio, `contents`, `rewriteFrom` | `ApiKeyValidator` (`GET models`) |
| DataStore | `theme` | — | `settings` + `credentials` + `session` |
| Outros | — | — | `ConnectivityObserver`, mapeador falha → mensagem PT/EN |

---

## Fase 1 — Visuais (Semana 7: 19/10 → 25/10)

*Matéria aplicada: Jetpack Compose, Material 3, recursos (strings PT/EN), orientações.*

| Dhruva Adão | João Sousa | Paulo Vaz |
|---|---|---|
| Tema custom e componentes base | Active Chat | Title (menu principal) |
| About | Estados visuais: a carregar, erro, offline | Settings: API key, definições, customização |
| Chat History: lista, pesquisa, apagar | Modo de edição de mensagem | Navegação entre ecrãs |

## Fase 2 — ViewModels (Semana 8: 26/10 → 01/11)
*Matéria aplicada: ViewModel, `viewModelScope`, `collectAsStateWithLifecycle`, `Application` como service locator.*

Cada membro define a **interface** e o **fake em memória** de cada contrato de que é dono, com contratos `suspend`/`Flow` realistas. Quem usa o contrato aprova as assinaturas. Os contratos devem estar no `core` até 07/11, para ninguém ficar bloqueado. Os contratos e os fakes são feitos antes dos ViewModels, porque com os fakes os outros conseguem avançar sem esperar pelas implementações reais.

| Dhruva Adão | João Sousa | Paulo Vaz |
|---|---|---|
| Interface + fake de `ConversationRepository` (listar, pesquisar, criar, apagar) | Interface + fake de `LlmProviderService` (latência, respostas e falhas simuladas) | Interface + fake de `SettingsRepository` e de `ApiKeyRepository` |
| Interface + fake de `MessageRepository` (incluindo apagar depois da posição *n*), com as assinaturas aprovadas pelo João + `sealed class` de falhas (incluindo `Offline`) | — | Interface + fake de `ApiKeyValidator`, de `ConnectivityObserver` e de `SessionRepository` (último chat ativo) |
| `ChatHistoryScreenViewModel`: listar, pesquisar e apagar conversas | `ActiveChatScreenViewModel`: estados Idle, Waiting, Failed e Offline; enviar mensagem | `TitleViewModel` e `SettingsViewModel` (guardar e validar a key) |
| `AppColorsRepository` passa a interface + implementação DataStore (a persistência já existe) | Reescrita no ViewModel: editar a mensagem *n* invalida as seguintes | `DependenciesContainer` + `DroidMentorApplication` com os fakes de todos |
| Testes unitários do `ChatHistoryScreenViewModel` | Testes unitários do `ActiveChatScreenViewModel` | Testes unitários dos seus ViewModels + verificar rotação e *config changes* |

## Fase 3 — Camada de dados (Semanas 9–10: 02/11 → 15/11)

*Matéria aplicada: Ktor, Kotlinx Serialization, DTOs, modelo de falhas, DataStore, Room.*

O Room fica com o Dhruva e o Paulo. Todos fazem uma parte de Ktor. O DataStore fica com o Dhruva e o Paulo.

| | Dhruva Adão | João Sousa | Paulo Vaz |
|---|---|---|---|
| **Room** | Entidades `Conversation` e `Message` (FK para `Conversation` com `onDelete = CASCADE`) + DAOs; query de pesquisa; query que apaga as mensagens após a posição *n*; `ConversationRepository` e `MessageRepository` reais | — | `AppDatabase`, `TypeConverters`, criação da BD no service locator; query "existe a conversa X?" para o arranque |
| **Ktor** | Modelo de falhas: mapeamento de 400/403 (key inválida), 429, 500 e timeouts, e logging. Publicado como função `(HttpResponse` ou `Throwable) → falha`, usada pelo cliente do João e pelo validador do Paulo. Texto da `system_instruction` com a persona, gerado a partir das definições do mentor (função publicada, usada pelo cliente do João) | Cliente Ktor, DTOs, conversão DTO ↔ domínio; pedido stateless com o histórico completo em `contents` (`user`/`model`) e a `system_instruction` do Dhruva; `LlmProviderService` real | `ApiKeyValidator` real (`GET models`), que reutiliza o `HttpClient` e o mapeamento de falhas |
| **DataStore** | Ficheiro `theme` (mudar o nome do atual) | — | Ficheiros `settings`, `credentials` e `session`: repositórios reais + exclusão dos backups |
| **Outros** | — | Envio mínimo a funcionar de ponta a ponta (uma mensagem → Room → API → resposta no ecrã) | `ConnectivityObserver` real (`Flow` online/offline); trocar os fakes pelas implementações reais; lógica de arranque (último chat ativo ou Title; se a conversa guardada já não existir, vai para o Title) |

**Ordem dentro da fase:** o João publica o cliente base e os DTOs até 09/11. O João e o Dhruva combinam desde já onde fica a configuração do `HttpTimeout`. O Dhruva publica o mapeamento de falhas até 10/11 e o `MessageRepository` real até 12/11, para o João ter o envio de ponta a ponta antes do milestone. Por isso, o Dhruva faz primeiro o Room das mensagens (entidade `Message`, `MessageDao`, `MessageRepository`) e só depois a pesquisa e o resto do Chat History. O Paulo faz primeiro o DataStore, a `AppDatabase` e o `ConnectivityObserver` (não dependem de ninguém) e só depois o validador.

**Milestone 3:** 16/11/2026.

## Fase 4 — Integração + Imagens (Semanas 11–12: 16/11 → 29/11)

| Dhruva Adão | João Sousa | Paulo Vaz |
|---|---|---|
| Ligar Chat History ao Room (pesquisa e apagar reais) | Fluxo completo de envio no Active Chat (Room + API) | Mapeador falha → mensagem PT/EN, usado pelo Active Chat e pelas Settings |
| Criar a conversa na BD ao enviar a primeira mensagem: `ConversationRepository.create` com o título tirado da 1.ª mensagem (o João só a chama) | Reescrita de ponta a ponta: `rewriteFrom(conversationId, posicao, novoTexto)` usa o `MessageRepository` para apagar as mensagens seguintes e guardar a editada, e reutiliza o envio normal; o modo de edição chama-a | Componentes `OfflineBanner` e `ErrorBanner` |
| Tema aplicado a partir do DataStore em toda a app | Mostrar os erros no Active Chat com o mapeador do Paulo; inserir no ecrã o componente offline do Paulo | Comportamento offline do Active Chat: componente publicado que mostra o banner e indica se o envio está bloqueado (o histórico continua disponível offline). O João só o insere no ecrã |
| **Imagens:** galeria e câmara (permissões, FileProvider); ficheiros no armazenamento local e só o URI no Room (coluna nova em `Message`); envio no pedido ao Gemini (`inline_data`, DTO + base64) | — | Limpar o "último chat ativo" na navegação ao sair do Active Chat; feedback da validação da key nas Settings |

**Dependências dentro da fase:** o Paulo publica o mapeador de erros, os banners e o componente offline até 19/11.
## Fase 5 — Testes e entrega (Semanas 13–14: 30/11 → 12/12)

Cada membro testa sobretudo o que construiu. Há duas exceções, para equilibrar a carga:
- Os testes com `MockEngine` ficam todos com o Dhruva, que já monta o `MockEngine` para os erros.
- Os testes de UI do Chat History e do estado offline do Active Chat ficam com o Paulo.

Testar o código de outro membro também ajuda a encontrar falhas que o autor não veria.

| Dhruva Adão | João Sousa | Paulo Vaz |
|---|---|---|
| Testes instrumentados do `ConversationDao` (pesquisa e cascata) e do `MessageDao` (reescrita) | Testes de UI (Compose) do Active Chat, incluindo o modo de edição | Testes do DataStore (`settings`, `credentials`, `session`) e da lógica de arranque |
| Testes Ktor com `MockEngine`: sucesso, serialização do pedido, 400/403, 429, 500, timeout | Testes de `rewriteFrom` | Teste Ktor da validação da key; testes do mapeador e do `ConnectivityObserver` |
| — | — | Testes de navegação e de UI das Settings, do Chat History e do estado offline do Active Chat |
| **Imagens:** testes do pedido com `inline_data` e da coluna de URI | — | Checklist dos testes manuais (offline, rotação, morte do processo) |

**Testes manuais (todos, cada um no seu dispositivo ou emulador):** seguindo o checklist do Paulo.

**08/12 → 11/12:** correção de bugs. O dia 12/12 fica só como margem.

---

Este plano é um ponto de partida. Se a carga ficar desproporcional, o grupo faz ajustes no fim de cada fase. As interfaces são o contrato entre membros: qualquer alteração a uma interface é combinada com o grupo antes de ser feita.
