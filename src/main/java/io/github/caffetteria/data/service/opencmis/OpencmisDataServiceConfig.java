package io.github.caffetteria.data.service.opencmis;

import java.util.Optional;

public interface OpencmisDataServiceConfig {

    String getValue( String name );

    Optional<String> getOptionalValue(String name );

}
