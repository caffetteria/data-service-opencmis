package io.github.caffetteria.data.service.opencmis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

class TestOpencmisConfig {

    @Test
    void testConfigDefault() throws IOException {
        Properties configProperties = new Properties();
        String userTest = "user1";
        configProperties.setProperty( OpencmisDataServiceFacade.KEY_USER, userTest );
        OpencmisDataServiceConfig config = new OpencmisDataServiceConfigDefault( configProperties );
        Optional<String> testFound = config.getOptionalValue( OpencmisDataServiceFacade.KEY_USER );
        Optional<String> testNotFound = config.getOptionalValue( "notFound" );
        Assertions.assertEquals( userTest, testFound.get() );
        Assertions.assertFalse( testNotFound.isPresent() );
    }

}
