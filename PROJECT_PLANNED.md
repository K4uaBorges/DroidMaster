# DroidMaster — Project Plan

## 1. Visão Geral do Projeto

O **DroidMaster** é uma aplicação Android que integra Inteligência Artificial através da API do **Google Gemini**.

O objetivo da aplicação é disponibilizar um **mentor especializado em desenvolvimento Android**, capaz de ajudar o utilizador com dúvidas, problemas e conceitos relacionados com o desenvolvimento de aplicações Android.

O DroidMaster utilizará o Gemini como modelo de Inteligência Artificial, sendo configurado através de uma `system_instruction` para assumir a persona de um mentor especializado em desenvolvimento Android.

A aplicação será responsável por gerir o estado das conversas, armazenar localmente o histórico e construir o contexto necessário para cada interação com o Gemini.

---

# 2. Objetivos do Projeto

Os principais objetivos do DroidMaster são:

- Integrar a API do Google Gemini numa aplicação Android.
- Criar uma experiência de interação com um mentor de desenvolvimento Android.
- Permitir criar e continuar conversas.
- Manter o contexto individual de cada conversa.
- Guardar localmente o histórico das conversas.
- Permitir consultar conversas anteriores sem ligação à Internet.
- Verificar a disponibilidade da rede antes de realizar pedidos à API.
- Informar o utilizador quando uma nova interação com a IA não é possível.
- Identificar e tratar erros de rede e erros HTTP.
- Armazenar de forma segura a API Key do Gemini.
- Separar as responsabilidades da aplicação através de uma arquitetura MVC.
- Desenvolver a aplicação de acordo com os requisitos definidos no enunciado.

---

# 3. Requisitos Técnicos

## 3.21 Comunicação com a API

Toda a comunicação com APIs externas será realizada através do **Ktor Client**.

O Ktor será responsável pela comunicação HTTP com a API REST do Google Gemini.

A aplicação não deverá utilizar outras bibliotecas para realizar diretamente as chamadas à API.

```text
Aplicação Android
       |
       v
   Controller
       |
       v
   API Layer
       |
       v
  Ktor Client
       |
       v
 Gemini REST API
```

## 3.2 Serialização

A comunicação com o Gemini utilizará **Kotlinx Serialization** para:

- Serializar os pedidos enviados à API.
- Desserializar as respostas recebidas.
- Representar os dados trocados através de modelos Kotlin.

```text
Modelo Kotlin
     |
     v
Kotlinx Serialization
     |
     v
JSON
     |
     v
Ktor
     |
     v
Gemini API
```

## 3.3 Gestão de Dependências

A aplicação utilizará a classe `Application` do Android como ponto central para a inicialização e disponibilização das dependências necessárias.

Não serão utilizados frameworks de Dependency Injection como **Dagger ou Hilt**.

A gestão das dependências será realizada manualmente através da configuração da própria aplicação.

```text
Application
    |
    +-- Ktor Client
    |
    +-- Database
    |
    +-- DAOs
    |
    +-- Repositories
    |
    +-- Controllers
```

---

# 4. Persistência Local

A persistência local será implementada utilizando a **Room Persistence Library**.

A Room será responsável por armazenar os dados necessários para manter as conversas e o respetivo histórico.

Os principais dados armazenados serão:

- Conversas.
- Mensagens.
- Relação entre mensagens e conversas.
- Informação necessária para reconstruir o contexto da conversa.

Estrutura conceptual:

```text
Conversation
    |
    +-- Message
    +-- Message
    +-- Message
    +-- Message
```

Cada mensagem deverá estar associada a uma conversa através de um identificador.

---

# 5. Contexto das Conversas

Cada conversa terá o seu próprio contexto.

O DroidMaster não dependerá de uma sessão multi-turn mantida pelo servidor para preservar o contexto.

Em vez disso, a aplicação será responsável por construir o contexto a partir do histórico armazenado localmente.

Quando o utilizador enviar uma nova mensagem, será realizado o seguinte processo:

```text
1. Utilizador envia mensagem
             |
             v
2. Controller recebe mensagem
             |
             v
3. Recuperar histórico da conversa
             |
             v
4. Construir contexto
             |
             v
5. Construir pedido Gemini
             |
             v
6. Enviar através do Ktor
             |
             v
7. Gemini processa pedido
             |
             v
8. Receber resposta
             |
             v
9. Guardar resposta na Room
             |
             v
10. Atualizar interface
```

