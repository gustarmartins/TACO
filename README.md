Este é um aplicativo Android desenvolvido em Kotlin com Jetpack Compose, utilizando a Tabela Brasileira de Composição de Alimentos (TACO) como sua fonte de dados principal.

O projeto foi desenvolvido como uma jornada de aprendizado e aplicação de conceitos modernos de desenvolvimento Android, desde a arquitetura do aplicativo até a construção de uma interface de usuário reativa e funcional, com a assistência de uma IA parceira de codificação do Google.

✨ Funcionalidades ✨
O aplicativo permite que os usuários explorem e utilizem os dados da Tabela TACO de maneira intuitiva e poderosa. As funcionalidades atuais e planejadas incluem:

📊 Consulta Rápida de Alimentos: Uma busca eficiente para encontrar qualquer um dos ~600 alimentos da base de dados.
🔬 Visualização Detalhada de Nutrientes: Ao selecionar um alimento, o usuário pode ver todos os seus nutrientes.
⚖️ Calculadora de Porção Customizada: Na tela de detalhes, o usuário pode inserir uma quantidade em gramas e ver todos os valores nutricionais serem recalculados instantaneamente para aquela porção específica.
🥗 Planejamento de Dietas:
Criação e listagem de múltiplas dietas personalizadas.
Adição de alimentos (buscados da base de dados) a uma dieta, especificando a quantidade de cada um.
Cálculo automático e exibição dos totais de calorias e macronutrientes da dieta.
🗓️ Diário Alimentar (Em Desenvolvimento): Funcionalidade futura para registrar o que foi consumido em um determinado dia.

Pré-população do Banco de Dados:
Um script Python personalizado foi desenvolvido para ler, limpar e mesclar os múltiplos arquivos CSV originais da Tabela TACO.
O script gera um arquivo taco_preload.sql que é usado pelo RoomDatabase.Callback para popular eficientemente o banco de dados na primeira inicialização do aplicativo.


📝 Agradecimentos
Agradecimentos ao NEPA-UNICAMP pela disponibilização dos dados da Tabela Brasileira de Composição de Alimentos (TACO), que são a base deste aplicativo.
Este projeto foi desenvolvido com a assistência de uma IA parceira de codificação do Google.
📄 Licença
Este projeto está licenciado sob a Licença MIT. Veja o arquivo LICENSE para mais detalhes.
