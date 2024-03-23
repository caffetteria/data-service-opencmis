package io.github.caffetteria.data.service.opencmis;

import org.fugerit.java.core.cfg.ConfigException;
import org.fugerit.java.core.io.StreamIO;
import org.fugerit.java.dsb.DataService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class OpencmisDataService implements DataService {

    private OpencmisDataServiceFacade facade;

    @Override
    public InputStream load(String id) throws IOException {
        return new ByteArrayInputStream(this.facade.load( id ));
    }

    @Override
    public String save(InputStream data) throws IOException {
        return this.facade.save(UUID.randomUUID().toString(), StreamIO.readBytes( data ));
    }

    public OpencmisDataService setup(OpencmisDataServiceConfig config ) throws ConfigException {
        if ( this.facade == null ) {
            this.facade = new OpencmisDataServiceFacade( config );
        } else {
            throw new ConfigException( "CmisDataServcice already configured!" );
        }
        return this;
    }

    public static OpencmisDataService newDataService(OpencmisDataServiceConfig config ) throws ConfigException {
        return new OpencmisDataService().setup( config );
    }

}