O histórico deverá manter a ordem das mensagens e distinguir as mensagens do utilizador das mensagens do modelo.

Exemplo:

```text
USER
Como crio uma RecyclerView?

MODEL
Uma RecyclerView pode ser criada...

USER
E como adiciono um click?

MODEL
Para adicionar um click...
```

Ao enviar a segunda pergunta, o pedido ao Gemini deverá incluir o contexto necessário da conversa.

---

# 6. Funcionamento Offline

O DroidMaster seguirá uma abordagem **offline-first**.

Isto significa que as funcionalidades que dependem de dados armazenados localmente continuarão disponíveis sem ligação à Internet.

## Funcionalidades disponíveis offline

O utilizador deverá conseguir:

- Abrir a aplicação.
- Navegar pelas diferentes áreas.
- Consultar o histórico de conversas.
- Abrir conversas anteriormente guardadas.
- Ler mensagens já armazenadas.
- Consultar informação armazenada localmente.

## Funcionalidades que necessitam de Internet

Uma nova interação com o Gemini necessita de ligação à Internet.

Antes de efetuar qualquer pedido à API, a aplicação deverá verificar a disponibilidade da rede.

```text
                 ┌── Internet disponível ──> Gemini API
                 |
Nova mensagem ───┤
                 |
                 └── Sem Internet ─────────> Informar utilizador
```

Quando o dispositivo estiver offline, a aplicação não deverá tentar efetuar um pedido à API.

O utilizador deverá receber uma mensagem clara indicando que a nova interação com o mentor está temporariamente indisponível.

---

# 7. Tratamento de Erros

A aplicação deverá identificar e tratar corretamente erros de rede e respostas HTTP inesperadas.

Alguns exemplos incluem:

| Código | Significado | Comportamento |
|---|---|---|
| 400 | Pedido inválido | Informar o utilizador |
| 401 | Não autorizado | Verificar API Key/configuração |
| 403 | Acesso proibido | Informar o utilizador |
| 404 | Recurso não encontrado | Informar o utilizador |
| 429 | Demasiados pedidos | Informar que o limite foi atingido |
| 500 | Erro interno do servidor | Informar que o serviço está temporariamente indisponível |
| Outros | Erro HTTP | Tratamento genérico adequado |

Os erros não deverão provocar o encerramento inesperado da aplicação.

```text
Ktor
  |
  v
Resposta HTTP
  |
  +---- Sucesso ------> Processar resposta
  |
  +---- Erro ---------> Identificar erro
                            |
                            v
                      Controller
                            |
                            v
                         View
                            |
                            v
                    Informar utilizador
```

---

# 8. API Key e Segurança

A API Key utilizada para comunicar com o Gemini deverá ser armazenada de forma segura através do **DataStore**.

A chave não deverá ser armazenada diretamente no código-fonte da aplicação.

O DataStore será utilizado para guardar a configuração necessária à utilização da API.

```text
Settings
   |
   v
DataStore
   |
   v
Gemini API Key
   |
   v
API Layer
```

---

# 9. Ecrãs da Aplicação

## 9.1 Title

Ecrã principal da aplicação.

A partir deste ecrã o utilizador poderá:

- Aceder ao histórico de conversas.
- Aceder às definições.
- Consultar informação sobre a aplicação.

## 9.2 Chat History

Apresenta a lista das conversas anteriormente guardadas.

O utilizador poderá:

- Consultar conversas existentes.
- Selecionar uma conversa.
- Iniciar uma nova conversa.

Os dados serão obtidos a partir da base de dados local.

## 9.3 Active Chat

Ecrã responsável pela interação com o mentor Gemini.

Deverá permitir:

- Visualizar as mensagens.
- Escrever novas mensagens.
- Enviar perguntas ao mentor.
- Receber respostas do Gemini.
- Continuar uma conversa existente.
- Apresentar mensagens de erro quando necessário.

## 9.4 About

Apresenta informação sobre:

- A aplicação.
- O objetivo do projeto.
- Os autores do projeto.
- Informação relevante sobre o DroidMaster.

## 9.5 Settings

Permite configurar a API Key utilizada pelo Gemini.

As configurações serão armazenadas utilizando DataStore.

---

