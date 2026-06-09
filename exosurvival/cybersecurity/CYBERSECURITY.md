# Cybersecurity — Global Solution 2026
## ExoSurvival: Plataforma Educacional de Dados Espaciais

---

## 1. Análise de Riscos e Ameaças (Threat Modeling)

### 1.1 Identificação de Ativos

Os ativos críticos do ExoSurvival são:

| Ativo | Tipo | Criticidade |
|-------|------|-------------|
| Credenciais dos usuários (email, senha hash) | Dado pessoal sensível | Alta |
| Tokens JWT ativos | Dado de autenticação | Alta |
| Dados de planetas criados pelos usuários | Dado de jogo | Média |
| Histórico de partidas (GameSession) | Dado comportamental | Média |
| Banco de dados PostgreSQL | Infraestrutura | Alta |
| Serviço de API (Spring Boot) | Infraestrutura | Alta |
| Parâmetros baseline de exoplanetas (ExoplanetBaseline) | Dado educacional | Baixa |

### 1.2 Modelo de Ameaças

**Vetor 1 — Interceptação de credenciais em trânsito (Man-in-the-Middle)**
Um atacante em rede não confiável (ex: Wi-Fi público) poderia interceptar o tráfego HTTP e capturar tokens JWT ou credenciais de login não criptografadas.

- **Impacto:** Roubo de sessão, acesso não autorizado a dados do usuário.
- **Probabilidade:** Alta se não houver TLS/HTTPS.
- **Controle:** Comunicação obrigatória via HTTPS com TLS 1.2+; tokens JWT com expiração de 24h.

**Vetor 2 — Força bruta e credential stuffing no endpoint de login**
O endpoint `POST /api/v1/auth/login` é público e poderia ser alvo de tentativas automatizadas de login com listas de senhas vazadas de outros serviços.

- **Impacto:** Comprometimento de contas de usuários reais.
- **Probabilidade:** Alta — é o ataque mais comum contra APIs públicas.
- **Controle:** Rate limiting por IP, bcrypt com custo 10+ no armazenamento de senhas.

**Vetor 3 — Acesso não autorizado a dados de outros usuários (IDOR)**
Um usuário autenticado poderia tentar acessar `/api/v1/planets/{id}` com IDs de planetas de outros usuários.

- **Impacto:** Exposição de dados de jogo e comportamentais de terceiros.
- **Probabilidade:** Média — requer usuário autenticado e conhecimento da API.
- **Controle:** Verificação de ownership em `PlanetService` e `GameSessionService` — toda operação valida que o recurso pertence ao `email` extraído do JWT.

**Vetor 4 — Injeção de dados maliciosos (SQL Injection / Payload inválido)**
Campos como `name` do planeta ou `causeOfDeath` da sessão poderiam receber payloads maliciosos.

- **Impacto:** Corrupção de dados, possível escalada para acesso ao banco.
- **Probabilidade:** Baixa (JPA/Hibernate usa queries parametrizadas por padrão).
- **Controle:** Bean Validation em todos os DTOs de entrada, JPA com queries parametrizadas, sem uso de `nativeQuery` com concatenação de string.

**Vetor 5 — Exfiltração ou destruição de dados via container comprometido**
Se o container da API for comprometido (ex: dependência vulnerável), o atacante teria acesso direto ao banco de dados pela rede interna Docker.

- **Impacto:** Dump completo da base de dados.
- **Probabilidade:** Baixa, mas com impacto máximo.
- **Controle:** Rede Docker isolada, usuário não-root no container (`exosurvival` user no Dockerfile), variáveis sensíveis via environment variables (nunca hardcoded).

---

## 2. Arquitetura de Segurança (Controles)

### 2.1 Controles de Acesso

**Autenticação:**
- Todos os endpoints, exceto `/api/v1/auth/register` e `/api/v1/auth/login`, exigem um Bearer Token JWT válido.
- O `JwtAuthenticationFilter` intercepta todas as requisições antes do processamento e valida a assinatura e expiração do token usando HMAC-SHA256.
- Tokens expirados são rejeitados silenciosamente — o usuário recebe 401 e deve reautenticar.

**Autorização por propriedade (principle of least privilege):**
- Não existe conceito de "admin" ou acesso cross-user. Cada usuário só enxerga e opera seus próprios planetas e sessões.
- A validação de propriedade (`assertOwnership`) é feita na camada de serviço, não apenas no banco — garantindo que um token válido de usuário A não acesse dados de B mesmo que B seja passado na URL.

**Senhas:**
- Armazenadas com BCrypt (Spring Security padrão, custo mínimo 10 rounds).
- Nunca retornadas em nenhum response DTO.

