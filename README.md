# E-commerce Spring

[![CI](https://github.com/renokkj/ecommerce-spring/actions/workflows/ci.yml/badge.svg)](https://github.com/renokkj/ecommerce-spring/actions/workflows/ci.yml)

Loja virtual full stack: API REST em Java com Spring Boot e front-end em React. Cobre catálogo com filtros e paginação, carrinho, checkout com baixa de estoque, pedidos com fluxo de status, upload de imagens e pagamento pelo Mercado Pago em modo de teste.

## Funcionalidades

- Cadastro e login com JWT; perfis de cliente e administrador
- Catálogo paginado com busca por nome, filtro por categoria, faixa de preço e disponibilidade, e ordenação
- Carrinho com validação de estoque
- Checkout transacional: confere estoque, dá baixa, copia o preço do momento da compra e esvazia o carrinho
- Pedidos com fluxo de status controlado (`PENDING`, `PAID`, `SHIPPED`, `DELIVERED`, `CANCELED`); cancelar devolve o estoque
- Pagamento pelo Checkout Pro do Mercado Pago, confirmado por webhook; sem credenciais, usa um gateway simulado
- Upload de imagens de produto com validação do tipo real do arquivo e do tamanho
- Painel administrativo de produtos e pedidos
- Documentação interativa da API com Swagger

## Tecnologias

| Camada | Tecnologias |
|---|---|
| Back-end | Java 21, Spring Boot 3 (Web, Data JPA, Security, Validation), JJWT |
| Banco de dados | PostgreSQL 16, Flyway |
| Front-end | React 18, TypeScript, Vite, React Router |
| Testes | JUnit 5, Mockito, AssertJ, Testcontainers |
| Infra | Docker, Docker Compose, GitHub Actions |

## Decisões de projeto

- **Preço congelado no pedido:** o item do pedido guarda nome e preço no momento da compra, então editar um produto não altera pedidos antigos.
- **Concorrência no estoque:** produtos usam bloqueio otimista (`@Version`). Se duas compras disputam o mesmo estoque, uma recebe `409` em vez de vender a mais.
- **Webhook confiável:** a notificação do Mercado Pago não é aceita como verdade; a API consulta o pagamento no provedor antes de marcar o pedido como pago. O processamento é idempotente.
- **Exclusão lógica:** remover um produto o desativa, preservando o histórico de pedidos.
- **Segredos fora do código:** senha do banco, chave do JWT, administrador e token de pagamento vêm de variáveis de ambiente.
- **Erros padronizados:** todas as respostas de erro seguem o mesmo formato, com mensagem e erros por campo.

## Como rodar

Pré-requisitos: Docker. Para rodar sem Docker: Java 21, Maven e Node 20.

```bash
git clone https://github.com/renokkj/ecommerce-spring.git
cd ecommerce-spring
cp .env.example .env    # preencha JWT_SECRET, ADMIN_EMAIL e ADMIN_PASSWORD
docker compose up --build
```

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html

Front-end:

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

Acesse http://localhost:5173. O catálogo já vem com produtos de exemplo, criados pela migration `V2__seed_catalog.sql`.

### Rodando a API sem Docker

```bash
docker compose up db          # ou um PostgreSQL local com banco "ecommerce"
cd backend
mvn spring-boot:run
```

## Variáveis de ambiente

| Variável | Descrição |
|---|---|
| `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` | Conexão com o PostgreSQL |
| `JWT_SECRET` | Chave de assinatura dos tokens (mínimo 32 caracteres) |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Administrador criado na primeira execução |
| `PUBLIC_BASE_URL` | URL pública da API (links de imagem e webhook) |
| `FRONTEND_URL`, `CORS_ALLOWED_ORIGINS` | Endereço do front-end |
| `MERCADOPAGO_ACCESS_TOKEN` | Opcional. Token de teste do Mercado Pago; sem ele, o pagamento é simulado |

## Testes

```bash
cd backend
mvn verify
```

- Testes unitários das regras de negócio: checkout, estoque, fluxo de status, JWT e validação de upload.
- Teste de integração de ponta a ponta (`PurchaseFlowIT`) com PostgreSQL real via Testcontainers: cadastro, catálogo, carrinho, checkout, pagamento e permissões. Precisa do Docker rodando.

O GitHub Actions executa os testes do back-end e o build do front-end a cada push e pull request.

## Principais endpoints

| Método | Rota | Acesso |
|---|---|---|
| `POST` | `/api/auth/register`, `/api/auth/login` | Público |
| `GET` | `/api/products?q=&categoryId=&minPrice=&maxPrice=&inStock=&page=&size=&sort=&direction=` | Público |
| `GET` | `/api/products/{id}`, `/api/categories` | Público |
| `GET` `POST` `PATCH` `DELETE` | `/api/cart`, `/api/cart/items/{productId}` | Cliente |
| `POST` | `/api/orders` (checkout), `/api/orders/{id}/cancel`, `/api/orders/{id}/payment` | Cliente |
| `GET` | `/api/orders`, `/api/orders/{id}` | Cliente |
| `POST` | `/api/payments/webhook` | Mercado Pago |
| `GET` `POST` `PUT` `DELETE` | `/api/admin/products`, `/api/admin/products/{id}/image` | Admin |
| `GET` `PATCH` | `/api/admin/orders`, `/api/admin/orders/{id}/status` | Admin |

A lista completa, com exemplos, está no Swagger.

## Estrutura

```
backend/
  src/main/java/com/isabellireno/ecommerce/
    user/      cadastro, login e administrador inicial
    security/  JWT e filtro de autenticação
    product/   catálogo, filtros (Specifications) e upload
    category/
    cart/
    order/     checkout e fluxo de status
    payment/   Mercado Pago, gateway simulado e webhook
    storage/   armazenamento de imagens
    common/    tratamento de erros e paginação
  src/main/resources/db/migration/   migrations do Flyway
frontend/
  src/pages/   catálogo, produto, carrinho, pedidos e painel admin
```

## Próximos passos

- Deploy da API e do banco (Render ou Railway) e do front-end (Vercel)
- Armazenamento de imagens em serviço externo
- Cupons de desconto e e-mail de confirmação de pedido