# 10. Requisitos Funcionais

| ID | Requisito |
|---|---|
| FR01 | O utilizador deve conseguir iniciar uma nova conversa. |
| FR02 | O utilizador deve conseguir enviar mensagens ao mentor. |
| FR03 | O sistema deve receber respostas do Gemini. |
| FR04 | Cada conversa deve possuir um contexto próprio. |
| FR05 | O sistema deve guardar as conversas localmente. |
| FR06 | O utilizador deve conseguir consultar conversas anteriores. |
| FR07 | O histórico deve estar disponível sem Internet. |
| FR08 | O sistema deve verificar a conectividade antes de realizar pedidos à API. |
| FR09 | O sistema deve impedir novos pedidos ao Gemini quando não existe Internet. |
| FR10 | O sistema deve identificar erros HTTP. |
| FR11 | O sistema deve apresentar mensagens adequadas quando ocorre um erro. |
| FR12 | O sistema deve armazenar a API Key através do DataStore. |
| FR13 | O sistema deve utilizar Ktor para comunicação HTTP. |
| FR14 | O sistema deve utilizar Kotlinx Serialization para os dados da API. |
| FR15 | O sistema deve utilizar Room para persistência local. |
| FR16 | O sistema deve permitir consultar informação sobre a aplicação. |

---

# 11. Arquitetura

A aplicação utilizará inicialmente o padrão arquitetural **MVC (Model-View-Controller)**.

A arquitetura terá como objetivo separar as responsabilidades da aplicação e evitar que uma única classe seja responsável pela interface, acesso à base de dados, comunicação com a API e lógica de negócio.

```text
                 ┌─────────────┐
                 │    VIEW     │
                 │             │
                 │ Interface   │
                 └──────┬──────┘
                        |
                        v
                 ┌─────────────┐
                 │ CONTROLLER  │
                 │             │
                 │ Lógica      │
                 │ de negócio  │
                 └──────┬──────┘
                        |
             ┌──────────┴──────────┐
             |                     |
             v                     v
      ┌─────────────┐       ┌─────────────┐
      │  STORAGE    │       │     API     │
      │             │       │             │
      │    Room     │       │    Ktor     │
      └─────────────┘       └──────┬──────┘
                                   |
                                   v
                            ┌─────────────┐
                            │   GEMINI    │
                            │     API     │
                            └─────────────┘
```

---

# 12. Responsabilidades da Arquitetura

## 12.1 Model

O `Model` representa os dados utilizados pela aplicação.

Responsabilidades:

- Representar conversas.
- Representar mensagens.
- Representar informação necessária ao funcionamento da aplicação.
- Definir estruturas de dados utilizadas pelos restantes componentes.

Exemplos:

```text
Conversation
Message
GeminiRequest
GeminiResponse
```

## 12.2 View

A `View` representa a interface apresentada ao utilizador.

Responsabilidades:

- Apresentar os ecrãs.
- Receber ações do utilizador.
- Apresentar mensagens.
- Apresentar conversas.
- Apresentar erros.
- Apresentar estados da aplicação.

A View não deverá ser responsável por comunicar diretamente com o Gemini ou com a base de dados.

## 12.3 Controller

O `Controller` será responsável pelo controlo do fluxo da aplicação e pela lógica de negócio.

Responsabilidades:

- Receber ações provenientes da View.
- Validar ações.
- Gerir o fluxo das conversas.
- Recuperar o histórico.
- Construir o contexto da conversa.
- Solicitar operações de armazenamento.
- Solicitar comunicação com a API.
- Processar resultados.
- Encaminhar estados e resultados para a View.
- Coordenar o tratamento de erros.

O Controller deverá **orquestrar** as operações e evitar concentrar diretamente toda a implementação de acesso à base de dados e comunicação HTTP.

## 12.4 API

A camada `API` será responsável pela comunicação externa.

Responsabilidades:

- Configurar o Ktor Client.
- Construir pedidos HTTP.
- Enviar pedidos para o Gemini.
- Receber respostas.
- Processar respostas HTTP.
- Utilizar Kotlinx Serialization.
- Comunicar erros à camada responsável.

A API não deverá controlar diretamente a interface.

## 12.5 Storage

A camada `Storage` será responsável pela persistência local.

Responsabilidades:

