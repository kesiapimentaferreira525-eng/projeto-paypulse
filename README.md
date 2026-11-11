 # projeto-paypulse

## API e dados para demonstração

O front-end pode carregar a tabela com `GET http://localhost:8082/api/v1/transactions`.
Cada item retorna `id`, `accountId`, `amount`, `status` e `createdAt`; a API aceita
novas transações em `POST` no mesmo endpoint.

Para preencher a tabela com dados fictícios, inicie a API com o perfil `demo`:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=demo"
```

O dashboard fica disponível em `http://localhost:8082/`. O perfil `demo` insere
40 transações fictícias no MySQL e as exibe no dashboard, com diferentes valores,
contas, status (aprovadas, recusadas, pendentes e em processamento) e datas
distribuídas pelos últimos 30 dias. A carga é idempotente: mantém os dados
existentes e não duplica contas de demonstração. Sem o perfil `demo`, nenhuma
carga de demonstração é inserida.

A porta padrão é `8082` para evitar conflito com serviços que já usam `8081`.
Para alterá-la, defina a variável de ambiente `SERVER_PORT`.
