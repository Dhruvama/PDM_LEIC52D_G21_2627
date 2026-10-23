# DroidMentor — Plano de Projeto

**Grupo:** Dhruva Adão · João Sousa · Paulo Vaz
**Período:** 19/10/2026 → 12/12/2026

As tarefas de cada semana usam as matérias dadas nas aulas até essa semana, seguindo o planeamento da cadeira.

## Marcos

| Marco | Data | Entrega |
|---|---|---|
| Milestone 3 | 16/11/2026 | Vídeo (5–7 min) + tag `mentor_3` |
| Final | 12/12/2026 | App completa + vídeo (5–7 min) + tag `mentor_f` |
| Discussões | a partir de 14/12/2026 | — |

## Responsabilidades gerais

| Membro | Ecrãs | Camada de dados / infraestrutura |
|---|---|---|
| Dhruva Adão | About, Chat History, Tema | Room (conversas e mensagens) |
| João Sousa | Active Chat | Cliente Gemini (Ktor + Kotlinx Serialization) |
| Paulo Vaz | Title, Settings, Navegação | `Application` (service locator), DataStore, conectividade |

Cada membro trata das animações dos seus ecrãs.

---

## Fase 1 — Visuais (Semana 7: 19/10 → 25/10)
*Matéria aplicada: Jetpack Compose, Material 3, recursos (strings PT/EN), orientações.*

Só UI, com dados *hardcoded*. Não há funcionalidades.

| Dhruva Adão | João Sousa | Paulo Vaz |
|---|---|---|
| Tema: cores, tipografia, dark/light, componentes base (botões, inputs, cards). **Primeira tarefa, porque todos dependem dele.** | Active Chat: balões user/model, campo de input, botão enviar | Title (menu principal) |
| About (app + autores) | Estados visuais: a carregar, erro, offline | Settings: input da API key, mostrar/ocultar |
| Chat History: lista, item, estado vazio, diálogo de apagar | Modo de edição de mensagem | Navegação entre ecrãs (Figura 1 do enunciado) |
| Animações dos seus ecrãs | Ajuda pontual nos ecrãs dos colegas + animações | Animações dos seus ecrãs |

## Fase 2 — ViewModels (Semana 8: 26/10 → 01/11)
*Matéria aplicada: ViewModel, StateFlow, `viewModelScope`, `collectAsStateWithLifecycle`, `Application` como service locator.*

Cada ecrã passa a ser uma máquina de estados num ViewModel. Os ViewModels usam repositórios *fake* com contratos `suspend` realistas.

| Dhruva Adão | João Sousa | Paulo Vaz |
|---|---|---|
| `HistoryViewModel`: listar e apagar conversas | `ChatViewModel`: estados Idle, Loading, Error e Offline; enviar mensagem | `TitleViewModel` e `SettingsViewModel` (guardar e validar a key) |
| Interfaces dos repositórios (contratos partilhados por todos) | Reescrita: editar a mensagem *n* invalida as mensagens seguintes | `DroidMentorApplication` como service locator, com os *fakes* |
| Implementações *fake* em memória | Testes unitários do `ChatViewModel` | Verificar que o estado sobrevive a rotação e a *config changes* |

## Fase 3 — Camada de dados (Semanas 9–10: 02/11 → 15/11)
*Matéria aplicada: Ktor, Kotlinx Serialization, DTOs, modelo de falhas, DataStore, Room.*

| Dhruva Adão — Room | João Sousa — Gemini API | Paulo Vaz — Infraestrutura |
|---|---|---|
| Entidades `Conversation` e `Message`, DAOs | Cliente Ktor, DTOs e conversão DTO ↔ domínio | DataStore: API key e id do último chat ativo |
| Apagar conversas em cascata | Pedido stateless: histórico completo em `contents`, alternando `user`/`model` | Verificação de conectividade antes de cada pedido |
| Query que apaga as mensagens após a posição *n* (reescrita) | `system_instruction` com a persona de mentor Android | Trocar os *fakes* pelas implementações reais no service locator |
| `ChatRepository` sobre Room | Erros HTTP (400/403 key inválida, 429, 500), timeouts, logging | Lógica de arranque: abrir o último chat ativo ou o Title |

### Milestone 3 — 16/11/2026
Cada membro grava 1–2 minutos sobre a sua parte. Paulo Vaz junta e edita o vídeo, atualiza o README e cria a tag `mentor_3`.

## Fase 4 — Integração + Imagens (Semanas 11–12: 16/11 → 29/11)

| Dhruva Adão | João Sousa | Paulo Vaz |
|---|---|---|
| Ligar Chat History ao Room | Fluxo completo de envio no Active Chat (Room + API) | Fechar ou sair do chat limpa o "último chat ativo" |
| Reescrita ponta a ponta (BD + UI) | Feedback de erros na UI | Banner offline; envio bloqueado sem rede (histórico disponível offline) |
| **Imagens (opcional):** guardar os ficheiros no armazenamento local e só o URI/caminho no Room | **Imagens (opcional):** enviar a imagem no pedido ao Gemini (`inline_data`) | **Imagens (opcional):** galeria e câmara (permissões, FileProvider) |

## Fase 5 — Testes e entrega (Semanas 13–14: 30/11 → 12/12)

| Dhruva Adão | João Sousa | Paulo Vaz |
|---|---|---|
| Testes instrumentados dos DAOs | Testes do cliente com Ktor `MockEngine` (sucesso, 429, 500) | Testes de navegação e de UI (Compose) |
| README final | Edição do vídeo final | Testes manuais: offline, rotação, morte do processo |
| — | — | Tag `mentor_f` |

**08/12 → 11/12:** todos corrigem bugs. O dia 12/12 fica só como margem.

---

## Equilíbrio de carga

| Fase | Dhruva Adão | João Sousa | Paulo Vaz |
|---|---|---|---|
| 1 | Tema + 2 ecrãs | 1 ecrã complexo + apoio | 2 ecrãs + navegação |
| 2 | 1 VM + contratos + *fakes* | 1 VM complexo + testes | 2 VMs + service locator |
| 3 | Room | Ktor / Gemini | DataStore + rede + arranque + vídeo M3 |
| 4 | Reescrita + imagens (storage) | Fluxo de envio + imagens (API) | Offline + imagens (galeria/câmara) |
| 5 | Testes BD + README | Testes rede + vídeo | Testes UI + tag |

O Active Chat é o ecrã mais complexo e fica com João Sousa em todas as fases. Para compensar, as tarefas de entrega (vídeo do M3, tags, README) ficam com Dhruva Adão e Paulo Vaz.