- Configuração da Room.
- Definição das Entities.
- Definição dos DAOs.
- Inserção de mensagens.
- Consulta de mensagens.
- Criação e consulta de conversas.
- Atualização do histórico.

---

# 13. Separação de Responsabilidades

Para evitar que o Controller se torne demasiado complexo, a aplicação poderá utilizar componentes de acesso aos dados dentro das respetivas camadas.

```text
VIEW
  |
  v
CONTROLLER
  |
  +----------------------+
  |                      |
  v                      v
Storage                  API
  |                      |
  v                      v
Room                    Ktor
                          |
                          v
                       Gemini
```

O Controller decide **o que deve acontecer**, enquanto as camadas Storage e API são responsáveis por **como executar as operações específicas**.

---

# 14. Estrutura Inicial das Pastas

A estrutura inicial do projeto será organizada da seguinte forma:

```text
DroidMaster/
│
├── apps/
│   └── MainActivity.kt
│
├── api/
│   ├── GeminiApi.kt
│   ├── GeminiClient.kt
│   └── dto/
│       ├── GeminiRequest.kt
│       └── GeminiResponse.kt
│
├── model/
│   ├── Conversation.kt
│   ├── Message.kt
│   └── ...
│
├── view/
│   ├── Title/
│   ├── ChatHistory/
│   ├── ActiveChat/
│   ├── Settings/
│   └── About/
│
├── controller/
│   ├── ChatController.kt
│   ├── ConversationController.kt
│   └── ...
│
├── storage/
│   ├── AppDatabase.kt
│   ├── dao/
│   │   ├── ConversationDao.kt
│   │   └── MessageDao.kt
│   │
│   └── entity/
│       ├── ConversationEntity.kt
│       └── MessageEntity.kt
│
├── README.md
└── PROJECT_PLAN.md
```

> A estrutura final das pastas será validada e ajustada após a definição dos diagramas finais da arquitetura e do fluxo MVC.

---

# 15. Fluxo Geral dos Dados

```text
┌─────────────┐
│    USER     │
└──────┬──────┘
       |
       v
┌─────────────┐
│    VIEW     │
└──────┬──────┘
       |
       v
┌─────────────┐
│ CONTROLLER  │
└──────┬──────┘
       |
       +----------------------+
       |                      |
       v                      v
┌─────────────┐        ┌─────────────┐
│   STORAGE   │        │     API     │
│             │        │             │
│    Room     │        │    Ktor     │
└─────────────┘        └──────┬──────┘
                              |
                              v
                       ┌─────────────┐
                       │   GEMINI    │
                       └──────┬──────┘
                              |
                              v
                       ┌─────────────┐
                       │     API     │
                       └──────┬──────┘
                              |
                              v
                       ┌─────────────┐
                       │ CONTROLLER  │
                       └──────┬──────┘
                              |
                              v
                       ┌─────────────┐
                       │    VIEW     │
                       └─────────────┘
```

---

# 16. Fluxo de uma Nova Mensagem

Quando o utilizador envia uma mensagem, o processo será:

1. O utilizador escreve uma pergunta no `Active Chat`.
2. A View envia a ação para o Controller.
3. O Controller identifica a conversa atual e obtém o respetivo histórico.
4. A camada Storage consulta a Room.
5. O Controller constrói o contexto da conversa utilizando o histórico.
6. A aplicação verifica se existe conectividade.
7. Se existir Internet, o pedido é enviado através do Ktor.
8. O Gemini processa o pedido utilizando a persona definida na `system_instruction`.
9. A resposta é recebida pela camada API.
10. A resposta é desserializada através do Kotlinx Serialization.
11. A resposta é armazenada na Room.
12. A interface é atualizada para apresentar a nova mensagem.

Fluxo resumido:

```text
User
 ↓
View
 ↓
Controller
 ↓
Room
 ↓
Conversation Context
 ↓
Network Check
 ↓
Ktor
 ↓
Gemini
 ↓
Ktor
 ↓
Controller
 ↓
Room
 ↓
View
```

---

# 17. Modelo de Dados

A estrutura de dados inicial será baseada em conversas e mensagens.

## Conversation

```text
Conversation
├── id
├── title
└── createdAt
```

## Message

```text
Message
├── id
├── conversationId
├── role
├── content
└── timestamp
```

A relação será:

```text
Conversation 1 ─────────── N Message
```

