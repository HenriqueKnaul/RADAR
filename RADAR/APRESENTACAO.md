# Apresentação do RADAR

1. **Introdução.** Apresente o RADAR como um organizador acadêmico Android para estudantes.
2. **Motivação.** Mostre como concentrar disciplinas, prazos e anotações facilita priorizar o trabalho da semana.
3. **Objetivo geral.** Registrar e acompanhar atividades acadêmicas no celular, com persistência local.
4. **Objetivos específicos.** Demonstre o cadastro de disciplinas e tarefas, os filtros, a conclusão de atividades, a foto anexada e a consulta de feriados.
5. **Desenvolvimento.** Apresente Kotlin, XML, Fragments com Bottom Navigation, ViewBinding e MVVM. Mostre Room para as atividades e Retrofit para a BrasilAPI.
6. **Dificuldades técnicas.** Explique o compartilhamento temporário do arquivo com a câmera, a consistência ao excluir tarefas vinculadas e o controle do listener e do intervalo do acelerômetro.
7. **Conclusão.** Mostre como o Painel reúne pendências e prazos, e como o app preserva os cadastros após fechar.
8. **Trabalhos futuros.** Considere alertas por prazo, integração com calendário e aviso quando uma entrega cai em feriado.

## Roteiro prático

| Etapa | Demonstração | Requisito |
| --- | --- | --- |
| 1 | Abra o **Painel** e percorra as quatro abas. | Fragments e Bottom Navigation |
| 2 | Em **Disciplinas**, abra **Nova disciplina**, escolha nome, professor e cor e salve. Abra **Detalhe da disciplina**. | Formulário XML, primeira RecyclerView com Adapter e Room |
| 3 | No detalhe, abra **Nova tarefa nesta disciplina**. Preencha título, tipo, prioridade, data e horário. Toque em **Fotografar anotação**, capture e confirme a imagem. Salve. | Cadastro vinculado e câmera |
| 4 | Na aba **Tarefas**, filtre por situação e disciplina. Abra o **Detalhe da tarefa**, confira a foto e alterne entre concluir e reabrir. Abra **Editar tarefa** para alterar os dados ou remover a foto. | Segunda RecyclerView com Adapter, CRUD e anexo |
| 5 | Feche e reabra o app. Confirme que a disciplina, a tarefa e a foto continuam disponíveis. | Persistência local com Room e arquivo privado |
| 6 | Em **Ajustes**, deixe **Agitar para atualizar o Painel** ativado. Volte ao Painel e agite o aparelho até ver **Painel atualizado!**. No emulador, use os controles de sensores virtuais para alternar dois picos de aceleração. | Sensor de aceleração, feedback e intervalo de segurança |
| 7 | Abra **Feriados nacionais**, troque o ano e consulte os resultados. Desconecte a rede, tente atualizar e observe a mensagem de erro. | Retrofit, carregamento e tratamento de falhas |
| 8 | Exclua uma tarefa de teste. Depois exclua uma disciplina de teste e confirme o aviso sobre as atividades e fotos associadas. | Exclusão e integridade dos dados |

## Correspondência com o protótipo versionado

| Protótipo | Tela implementada |
| --- | --- |
| 01 Painel | Resumo, próximos sete dias, acesso a feriados e atualização pelo acelerômetro |
| 02 Disciplinas | Lista, cadastro e acesso ao detalhe |
| 03 Nova disciplina | Mesmo formulário para criar e editar |
| 04 Detalhe da disciplina | Informações, ações e atividades vinculadas |
| 05 Tarefas | Lista geral com filtros |
| 06 Nova tarefa | Formulário de cadastro e edição com foto, data, horário e prioridade |
| 07 Detalhe da tarefa | Dados, foto, conclusão, edição e exclusão |
| 08 Feriados | Consulta de feriados nacionais por ano |
| 09 Ajustes | Preferência do gesto, acesso a feriados e informações do app |

A atualização por movimento substitui a abertura do cadastro por movimento prevista no protótipo. O cadastro segue acessível na aba Tarefas e no detalhe da disciplina. Os alertas por prazo previstos no protótipo ficaram como trabalho futuro. O requisito de dispositivo é demonstrado pela câmera e pelo acelerômetro.

## Checklist do T1

| Exigência | Evidência |
| --- | --- |
| Tema e requisitos funcionais | Organizador acadêmico e funcionalidades descritas no README |
| Protótipo e pelo menos seis telas | Nove telas mapeadas acima. Validação das telas com o professor a confirmar |
| Componentes gráficos | Campos, seletores, botões, checkbox, imagem, indicador de progresso, mensagens e diálogos |
| Duas listas em contextos distintos | Disciplinas e Tarefas com RecyclerView e ListAdapter |
| Navegação | Fragments e quatro abas de Bottom Navigation |
| Duas opções de integração ou persistência | Room e Retrofit |
| MVVM | Fragments, ViewModels, repositórios e fluxos observáveis |
| Dois recursos do aparelho | Câmera e acelerômetro |
| Apresentação e entrega | Roteiro acima e código-fonte compactado para o Moodle |