### 2.2 Proteção de Dados

**Em trânsito:**
- A API deve ser exposta exclusivamente via HTTPS. Em produção, recomenda-se um reverse proxy (NGINX ou AWS ALB) responsável pela terminação TLS com certificado válido.
- Em ambientes de desenvolvimento local, o Docker Compose sobe apenas HTTP — essa configuração não deve ser usada em produção.

**Em repouso:**
- Senhas são armazenadas exclusivamente como hash BCrypt — não há senha reversível no banco.
- Tokens JWT não são persistidos — são stateless e vivem apenas no cliente.
- Dados de comportamento do jogo (sessões) são armazenados sem informações financeiras ou de localização física.

**Minimização de dados:**
- O registro exige apenas `email`, `username` e `password`. Nenhum dado pessoal além desses é coletado.
- Os dados de planeta são totalmente ficcionais — não há coleta de geolocalização ou dados sensíveis de terceiros.

### 2.3 Segurança da Infraestrutura

**Arquitetura de rede Docker:**
```
[React Native App]
        |
       HTTPS
        |
[NGINX (TLS termination)] ← recomendado em prod
        |
[API Container - porta 8080, usuário não-root]
        |
[Rede interna Docker]
        |
[PostgreSQL Container - porta 5432, não exposta externamente]
```

O banco de dados NÃO deve ter a porta `5432` mapeada para o host em produção. No `docker-compose.yml` fornecido, o mapeamento `5432:5432` existe apenas para facilitar o desenvolvimento — remova-o em ambiente produtivo.

**Variáveis de ambiente e segredos:**
- `JWT_SECRET`, `DB_PASSWORD` são injetados via environment variables.
- O arquivo `.env` está listado no `.gitignore` — o `.env.example` é o único commitado.
- Em produção, usar serviço de secrets management (AWS Secrets Manager, HashiCorp Vault ou variáveis do Railway/Render).

**Dependências:**
- Usar apenas versões LTS do Spring Boot e verificar CVEs periodicamente via `mvn dependency:check` ou Dependabot.
- A imagem base `eclipse-temurin:17-jre-jammy` é uma das mais auditadas para Java em produção.

---

## 3. Governança e Compliance

### 3.1 Alinhamento com ISO 27001

O projeto adota, na medida do contexto acadêmico, os princípios dos controles da ISO 27001:

| Controle ISO 27001 | Implementação no ExoSurvival |
|--------------------|------------------------------|
| A.9 — Controle de Acesso | JWT stateless, sem sessões server-side, verificação de ownership por recurso |
| A.10 — Criptografia | BCrypt para senhas, HMAC-SHA256 para tokens, TLS para trânsito |
| A.12 — Segurança operacional | Imagem Docker com usuário não-root, sem secrets hardcoded |
| A.13 — Segurança de comunicações | CORS configurado, HTTPS obrigatório em produção |
| A.14 — Aquisição e desenvolvimento seguro | Bean Validation em todos os inputs, JPA parametrizado |
| A.16 — Gestão de incidentes | Plano de resposta documentado na seção 4 |
| A.18 — Conformidade | Minimização de dados pessoais coletados, alinhamento com LGPD |

**Gestão de Riscos:**
Os cinco vetores identificados na seção 1.2 foram avaliados por probabilidade × impacto. Os de maior criticidade (MITM, IDOR, credential stuffing) têm controles técnicos ativos implementados no código. Os de menor probabilidade (comprometimento de container) têm controles de contenção documentados.

### 3.2 Privacidade e LGPD

O ExoSurvival coleta apenas os dados estritamente necessários para funcionamento:

**Dados coletados:**
- `email` — identificador de conta (finalidade: autenticação)
- `username` — nome de exibição (finalidade: personalização)
- `password` (hash) — credencial de acesso (finalidade: autenticação)
- Dados de jogo: parâmetros de planeta e resultados de sessão (finalidade: funcionalidade do jogo)

**Princípios LGPD aplicados:**

| Princípio | Aplicação |
|-----------|-----------|
| Finalidade | Dados coletados exclusivamente para operação do jogo |
| Necessidade | Apenas email, username e senha são coletados no cadastro |
| Livre acesso | Endpoints GET permitem ao usuário visualizar todos os seus dados |
| Segurança | BCrypt, JWT, HTTPS, validação de inputs |
| Não discriminação | Nenhum dado sensível de categoria especial (raça, saúde, etc.) é coletado |

**Retenção:** Dados são mantidos enquanto a conta existir. A exclusão de conta (endpoint a implementar) deve remover em cascata todos os planetas e sessões (configurado via `CascadeType.ALL` nas entidades).