Uma conversa poderá conter várias mensagens e cada mensagem pertencerá a uma única conversa.

---

# 18. Requisitos de Desenvolvimento

Durante o desenvolvimento deverão ser respeitadas as seguintes regras:

- Utilizar Kotlin.
- Utilizar Ktor para comunicação HTTP.
- Utilizar Kotlinx Serialization.
- Utilizar Room Persistence Library.
- Utilizar DataStore para armazenamento da API Key.
- Não utilizar Dagger ou Hilt.
- Utilizar a classe `Application` para a configuração das dependências.
- Manter o contexto das conversas no lado do cliente.
- Não depender de sessões multi-turn mantidas pelo servidor.
- Permitir acesso offline ao histórico.
- Verificar a conectividade antes de pedidos ao Gemini.
- Tratar erros HTTP e de rede.
- Manter separação clara entre View, Controller, Model, API e Storage.

---

# 19. Dependências entre Tarefas

Algumas tarefas dependem da conclusão de tarefas anteriores.

```text
Requisitos
    |
    v
Arquitetura
    |
    v
Estrutura do projeto
    |
    +-------------------+
    |                   |
    v                   v
  Model              Storage
    |                   |
    |                   v
    |                 Room
    |                   |
    +---------+---------+
              |
              v
        Conversation
           Context
              |
              v
            API
              |
              v
            Ktor
              |
              v
           Gemini
              |
              v
          Integração
              |
              v
            Testes
              |
              v
        Entrega
```

A interface poderá ser desenvolvida em paralelo com partes do backend da aplicação, desde que as interfaces entre os componentes estejam previamente definidas.


# 20. Diagramas da Arquitetura

## 20.1 Diagrama MVC

O diagrama MVC deverá representar a comunicação entre:

```text
View
 ↓
Controller
 ↓
Model
```

bem como a interação do Controller com as componentes responsáveis pela persistência e comunicação externa.

> **Diagrama MVC: adicionar após definição final da arquitetura.**

---

## 20.2 Diagrama da Arquitetura

O diagrama completo deverá representar:

```text
             ┌─────────────┐
             │    View     │
             └──────┬──────┘
                    |
                    v
             ┌─────────────┐
             │ Controller  │
             └──────┬──────┘
                    |
          ┌─────────┴─────────┐
          |                   |
          v                   v
     ┌─────────┐         ┌─────────┐
     │ Storage │         │   API   │
     │  Room   │         │  Ktor   │
     └─────────┘         └────┬────┘
                              |
                              v
                         ┌─────────┐
                         │ Gemini  │
                         └─────────┘
```

> **Diagrama da arquitetura: adicionar após definição final.**

---

# 21. Critérios de Organização do Código

Para manter o projeto organizado:

- Cada componente deverá possuir uma responsabilidade bem definida.
- A View não deverá comunicar diretamente com a API.
- A View não deverá aceder diretamente à Room.
- O acesso à API deverá ser realizado através da camada API.
- O acesso à base de dados deverá ser realizado através da camada Storage.
- O Controller deverá coordenar a lógica de negócio.
- Os modelos deverão representar os dados da aplicação.
- O código deverá evitar duplicação desnecessária.
- As classes deverão ser divididas quando uma responsabilidade se tornar demasiado complexa.

---

# 22. Critérios de Qualidade

Durante o desenvolvimento serão considerados os seguintes critérios:

## Manutenção

O código deverá ser organizado de forma a permitir alterações futuras sem modificar desnecessariamente outras componentes.

## Separação de responsabilidades

Cada camada deverá possuir responsabilidades específicas.

## Robustez

A aplicação deverá continuar funcional perante:

- Falta de Internet.
- Erros HTTP.
- Respostas inesperadas da API.
- Histórico vazio.
- Conversas existentes.
- Falhas de comunicação.

## Persistência

As conversas deverão permanecer disponíveis após o encerramento e reabertura da aplicação.

## Experiência do utilizador

A aplicação deverá fornecer feedback adequado quando:

- Uma mensagem está a ser enviada.
- Não existe Internet.
- O Gemini devolve um erro.
- A API Key não está configurada.
- Ocorre outro problema de comunicação.


# Referencias

Imagens e ideas de codigo:

Imagem construção Construction.png -> https://stackoverflow.com/questions/36053603/mvc-project-and-mvc-project-with-api
