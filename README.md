# data-service-opencmis

[![Keep a Changelog v1.1.0 badge](https://img.shields.io/badge/changelog-Keep%20a%20Changelog%20v1.1.0-%23E05735)](CHANGELOG.md)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.caffetteria/data-service-opencmis.svg)](https://central.sonatype.com/artifact/io.github.caffetteria/data-service-opencmis)
[![license](https://img.shields.io/badge/License-MIT%20License-teal.svg)](https://opensource.org/license/mit)
[![code of conduct](https://img.shields.io/badge/conduct-Contributor%20Covenant-purple.svg)](https://github.com/fugerit-org/fj-universe/blob/main/CODE_OF_CONDUCT.md)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=caffetteria_data-service-opencmis&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=caffetteria_data-service-opencmis)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=caffetteria_data-service-opencmis&metric=coverage)](https://sonarcloud.io/summary/new_code?id=caffetteria_data-service-opencmis)

[![Java runtime version](https://img.shields.io/badge/run%20on-java%208+-%23113366.svg?style=for-the-badge&logo=openjdk&logoColor=white)](https://universe.fugerit.org/src/docs/versions/java11.html)
[![Java build version](https://img.shields.io/badge/build%20on-java%2011+-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)](https://universe.fugerit.org/src/docs/versions/java11.html)
[![Apache Maven](https://img.shields.io/badge/Apache%20Maven-3.9.0+-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white)](https://universe.fugerit.org/src/docs/versions/maven3_9.html)

Semplice implementazione di un 
[Data Service Client](https://github.com/fugerit-org/fj-service-helper-bom/tree/main/data-service-base)
che si interfaccia con un server 
[OpenCMIS](https://chemistry.apache.org/java/developing/dev-server.html).

![Data Service OpenCMIS](src/main/docs/dso_logo.jpg "Data Service OpenCMIS")

## Quickstart

1. Aggiungere dipendenza : 

```xml
<dependency>
    <groupId>io.github.caffetteria</groupId>
    <artifactId>data-service-opencmis</artifactId>
    <version>${data-service-opencmis-version}</version>
</dependency>
```

2. Definire le proprietà di configurazione : 

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

3. Creazione e uso data service

```
        // esempio di configurazione 
        // nota : e' possibile creare implementazioni personalizzate di OpencmisDataServiceConfig
        Properties configProperties = ... caricamento configurazione ...
        String configNamespace = "testconfig.";
        ConfigParams config = new ConfigParamsDefault( configNamespace, configProperties );
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


## OpenCMIS test server

Il progetto è stato testato con l'immagine docker : 

<https://hub.docker.com/repository/docker/fugeritorg/opencmis/general>

Basata sul repository : 

<https://github.com/caffetteria/cmis-server>
