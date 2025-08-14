# PriceWatcher

Sistema para monitoramento de preços de produtos em sites, com envio automático de alertas via WhatsApp ou SMS.

## Funcionalidades

- Cadastro de produtos a serem monitorados e valor alvo.
- Agendamento automático (de hora em hora) para consulta de preços.
- Envio de notificação se preço atingir ou ficar abaixo do alvo.
- Histórico de consultas realizadas.
- API RESTful.

## Stack

- Java 17+, Spring Boot 3.x, Spring Data JPA
- PostgreSQL (produção), H2 (testes)
- Scheduler (`@EnableScheduling`)
- Jsoup (scraping)
- Twilio (notificações)
- Testcontainers, WireMock, JUnit 5, Mockito
- Docker, Docker Compose

## Rotas principais

| Método | URL                       | Descrição                      |
|--------|---------------------------|--------------------------------|
| POST   | /produtos                 | Cadastrar produto              |
| GET    | /produtos                 | Listar produtos monitorados    |
| GET    | /produtos/{id}            | Detalhes de um produto         |
| DELETE | /produtos/{id}            | Remover produto                |
| GET    | /produtos/{id}/historico  | Histórico de preços            |

## Como rodar

1. Configure variáveis de ambiente/senhas no `docker-compose.yml`.
2. Execute:
   ```sh
   docker-compose up --build
   ```

## Teste rápido da API

```http
POST /produtos
Content-Type: application/json

{
  "nome": "Monitor LG",
  "url": "https://example.com/produto/123",
  "precoAlvo": 900.00,
  "canalNotificacao": "WHATSAPP",
  "telefone": "+5511999999999"
}
```

---

## Explicação detalhada

**1. Estrutura em camadas:**  
Separação por domínio/função (produto, notificação, scheduler, etc). Cada feature tem seu controller, service, repository, model e dto.

**2. Modelagem:**  
ProdutoMonitorado e HistoricoPreco representam os dados principais. Enum CanalNotificacao determina se o alerta é SMS ou WhatsApp.

**3. Repositórios:**  
Interfaces JPA para CRUD automático.

**4. Services:**  
Camada de lógica de negócio: cadastro, listagem, busca, remoção, scraping simulado e histórico.

**5. Scheduler:**  
Executa de hora em hora, consulta cada produto, salva histórico e envia alerta se preço <= alvo.

**6. Notificações:**  
Stub do serviço Twilio (para produção, basta implementar a chamada real da API).

**7. Controllers:**  
Expondo as rotas RESTful para CRUD e histórico.

**8. Teste:**  
Exemplo simples usando Mockito.

**9. Docker:**  
Ambiente pronto para desenvolvimento e produção local.

---

## Como customizar scraping/integração

Basta adaptar o `PriceScraperService` para consumir APIs reais ou fazer scraping com Jsoup, conforme o site monitorado.
