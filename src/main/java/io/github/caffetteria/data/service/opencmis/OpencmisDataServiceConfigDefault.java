package io.github.caffetteria.data.service.opencmis;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.Properties;

@Slf4j
public class OpencmisDataServiceConfigDefault implements OpencmisDataServiceConfig {

    private String configNamespace;

    private Properties configProperties;

    public OpencmisDataServiceConfigDefault(String configNamespace, Properties configProperties) {
        this.configNamespace = configNamespace;
        this.configProperties = configProperties;
    }

    public OpencmisDataServiceConfigDefault(Properties configProperties) {
        this( "", configProperties );
    }

    @Override
    public String getValue(String name) {
        String key = this.configNamespace+name;
        String value = this.configProperties.getProperty( this.configNamespace+name );
        log.debug( "key : {}, value : {}", key, value );
        return value;
    }

    @Override
    public Optional<String> getOptionalValue(String name) {
        String value = this.getValue( name );
        if ( value == null ) {
            return Optional.empty();
        } else {
            return Optional.of( value );
        }
    }
}
