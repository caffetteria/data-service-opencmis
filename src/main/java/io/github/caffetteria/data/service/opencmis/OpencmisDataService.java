package io.github.caffetteria.data.service.opencmis;

import org.fugerit.java.core.cfg.ConfigException;
import org.fugerit.java.core.io.StreamIO;
import org.fugerit.java.dsb.DataService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * DataService interface implementation based on a CMIS system.
 *
 */
public class OpencmisDataService implements DataService {

    private OpencmisDataServiceFacade facade;

    /**
     * Load a data stream from the CMIS.
     *
     * @param id            the id of the resource to be loaded
     * @return              the loaded resource data stream
     * @throws IOException  if Input/Output issues arise
     */
    @Override
    public InputStream load(String id) throws IOException {
        return new ByteArrayInputStream(this.facade.load( id ));
    }


    /**
     * Save a data stream in the CMIS, a UUID is generated as resource name.
     *
     * @param data          the data stream to be saved
     * @return              the id of the saved resources
     * @throws IOException if Input/Output issues arise
     */
    @Override
    public String save(InputStream data) throws IOException {
        return this.save( data, UUID.randomUUID().toString() );
    }

    /**
     * Save a data stream in the CMIS, with the given resource name.
     *
     * @param data          the data stream to be saved
     * @param resourceName  the name of the resource to be saved
     * @return              the id of the saved resource
     * @throws IOException if Input/Output issues arise
     */
    @Override
    public String save(InputStream data, String resourceName) throws IOException {
        return this.facade.save( resourceName, StreamIO.readBytes( data ) );
    }

    /**
     * Setup this DataService, based on a OpencmisDataServiceConfig.
     *
     * Can be invoked only one on any give instance.
     *
     * @param config             the configuration
     * @return                   the self configured instance
     * @throws ConfigException   if configuration issues arise
     */
    public OpencmisDataService setup( OpencmisDataServiceConfig config ) throws ConfigException {
        if ( this.facade == null ) {
            this.facade = new OpencmisDataServiceFacade( config );
        } else {
            throw new ConfigException( "CmisDataServcice already configured!" );
        }
        return this;
    }

    /**
     * DataService factory method, based on a OpencmisDataServiceConfig.
     *
     * @param config            the configuration
     * @return                  the self configured instance
     * @throws ConfigException  if configuration issues arise
     */
    public static OpencmisDataService newDataService(OpencmisDataServiceConfig config ) throws ConfigException {
        return new OpencmisDataService().setup( config );
    }

}
