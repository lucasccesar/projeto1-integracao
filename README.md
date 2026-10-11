# Demonstração OAuth 2.0, OpenID Connect e segurança de APIs

Projeto acadêmico executável para um seminário de 30 minutos. A API de pedidos é um OAuth2 Resource Server; o Keycloak é o Identity Provider, OpenID Provider e Authorization Server; o PostgreSQL armazena apenas pedidos.

## Pré-requisitos

- Docker Desktop com Docker Compose.
- Java 21 para executar ou compilar fora do container.
- Postman para o login interativo com Authorization Code + PKCE.

O ambiente usa HTTP e credenciais descartáveis somente para localhost. Não reutilize essas credenciais fora da demonstração. O projeto não contém tokens reais nem segredos externos.

## Subir o ambiente

Na raiz do projeto:

```bash
docker compose up --build -d
docker compose ps
```

Os serviços publicados são:

| Serviço | URL/porta | Função |
| --- | --- | --- |
| API | `http://localhost:8080` | Resource Server e API de pedidos |
| Keycloak | `http://localhost:8081` | Realm `seminar`, OIDC e emissão de tokens |
| PostgreSQL | `localhost:5432` | Banco da tabela `orders` |

Verifique `healthy` em `docker compose ps`. Os logs da API podem ser acompanhados com:

```bash
docker compose logs -f api
```

O realm é importado automaticamente de `infra/keycloak/seminar-realm.json`. O cliente público `seminar-postman` aceita somente a URI exata `https://oauth.pstmn.io/v1/callback`, exige PKCE S256 e não possui client secret.

## Compilar sem Docker

Use Java 21 e o Maven Wrapper:

```bash
./mvnw clean package -Dmaven.test.skip=true
```

No Windows:

```powershell
.\mvnw.cmd clean package "-Dmaven.test.skip=true"
```

Não há testes automatizados, classes em `src/test`, mocks ou dependências de teste. A comprovação do seminário é feita pela execução dos serviços e por requisições HTTP reais.

Para manter a configuração visível no seminário, o cliente público tem mappers explícitos para `sub` e para `resource_access.orders-api.roles`, além do mapper de audiência. O escopo completo é uma simplificação exclusiva deste laboratório local; não é uma recomendação para produção.

## Contas locais descartáveis

| Usuário | Senha | Role em `orders-api` |
| --- | --- | --- |
| `demo-user` | `demo-user-123` | `USER` |
| `demo-admin` | `demo-admin-123` | `USER`, `ADMIN` |

As contas existem somente no realm local importado. O administrador técnico inicial do Keycloak é `admin` / `admin` e serve apenas para a administração local, se necessária.

## Postman e login OIDC

1. Importe `postman/oauth-oidc-demo.postman_environment.json` e selecione o ambiente.
2. Importe `postman/oauth-oidc-demo.postman_collection.json`.
3. Na coleção, abra `Authorization`, escolha OAuth 2.0 e use `Get New Access Token`.
4. Selecione exatamente `Authorization Code (With PKCE)`, `Code Challenge Method = SHA-256`, `Client ID = seminar-postman`, `Auth URL = http://localhost:8081/realms/seminar/protocol/openid-connect/auth`, `Access Token URL = http://localhost:8081/realms/seminar/protocol/openid-connect/token`, `Scope = openid`, redirect URI `https://oauth.pstmn.io/v1/callback` e `Authorize using browser`. Em `Advanced Options`, deixe `Auth Request` e `Token Request` sem parâmetros manuais de PKCE; o Postman deve gerar `code_challenge` e `code_verifier` automaticamente.
5. Faça login como `demo-user` ou `demo-admin` no navegador aberto pelo Postman.
6. Na resposta de tokens, guarde o `access_token` na variável `accessToken` ou `adminAccessToken` e guarde o `id_token` em `idToken` para inspeção. Nunca use `id_token` no header da API.
7. Para demonstrar as duas contas, repita o login como administrador e salve o segundo Access Token em `adminAccessToken`.

O `id_token` prova ao cliente informações da autenticação OIDC. O `access_token` é a credencial de autorização que a API valida. No Access Token, mostre `iss`, `sub`, `aud`, `exp` e `resource_access.orders-api.roles`.

## Cenas HTTP

Também é possível usar cURL. Os comandos abaixo não contêm tokens fixos:

```bash
curl -i http://localhost:8080/api/public
curl -i http://localhost:8080/api/orders
curl -i -H "Authorization: Bearer $ACCESS_TOKEN" http://localhost:8080/api/orders
curl -i -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"description":"Inscrição no seminário","amount":49.90}'
curl -i -X DELETE http://localhost:8080/api/orders/1 \
  -H "Authorization: Bearer $ACCESS_TOKEN"
curl -i -X DELETE http://localhost:8080/api/orders/1 \
  -H "Authorization: Bearer $ADMIN_ACCESS_TOKEN"
```

Resultados esperados:

| Cena | Resultado |
| --- | --- |
| `GET /api/public` sem token | `200 OK` |
| `GET /api/orders` sem token | `401 Unauthorized` |
| `GET /api/orders` com Access Token do usuário | `200 OK` e somente os pedidos do `sub` |
| `POST /api/orders` com Access Token | `201 Created`; `owner_subject` vem do `sub` validado |
| `DELETE /api/orders/{id}` com usuário comum | `403 Forbidden` |
| `DELETE /api/orders/{id}` com administrador | `204 No Content` |
| Access Token expirado, adulterado ou com audience incorreta | `401 Unauthorized` |

O contrato completo está em `docs/context/openapi.yaml`.

## Estrutura

- `src/main/java/com/example/oauthoidcdemo/order`: controller e domínio de pedidos.
- `src/main/java/com/example/oauthoidcdemo/common/security`: decoder JWT, audience, roles e handlers 401/403.
- `src/main/java/com/example/oauthoidcdemo/common/exception`: exceções tipadas e `GlobalExceptionHandler`.
- `infra/keycloak/seminar-realm.json`: realm reproduzível, cliente, roles e usuários de demonstração.
- `docker-compose.yml` e `Dockerfile`: execução dos três serviços.
- `docs/context/`: fonte de verdade do contrato, schema, regras, progresso e ordem de construção.
- `postman/`: coleção e ambiente sem tokens reais.

## Hostnames, issuer e JWKS

O navegador/Postman acessa Keycloak por `http://localhost:8081`. A API dentro do Compose acessa JWKS por `http://keycloak:8080`. O issuer publicado nos tokens continua `http://localhost:8081/realms/seminar`, e a API valida esse valor enquanto busca as chaves no endereço interno.

Se houver `issuer-uri` inválido, confirme que o token contém exatamente `iss = http://localhost:8081/realms/seminar`. Se houver falha de JWKS, confirme que o serviço `keycloak` está `healthy` e que `KEYCLOAK_JWK_SET_URI` usa o hostname Docker. Se houver falha de audience, confirme `aud` contém `orders-api`; não remova o `AudienceValidator`. Se houver erro de PKCE, confirme o redirect URI, `client_id`, scope `openid` e método S256.

Se o realm já tiver sido inicializado em um volume e o JSON for alterado, recrie o volume local da demonstração:

```bash
docker compose down -v
docker compose up --build -d
```

Isso apaga somente os dados locais do Compose, inclusive pedidos e o armazenamento efêmero do Keycloak.

## Encerrar

```bash
docker compose down
```

Para remover também o volume local do PostgreSQL, use `docker compose down -v` conscientemente.
