package io.github.caffetteria.data.service.opencmis;

import lombok.extern.slf4j.Slf4j;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.fugerit.java.core.cfg.ConfigException;
import org.fugerit.java.core.io.StreamIO;
import org.fugerit.java.core.util.PropsIO;
import org.fugerit.java.simple.config.ConfigParams;
import org.fugerit.java.simple.config.ConfigParamsDefault;
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
class TestOpencmisService {

    private static final String CMIS_SERVER_DOCKER_IMAGE = "fugeritorg/opencmis:latest";
    private static final int CMIS_SERVER_EXPOSED_PORT = 8080;

    private static final String CMIS_SERVER_TEST_URL = "/cmis/services11/cmis?wsdl";

    private static final int CMIS_SERVER_STARTUP_TIMEOUT_SECONDS = 90;

    private static final String NAMESPACE_CONFIG = "testconfig.";

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
    void testDataServiceOpencmisRun() throws IOException, ConfigException {
        this.cmisServer.start();
        String address = this.cmisServer.getHost();
        Integer port = this.cmisServer.getFirstMappedPort();
        log.info( "cmis server -> {}:{}", address, port );
        Properties configProperties = PropsIO.loadFromClassLoader("config/opencmis_data_service_browser.properties");
        // override port
        String browserUrlKey = String.format( "%sbrowserUrl", NAMESPACE_CONFIG );
        String browserUrl = String.format( "http://%s:%s/cmis/browser", address, port );
        log.info( "cmis server browserUrl key:{}, value{}", browserUrlKey, browserUrl );
        configProperties.setProperty( browserUrlKey, browserUrl );
        // test browser configuration
        ConfigParams config = new ConfigParamsDefault( NAMESPACE_CONFIG, configProperties );
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

    @Test
    void testDataServiceOpencmisConfig() throws IOException, ConfigException {
        // ws configuration
        Properties configPropertiesWs = PropsIO.loadFromClassLoader("config/opencmis_data_service_ws.properties");
        ConfigParams configWs = new ConfigParamsDefault( NAMESPACE_CONFIG, configPropertiesWs );
        OpencmisDataService dataServiceWs = OpencmisDataService.newDataService( configWs );
        Assertions.assertNotNull( dataServiceWs );
        // check double configurations
        Assertions.assertThrows( ConfigException.class, () -> dataServiceWs.setup( configWs ) );
        // check invalid binding
        configPropertiesWs.setProperty( NAMESPACE_CONFIG+OpencmisDataServiceFacade.KEY_BINDING_TYPE, "unknown" );
        ConfigParams configWsFail = new ConfigParamsDefault( NAMESPACE_CONFIG, configPropertiesWs );
        OpencmisDataService dataServiceWsFail = new OpencmisDataService();
        Assertions.assertThrows(IllegalArgumentException.class, () -> dataServiceWsFail.setup( configWsFail ));
        // atom configuration
        Properties configPropertiesAtom = PropsIO.loadFromClassLoader("config/opencmis_data_service_atom.properties");
        ConfigParams configAtom = new ConfigParamsDefault( NAMESPACE_CONFIG, configPropertiesAtom );
        OpencmisDataService dataServiceAtom = OpencmisDataService.newDataService( configAtom );
        Assertions.assertNotNull( dataServiceAtom );
        // test missing
        configPropertiesAtom.remove( NAMESPACE_CONFIG+SessionParameter.ATOMPUB_URL );
        ConfigParams configAtomFail = new ConfigParamsDefault( NAMESPACE_CONFIG, configPropertiesAtom );
        OpencmisDataService dataServiceAtomFail = new OpencmisDataService();
        Assertions.assertThrows(IllegalArgumentException.class, () -> dataServiceAtomFail.setup( configAtomFail ));
    }

}