**Dados de menores:** O sistema não possui controle de idade. Em versão produtiva, recomenda-se adicionar verificação de faixa etária no cadastro, pois dados de menores exigem consentimento parental sob a LGPD.

---

## 4. Plano de Resiliência e Continuidade

### 4.1 Plano de Resposta a Incidentes

O plano segue o framework NIST SP 800-61 adaptado:

#### Fase 1 — Detecção e Análise

**Sinais de comprometimento a monitorar:**
- Pico anormal de requisições em `/api/v1/auth/login` (possível brute force)
- Tokens JWT sendo usados de múltiplos IPs geograficamente distantes em curto intervalo
- Queries inesperadas no banco fora do padrão da aplicação
- Erros 500 em cascata com stack traces expostos nos logs

**Ferramentas recomendadas:**
- Logs centralizados (ELK Stack ou CloudWatch)
- Alertas de anomalia via uptime monitoring (UptimeRobot, Grafana)

#### Fase 2 — Contenção

**Contenção imediata (< 1 hora):**
1. Rotacionar o `JWT_SECRET` para invalidar todos os tokens ativos em circulação. Como os tokens são stateless e assinados com o secret, a troca força reautenticação de todos os usuários — efetivamente um logout global.
2. Bloquear o IP ou range de IPs maliciosos via firewall de rede (regra no security group AWS ou no NGINX).
3. Colocar o endpoint comprometido em modo read-only ou desativá-lo temporariamente.

**Contenção de longo prazo (< 24 horas):**
4. Snapshot do banco de dados antes de qualquer limpeza.
5. Isolar o container comprometido sem destruí-lo (preservação de evidências).
6. Subir nova instância da API com imagem limpa e secret rotacionado.

#### Fase 3 — Erradicação

1. Identificar e corrigir a vulnerabilidade explorada no código-fonte.
2. Atualizar a dependência vulnerável (se aplicável) e reconstruir a imagem Docker.
3. Revisar todos os logs do período comprometido para mapear o escopo do acesso.
4. Se senhas foram expostas (ex: dump do banco): notificar usuários afetados e forçar redefinição de senha.

#### Fase 4 — Recuperação

1. Restaurar o serviço a partir do último backup íntegro do PostgreSQL (backup diário via `pg_dump` recomendado).
2. Validar integridade dos dados restaurados contra logs de auditoria.
3. Reativar o serviço com monitoramento intensificado por 72 horas.
4. Comunicar usuários sobre o incidente, dados potencialmente afetados e ações tomadas (obrigação LGPD Art. 48 — notificação em até 72h à ANPD em casos de risco relevante).

#### Fase 5 — Lições aprendidas

Dentro de 7 dias após o incidente:
- Documentar linha do tempo, causa raiz, impacto e controles adicionados.
- Atualizar o modelo de ameaças com o novo vetor identificado.
- Revisar e reforçar os controles da camada afetada.

### 4.2 Estratégia de Backup

| Item | Frequência | Retenção | Mecanismo |
|------|-----------|---------|-----------|
| Dump PostgreSQL | Diário | 7 dias | `pg_dump` via cron no container ou serviço gerenciado |
| Código-fonte | Contínuo | Indefinido | Git (GitHub/GitLab) |
| Variáveis de ambiente | Manual | Sempre que houver mudança | Secrets manager ou repositório privado criptografado |

### 4.3 Objetivos de Recuperação

- **RTO (Recovery Time Objective):** < 4 horas — tempo máximo aceitável de indisponibilidade
- **RPO (Recovery Point Objective):** < 24 horas — máximo de dados que podem ser perdidos

---

## Diagrama de Fluxo de Segurança

```
[Cliente Mobile]
      |
      | HTTPS + Bearer Token
      v
[JwtAuthenticationFilter]
      |
      |-- Token inválido/expirado --> 401 Unauthorized
      |
      |-- Token válido
      v
[Controller] --> [Service]
                     |
                     |-- Verifica ownership do recurso
                     |
                     |-- Recurso de outro usuário --> 403 Forbidden
                     |
                     |-- OK --> [Repository] --> [PostgreSQL]
```

---

*Documento elaborado como entregável da Global Solution 2026 — Cybersecurity (3ES).*
*Projeto: ExoSurvival — Plataforma educacional de dados espaciais.*


## Autores

Desenvolvido como projeto acadêmico para a **Global Solution** — Engenharia de Software (FIAP).

Bruno Otavio Silva De Oliveira RM556196

Guilherme Flores Pereira de Almeida RM554948

Luiz Fernando de Aragão Souza RM555561

Bruno Otavio Silva De Oliveira RM556196

Marcello de Freitas Moreira RM557531

Leonardo Gonçalves Novaes RM554807