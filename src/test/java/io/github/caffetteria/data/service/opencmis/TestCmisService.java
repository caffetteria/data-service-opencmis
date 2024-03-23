package io.github.caffetteria.data.service.opencmis;

import lombok.extern.slf4j.Slf4j;
import org.fugerit.java.core.cfg.ConfigException;
import org.fugerit.java.core.io.StreamIO;
import org.fugerit.java.core.util.PropsIO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

@Slf4j
class TestCmisService {

    private static final String CMIS_SERVER_DOCKER_IMAGE = "fugeritorg/opencmis:latest";
    private static final int CMIS_SERVER_EXPOSED_PORT = 8080;

    private static final String CMIS_SERVER_TEST_URL = "/cmis/services11/cmis?wsdl";

    private static final int CMIS_SERVER_STARTUP_TIMEOUT_SECONDS = 180;

    /*
     * See :
     * https://java.testcontainers.org/features/startup_and_waits/
     * https://github.com/caffetteria/cmis-server
     * https://hub.docker.com/repository/docker/fugeritorg/opencmis
     */
    @Container
    GenericContainer cmisServer = new GenericContainer(DockerImageName.parse(CMIS_SERVER_DOCKER_IMAGE))
            .waitingFor(Wait.forHttp(CMIS_SERVER_TEST_URL))
            .withExposedPorts(CMIS_SERVER_EXPOSED_PORT)
            .withStartupTimeout( Duration.ofSeconds( CMIS_SERVER_STARTUP_TIMEOUT_SECONDS ) );

    @Test
    void testDataServiceCmis() throws IOException, ConfigException {
        this.cmisServer.start();
        String address = this.cmisServer.getHost();
        Integer port = this.cmisServer.getFirstMappedPort();
        log.info( "cmis server -> {}:{}", address, port );
        String configNamespace = "testconfig.";
        Properties configProperties = PropsIO.loadFromClassLoader("config/opencmis_data_service_browser.properties");
        // override port
        String browserUrlKey = String.format( "%sbrowserUrl", configNamespace );
        String browserUrl = String.format( "http://%s:%s/cmis/browser", address, port );
        log.info( "cmis server browserUrl key:{}, value{}", browserUrlKey, browserUrl );
        configProperties.setProperty( browserUrlKey, browserUrl );
        OpencmisDataServiceConfig config = new OpencmisDataServiceConfigDefault( configNamespace, configProperties );
        OpencmisDataService dataService = OpencmisDataService.newDataService( config );
        String testString = "TEST";
        try (InputStream saveIs = new ByteArrayInputStream( testString.getBytes() ) ) {
            String dataId = dataService.save( saveIs );
            try (InputStream loadIs = dataService.load( dataId ) ) {
                String content = StreamIO.readString( loadIs );
                log.info( "check save/load result : {} - {}", testString, content );
                Assertions.assertEquals( testString, content );
            }
        }
        Assertions.assertNotNull( dataService );
    }

}
