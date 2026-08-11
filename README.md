# HR Management System

Sistema fullstack de gestão de RH desenvolvido do zero, com deploy completo em produção — backend, frontend e banco de dados hospedados em nuvem.

🌐 [Demo Online](https://hr-management-system-seven-gilt.vercel.app)
📋 [Swagger API](https://hr-management-system-4smu.onrender.com/swagger-ui/index.html)
💻 [Repositório](https://github.com/vinizx3/HR-Management-System)

---

## Índice

- [Sobre o projeto](#sobre-o-projeto)
- [Contas de demonstração](#contas-de-demonstração)
- [Stack](#stack)
- [Funcionalidades](#funcionalidades)
- [Arquitetura](#arquitetura)
- [Testes](#testes)
- [CI/CD](#cicd)
- [Rodando localmente](#rodando-localmente)
- [Autor](#autor)

---

## Sobre o projeto

O sistema simula o ambiente de RH de uma empresa real, com dois perfis de acesso distintos — **HR Manager** e **Employee** — cada um com permissões e telas específicas.

O foco foi implementar regras de negócio reais (inspiradas na CLT), arquitetura em camadas, segurança de API e comunicação assíncrona entre módulos, tudo isso com deploy automatizado e funcionando publicamente.

---

## Contas de demonstração

Para explorar o sistema sem precisar criar uma conta, use as credenciais abaixo. São contas **somente leitura** — permitem navegar por todas as telas e visualizar dados reais de teste, mas não é possível criar, editar, aprovar ou excluir nada, protegendo a integridade dos dados de demonstração.

| Perfil | E-mail | Senha | O que dá pra ver |
|---|---|---|---|
| HR Manager (demo) | `demo.admin@rh.com` | `demo123` | Dashboard gerencial, lista de funcionários, aprovações pendentes de férias e ajustes de ponto |
| Employee (demo) | `demo.employee@rh.com` | `demo123` | Dashboard pessoal, histórico de ponto, banco de horas, solicitações de férias e notificações |

> As contas demo usam roles dedicadas (`DEMO_ADMIN` / `DEMO_EMPLOYEE`), restritas a endpoints de leitura no Spring Security — a restrição é garantida no backend, não só escondida na interface.

---

## Stack

**Backend**
Java 21 · Spring Boot 3 · Spring Security · JWT · Spring Data JPA · Hibernate · PostgreSQL · Apache Kafka · JUnit 5 · Mockito · Swagger/OpenAPI

**Frontend**
Angular 19 · TypeScript · Bootstrap 5

**DevOps**
Docker · Docker Compose · GitHub Actions · Render · Vercel · Neon (PostgreSQL)

---

## Funcionalidades

### Autenticação e Segurança
- JWT stateless — role do usuário no payload do token, sem consulta ao banco a cada requisição
- RBAC com quatro perfis: HR Manager, Employee, e duas roles de demonstração somente leitura
- Guards de rota no Angular por perfil
- Endpoints protegidos por método HTTP e role no Spring Security

### Gestão de Funcionários
- CRUD completo com soft delete
- Listagem apenas de funcionários ativos
- Validação de e-mail duplicado

### Controle de Ponto
- Clock-in e clock-out com cálculo automático de horas trabalhadas
- Jornada padrão de 8h com tolerância de 10 minutos
- Banco de horas acumulado automaticamente no clock-out
- Fechamento automático de registros em aberto às 23h55
- Fluxo completo de ajuste de ponto com aprovação pelo RH

### Gestão de Férias
- Solicitação com validações CLT: mínimo 30 dias de antecedência, máximo 30 dias de período
- Fluxo de aprovação gerencial
- Notificações assíncronas via Kafka após aprovação ou rejeição

### Notificações
- Eventos publicados no Kafka ao aprovar ou rejeitar férias
- Consumer processa e persiste a notificação para o funcionário
- Em produção (sem Kafka): producer direto via Spring Profiles

---

## Arquitetura

```
Angular 19 (Vercel)
        ↓
Spring Boot REST API (Render)
        ↓
PostgreSQL (Neon)

Componentes auxiliares: Apache Kafka · Docker · GitHub Actions
```

**Decisão de design relevante:** o módulo de férias não chama o `NotificationService` diretamente. Ele publica um evento no Kafka e o consumer processa de forma independente. Isso desacopla os módulos — uma falha na notificação não impacta a aprovação das férias.

Em produção, onde Kafka não está disponível, um producer alternativo (`@Profile("prod")`) chama o `NotificationService` diretamente, mantendo o comportamento sem dependência de infraestrutura externa.

---

## Testes

+55 testes unitários com JUnit 5 e Mockito.

Detalhe importante: todos os services que dependem de data/hora recebem um `Clock` injetável via construtor. Isso garante que os testes são 100% determinísticos — sem `LocalDate.now()` solto no código.

Cenários cobertos:
- Regras de negócio de ponto, férias e banco de horas
- Fluxos de aprovação e rejeição
- Validações de negócio e exceções esperadas
- Cálculo de horas extras e tolerância
- Notificações e marcação como lidas

---

## CI/CD

Pipeline com GitHub Actions a cada push na `main`:

1. Checkout do repositório
2. Setup Java 21
3. Build com Maven
4. Execução dos testes
5. Deploy automatizado no Render

---

## Rodando localmente

```bash
# Clonar o repositório
git clone https://github.com/vinizx3/HR-Management-System

# Subir os containers (PostgreSQL + Kafka + aplicação)
docker-compose up --build

# Backend disponível em
http://localhost:8081

# Frontend
cd frontend
npm install
ng serve
# Disponível em http://localhost:4200
```

---

## Autor

**Vinicius Fernandes** — Desenvolvedor Java Full Stack
[LinkedIn](https://www.linkedin.com/in/viniciusfernandes-dev/) · [GitHub](https://github.com/vinizx3)
