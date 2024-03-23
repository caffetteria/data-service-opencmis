# data-service-opencmis

[![Keep a Changelog v1.1.0 badge](https://img.shields.io/badge/changelog-Keep%20a%20Changelog%20v1.1.0-%23E05735)](CHANGELOG.md)

Semplice implementazione di un 
[Data Service Client](https://github.com/fugerit-org/fj-service-helper-bom/tree/main/data-service-base)
che si interfaccia con un server 
[OpenCMIS](https://chemistry.apache.org/java/developing/dev-server.html).

## Quickstart

1. Definire le proprietà di configurazione : 

```
testconfig.objectTypeId=cmis:document
testconfig.repositoryName = test
testconfig.bindingType = browser
testconfig.browserUrl = http://localhost:9000/cmis/browser
testconfig.user = user1
testconfig.password = cm1sp@ssword
testconfig.repositoryFolder = /
testconfig.setTitle = false
```

2. Creazioen e uso data service

```
        // esempio di configurazione 
        // nota : e' possibile creare implementazioni personalizzate di OpencmisDataServiceConfig
        Properties configProperties = ... caricamento configurazione ...
        String configNamespace = "testconfig.";
        OpencmisDataServiceConfig config = new OpencmisDataServiceConfigDefault( configNamespace, configProperties );
        // utilizzo
        OpencmisDataService dataService = OpencmisDataService.newDataService( config );
        String testString = "TEST";
        try (InputStream saveIs = new ByteArrayInputStream( testString.getBytes() ) ) {
            String dataId = dataService.save( saveIs );
            try (InputStream loadIs = dataService.load( dataId ) ) {
                String content = StreamIO.readString( loadIs );
                log.info( "check save/load result : {} - {}", testString, content );
            }
        }
```

## Parametri di configurazione

| parametero       | obbligatorio | default | note                                                |
|------------------|--------------|---------|-----------------------------------------------------|
| objectTypeId     | true         |         | object id CMIS                                      |
| repositoryName   | true         |         | nome del repository                                 |
| bindingType      | true         |         | tipo di binding                                     |
| browserUrl       | true         |         | obbligatorio se bindingType = browser               |
| user             | true         |         | obbligatorio se bindingType = browser               |
| password         | true         |         | obbligatorio se bindingType = browser               |
| repositoryFolder | true         |         |                                                     |
| setTitle         | false        | false   | se 'true' imposta l'attributo 'title' sui documenti |



