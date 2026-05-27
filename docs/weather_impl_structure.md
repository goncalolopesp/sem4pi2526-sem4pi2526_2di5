**Fluxo de classes para implementacao:
```txt
+--------------------------------------------------+
|                      <<UI>>                      |
|                  WeatherDataUI                   |
+--------------------------------------------------+
| + void show()                                    |
+--------------------------------------------------+
          |                               |
          | creates dto                   | sends file path
          v                               v

+--------------------------------------------------+
|                 WeatherDataDTO                   |
+--------------------------------------------------+
| - temperature : double                           |
| - humidity : double                              |
| - windSpeed : double                             |
| - timestamp : LocalDateTime                      |
+--------------------------------------------------+
          |                               |
          | passed to                     |
          v                               v

+--------------------------------------------------+
|                 <<Controller>>                   |
|              WeatherDataController               |
+--------------------------------------------------+
| + void registerWeatherData(WeatherDataDTO dto)   |
| + WeatherDataDTO importWeatherData(              |
|       String filePath, String fileType           |
|   )                                              |
+--------------------------------------------------+
          |                               |
          | sends dto                     | sends file path + file type
          v                               v

+--------------------------------------------------+
|                   <<Service>>                    |
|               WeatherDataService                 |
+--------------------------------------------------+
| + void registerWeatherData(WeatherDataDTO dto)   |
| + WeatherDataDTO importWeatherData(              |
|       String filePath, String fileType           |
|   )                                              |
+--------------------------------------------------+
                                          |
                                          | uses only for file import
                                          v

+--------------------------------------------------+
|                   <<Factory>>                    |
|           WeatherDataImporterFactory             |
+--------------------------------------------------+
| + WeatherDataImporter createImporter(            |
|       String fileType                            |
|   )                                              |
+--------------------------------------------------+
                         |
                         | creates
                         v

+--------------------------------------------------+
|                 <<Interface>>                    |
|              WeatherDataImporter                 |
+--------------------------------------------------+
| + boolean validate(String filePath)              |
| + WeatherDataDTO execute(String filePath)        |
+--------------------------------------------------+
                   ^                         ^
                   | implements              | implements
                   |                         |
   +--------------------------------+   +--------------------------------+
   |     WeatherDataCSVImporter     |   |    WeatherDataJSONImporter     |
   +--------------------------------+   +--------------------------------+
   | + boolean validate(            |   | + boolean validate(            |
   |     String filePath)           |   |     String filePath)           |
   | + WeatherDataDTO execute(      |   | + WeatherDataDTO execute(      |
   |     String filePath)           |   |     String filePath)           |
   +--------------------------------+   +--------------------------------+
```

O importer tem 2 exemplos CSV e JSON, mas podem ser adicionados outros tipos de ficheiros no futuro sem impactar o resto do sistema, seguindo o princípio de aberto/fechado.

Fluxo por defeito:

```txt
Manual input:
UI -> WeatherDataDTO -> Controller -> Service

File input:
UI -> filePath + fileType -> Controller -> Service -> Factory -> Importer -> WeatherDataDTO -> Service
```

Notas de implementacao:
- O importador CSV suporta o ficheiro de vento (colunas ACA, Alt_inf, Alt_Sup, Direction, Value, Day, Start, End).
- O importador XLSX suporta a mesma estrutura do CSV, procurando a linha de cabecalho para mapear colunas.
- Se o ACA nao existir, o sistema tenta cria-lo a partir do ficheiro ACA correspondente.
- A altitude e convertida de feet para metros (media entre Alt_inf e Alt_Sup).
- Temperatura, pressao e humidade sao assumidas com valores de referencia ate estarem disponiveis no ficheiro.
- Registos invalidos sao reportados com a linha de origem e nao sao persistidos.
