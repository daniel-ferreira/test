# Sistema de Pedidos

## 1. Primeira parte - Refactoring

A primeira parte do teste é fazer o refactor dessa classe Java. Devem ser apontadas quais são as más práticas de acordo com Clean Code e Convenções de código Java:

### **Código original:**

```java
public class TransacaoValidator {

    // A classe configurada no log é diferente da classe atual.
    private static final Logger LOGGER = LoggerFactory.getLogger(CapturaTransacaoService.class);

    // Essa constante não é usada na classe.
    private static final String BIT_02 = "02";

    // O nome da variável é genérico e não descritivo. Lista de quê?
    // Erro de lógica: define os valores da lista, não faz nenhuma alteração e valida se contém um valor não definido.
    private static final List<String> lista = List.of("02", "03", "04", "05", "12");

    // O nome validate não significa muito. O que está sendo validado?
    // O argumento deve ter nome significativo que o descreva, e não apenas uma letra.
    // Ao fazer validações complexas e chamar o método salvar, esse método viola o princípio da responsabilidade.
    public void validate(ISOModel m) {
        // A mensagem do 'log' é pobre e pouco informativa, o que dificulta a depuração e rastreabilidade.
        LOGGER.info("Início");

        // Os nomes das variáveis são pouco significativas.
        // O que não está preenchido? validateAux e auxValidacao são facilmente confundidos. Valor de quê?
        boolean isNotPreenchido = m.getBit02() == null;
        boolean validateAux = m.getBit02() != null && m.getBit02().getValue().isEmpty();
        boolean auxValidacao = m.getBit02() != null && m.getBit02().getValue().isEmpty() && m.getBit03() == null;
        String valor = isNotPreenchido ? "01" : "02";

        try{
            // IFs aninhados dificultam a legibilidade e manutenção. 
            if(!isNotValid(isNotPreenchido, validateAux, auxValidacao, valor)) {
                if(m.getBit03() != null) {
                    // Erro de lógica: define os valores da lista, não faz nenhuma alteração e valida se contém um valor não definido (10).
                    if(m.getBit04() != null && lista.contains("10")) {
                        if(m.getBit05() != null) {
                            if(m.getBit12() != null) {
                                salvar(m, auxValidacao);
                            }
                        }
                    }
                }
            }
        // O corpo do catch vazio esconde erros e dificultam o entendimento da causa-raiz.
        } catch (Exception e) {
        }

        // O método isNotValid já foi chamado com essa mesma entrada. Deveria ser chamado apenas uma vez.
        if(isNotValid(isNotPreenchido, validateAux, auxValidacao, valor)) {
            throw new IllegalArgumentException("Valores não preenchidos");
        }

    }

    // Outro problema com nome pouco descritivo, além de tratar diversas condições.
    private boolean isNotValid(boolean validaPreenchido, boolean validaVazio, boolean validaAux, String str) {
        // Mistura operadores || e && na mesma expressão, o que dificulta o entendimento e pode levar a erros.
        // str pode ser null o que ocasionaria NullPointerException. O melhor seria comparar a constante com a variável "01".equals(str)
        return validaPreenchido || validaVazio && !validaAux && str.equals("01");
    }

    private void salvar(ISOModel m, boolean auxValidacao) {
        // Essa validação afeta a coesão do método. Melhor seria mover essa validação para fora do método.
        if(auxValidacao) {
            throw new IllegalArgumentException("Validacao falhou");
        }
        // Deveria usar o 'log'.
        System.out.println("Salvando transacao " + m.getBit02().getValue());
    }

}

// No geral não há comentários explicativos explicando a intenção da classe, métodos e variáveis.
```

### **Código refatorado:**

```java
public class TransacaoValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransacaoValidator.class);
    private static final List<String> CODIGOS_VALIDOS = List.of("02", "03", "04", "05", "12");

    public void validate(ISOModel isoModel) {
        LOGGER.info("Iniciando validação da transação");

        if (isInvalid(isoModel)) {
            throw new IllegalArgumentException("Valores obrigatórios não preenchidos");
        }

        try {
            processarTransacao(isoModel);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Validação falhou: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            LOGGER.error("Erro inesperado ao validar transação", e);
            throw new RuntimeException("Erro ao processar transação", e);
        }
    }

    private boolean isInvalid(ISOModel isoModel) {
        if (isoModel.getBit02() == null) {
            return true;
        }

        String bit02Value = isoModel.getBit02().getValue();
        boolean isBit02Empty = bit02Value == null || bit02Value.isEmpty();
        boolean isAuxValidation = isBit02Empty && isoModel.getBit03() == null;

        return isBit02Empty && !isAuxValidation;
    }

    private void processarTransacao(ISOModel isoModel) {
        if (!isProcessavel(isoModel)) {
            throw new IllegalArgumentException("Transação inválida para processamento");
        }

        LOGGER.info("Salvando transação com Bit02: {}", isoModel.getBit02().getValue());
        salvarTransacao(isoModel);
    }

    private boolean isProcessavel(ISOModel isoModel) {
        return isoModel.getBit03() != null &&
            isoModel.getBit04() != null &&
            isoModel.getBit05() != null &&
            isoModel.getBit12() != null;
    }

    private void salvarTransacao(ISOModel isoModel) {
        LOGGER.info("Transação salva com sucesso: Bit02 = {}", isoModel.getBit02().getValue());
    }
}

```

## 2. Segunda parte - Aplicação

A segunda parte é construir uma pequena aplicação segundo os seguintes requisitos:

Stack a ser utilizada no teste:
- Spring Boot 
- Apache Kafka
- MongoDB
- Java 17
- Maven

Para a construção do Microsserviço deve ser utilizada a arquitetura Hexagonal.

A partir do arquivo docker-compose fornecido crie um pequeno sistema de pedidos que irá funcionar de forma assíncrona. Utilize Spring Boot, Java 17 e Maven.
Considere que o sistema terá duas partes, uma responsável pela criação e consulta de pedidos e outra que será responsável pelo processamento do pedido e envio dos dados para a transportadora que enviará o pedido ao cliente.

O sistema deve conter endpoints REST para criação e também para consulta do STATUS de um pedido. Os possíveis status são AGUARDANDO_ENVIO e ENVIADO_TRANSPORTADORA

Assim que um pedido é efetuado, os dados do pedido devem ser gravados em uma collection no Mongo com o status AGUARDANDO_ENVIO
e uma mensagem deve ser postada em um tópico Kafka informando que um novo pedido foi efetuado.

Deve existir um consumidor para esse tópico Kafka que seja capaz de ler os dados, realizar a busca desse pedido no Mongo e alterar o status do mesmo para ENVIADO_TRANSPORTADORA.

Ao consultar o pedido através do endpoint de consulta, ele deve retornar o pedido com o status devidamente atualizado.

Para fins de simplificação tanto o producer quanto o consumer podem ser implementados em um único Microsserviço.

### **Como executar**

1. Clone o repositório:
   ```bash
   docker-compose up -d
   ```
2. Compile e inicie o projeto:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

3. Acesse os endpoints REST:
- **Criação de Pedido**:
  ```http
  POST http://localhost:8080/api/pedidos
  Content-Type: application/json
  {
    "descricao": "Produto A"
  }
  ```
- **Consulta de Pedido**:
  ```http
  GET http://localhost:8080/api/pedidos/{id}
  ```
