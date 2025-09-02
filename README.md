![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens)
![MySQL](https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white)

<h1>Budget Master</h1>
<p> 
O BudgetMaster é uma API RESTful para gestão financeira pessoal e familiar.
Ele simplifica o controle de entradas e saídas, calcula automaticamente sua reserva de emergência e ainda permite criar grupos familiares para acompanhar a saúde financeira conjunta.
Além disso, o projeto enfatiza segurança, implementando autenticação robusta e boas práticas de arquitetura.
</p>
<h2>📌 Funcionalidades</h2>
<ul>
  <li>Gestão Financeira Pessoal</li>
    <ul>
      <li>Registro de entradas (renda) e saídas (despesas).</li>
      <li>Cálculo automático da reserva de emergência.</li>
    </ul>
  <li>Gestão Familiar</li>
    <ul>
      <li>Criação de famílias.</li>
      <li>Convite de membros via email ou QR Code.</li>
      <li>Cálculo de rendas e despesas totais da família.</li>
      <li>Reserva de emergência familiar.</li>
    </ul>
  <li>Segurança</li>
    <ul>
      <li>Autenticação com JWT.</li>
      <li>Login com Google OAuth2.</li>
      <li>Autenticação de 2 fatores (via email e aplicativo autenticador com QR Code).</li>
    </ul>
  <li>Testes</li>
  <li>Documentação utilizando da API com Swagger</li>
</ul>
<br>

<h2>🛠 Tecnologias </h2>
<p>As seguintes ferramentas foram utilizadas para construir o projeto: 
  <ul>
    <li>Java 17+</li>
    <li> <a href= https://spring.io/projects/spring-boot> Spring Boot</a> </li> 
    <li> <a href= https://spring.io/projects/spring-security>Spring Security</a> </li>
    <li> <a href= https://jwt.io>JWT</a> </li>
    <li><a href= https://dev.mysql.com/doc/>MySQL</a> </li>
    <li>Flyway</li>
    <li>Testes: JUnit + Mockito</li>
  </ul>
</p>

<h2>Estrutura do Projeto</h2>
<pre>
  src/main/java/phenriqued/BudgetMaster
  | - Controllers # Controllers REST
  | - DTOs # Objetos de transferencia de dados
  | - Infra # Infraestrutura do projeto contento as pastas Email, Exceptions e Security
  | - Models # Entidades JPA
  | - Repositories # Interfaces de persistência
  | - Services # Regras de negócios
  src/main/resources
  | - db.migration # migrations FyWay
</pre>

<h2> 🚀 Começando. </h2>
<p>
Essas instruções permitirão que você obtenha uma cópia do projeto em operação na sua máquina local para fins de desenvolvimento e teste.
</p>
</p>
Antes de começar, certifique-se de ter instalado:
  <ul>
    <li>Git</li>
    <li>Java 17+</li>
    <li>MySQL</li>
  </ul>
</p>
<h3>📥 Clonando o Repositório</h3>
Abra o terminal e execute:
<pre><code> git clone https://github.com/phenriqued/BudgetMaster.git </code></pre>
<br>
<strong>⚙️ Configurando o Ambiente:</strong>
<p>
  Configure o application.properties com suas credenciais do <a href= https://mail.google.com> Gmail</a>:
  <pre>
    <code>
      spring.mail.username=${EMAIL_USERNAME}
      spring.mail.password=${EMAIL_PASSWORD}</code>
  </pre>
  <br>
   Configure o application.properties-dev com suas credenciais do MySQL, caso deseje utiliza-lo:
     <pre>
    <code>
      spring.datasource.url=${DATASOURCE_URL}
      spring.datasource.username=${DATASOURCE_USERNAME}
      spring.datasource.password=${DATASOURCE_PASSWORD}</code>
  </pre>
</p>
<p>
  <p>Configure a variável <code>ALGORITHM_JWT</code> de ambiente da <strong>SECRET</strong> dos tokens gerados por JWT:</p>
  Localizado na pasta: <code>src/main/java/phenriqued/BudgetMaster/Infra/Security/Service/JWTService.java</code>:
    <pre>
    <code>
          @Value("${ALGORITHM_JWT}")
          private String secret;</code>
  </pre>
</p>
<p>
  <p>Configure as variáveis <code>CLIENT_ID_GOOGLE</code> e <code>CLIENT_SECRET_GOOGLE</code> de ambiente da <strong>login pelo google</strong>:</p>
  Localizado na pasta: <code>src/main/java/phenriqued/BudgetMaster/Services/LoginService/LoginGoogleService.java</code>:
    <pre>
    <code>
        @Value("${CLIENT_ID_GOOGLE}")
        private String clientId;
        @Value("${CLIENT_SECRET_GOOGLE}")
        private String clientSecret;</code>
  </pre>
</p>
<h2> 📃 Documentação da API com Swagger </h2>
<p>
A documentação interativa da API foi gerada usando o Swagger. Ela permite que você visualize todos os endpoints, entenda os schemas de requisição e resposta, e até mesmo teste as rotas diretamente do seu navegador.
</p>
<h3>Como Acessar a Documentação:</h3>
<p>Para visualizar a documentação, siga os passos abaixo:</p>
<ol>
  <li>Verifique se a aplicação está rodando na sua máquina.</li>
  <li>Abra seu navegador e acesse a seguinte URL:</li>
  `http://localhost:8080/swagger-ui.html`
</ol>
<p>Com esta interface, você pode explorar as rotas, entender os parâmetros de cada requisição e testar a API em tempo real.</p>

<h2>🧪 Testes</h2>
<p>O projeto possui testes para os controllers de Login e lógica de serviços. Após configurar o ambiente, é possível testar os controllers de login utilizando o Google, login padrão, cadastro de usuário e autenticação de dois fatores.</p>
<h3>Estrutura de Testes:</h3>
<p>A estrutura de testes do projeto é organizada por pacotes, seguindo a mesma hierarquia do código de produção.</p>
<ul>
  <li><strong>Controllers:</strong> Contém os testes de integração para os endpoints da API, garantindo que as requisições e respostas estão funcionando conforme o esperado.</li>
      <ul>
        <li><strong>LoginControllers:</strong> Testa a funcionalidade de autenticação, incluindo login padrão, cadastro de usuário e integrações de terceiros.</li>
        <li><strong>TwoFactorAuthenticationController:</strong> Foca nos testes para a autenticação de dois fatores.</li>
        <li><strong>UserControllers:</strong> Inclui testes relacionados à gestão de usuários.</li>
      </ul>
  <li><strong>Services:</strong> Abriga os testes unitários da lógica de negócio. 
    Esses testes verificam que os métodos dos serviços (como ExpenseService, FamilyService, etc.) funcionam corretamente de forma isolada, sem depender de recursos externos como o banco de dados.</li>
</ul>
<h3>Como Rodar os Testes</h3>
<p>Para executar todos os testes da aplicação, utilize o seguinte comando no terminal na raiz do projeto:</p>
  <pre>
    <code>./mvnw test</code></pre>
<p>Este comando irá compilar o projeto e rodar todos os testes automatizados, garantindo a qualidade e confiabilidade do código. <br>
<strong>⚠️ Importante:</strong> Para que os testes de controllers rodem corretamente, é essencial que todas as variáveis de ambiente estejam configuradas, caso contrário, erros de conexão ou autenticação podem ocorrer.
</p>


