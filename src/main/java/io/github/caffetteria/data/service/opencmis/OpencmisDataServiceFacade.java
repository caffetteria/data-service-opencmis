package io.github.caffetteria.data.service.opencmis;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.chemistry.opencmis.client.SessionFactoryFinder;
import org.apache.chemistry.opencmis.client.SessionParameterMap;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.util.ContentStreamUtils;
import org.apache.chemistry.opencmis.client.util.OperationContextUtils;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.fugerit.java.core.cfg.ConfigRuntimeException;
import org.fugerit.java.core.db.dao.DAORuntimeException;
import org.fugerit.java.core.function.SafeFunction;
import org.fugerit.java.core.io.StreamIO;
import org.fugerit.java.core.lang.helpers.BooleanUtils;
import org.fugerit.java.core.lang.helpers.StringUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class OpencmisDataServiceFacade {

    private SessionParameterMap cmisParameters = null;

    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    private String myRepositoryFolder = null;

    private static SessionFactory sessionfactory = null;

    private final String documentumObjectTypeId;

    private final boolean setTitle;

    public static final String KEY_OBJECT_TYPE_ID = "objectTypeId";

    public static final String KEY_BROWSER_URL = "browserUrl";

    public static final String KEY_BINDING_TYPE = "bindingType";

    public static final String KEY_USER = "user";

    public static final String KEY_PASSWORD = "password";

    public static final String KEY_REPOSITORY_FOLDER = "repositoryFolder";

    public static final String KEY_REPOSITORY_NAME = "repositoryName";

    public static final String FLAG_SET_TITLE = "setTitle";

    public OpencmisDataServiceFacade(OpencmisDataServiceConfig config) {
        log.debug("init CmisClientFacade : {}", config);
        this.cmisParameters = setupCmisBinding(config);
        this.setMyRepositoryFolder(config.getValue(KEY_REPOSITORY_FOLDER));
        this.documentumObjectTypeId = config.getValue(KEY_OBJECT_TYPE_ID);
        this.setTitle = BooleanUtils.isTrue(config.getValue(FLAG_SET_TITLE));
    }

    public byte[] load(String id) {
        log.debug("opencmis data service load : {}", id);
        OperationContext opCtxMin = OperationContextUtils.createMinimumOperationContext("cmis:objectId");
        Session session = this.getCmisRepositorySession();
        ObjectId cmisDocId = session.getObject(id, opCtxMin);
        ContentStream cmisStream = session.getContentStream(cmisDocId);
        return SafeFunction.get(() -> StreamIO.readBytes(cmisStream.getStream()));
    }

    public String save(String fileName, byte[] file) {
        log.debug("Start CMIS save : {}", fileName);
        Session session = this.getCmisRepositorySession();
        ContentStream cmisLocalContentStream = ContentStreamUtils.createByteArrayContentStream(fileName, file);
        return DAORuntimeException.get(() -> {
            try (InputStream is = cmisLocalContentStream.getStream()) {
                Map<String, Object> properties = new HashMap<>();
                properties.put(PropertyIds.NAME, cmisLocalContentStream.getFileName());
                if (this.setTitle) {
                    properties.put("title", cmisLocalContentStream.getFileName());
                }
                properties.put(PropertyIds.OBJECT_TYPE_ID, this.documentumObjectTypeId);
                OperationContext opCtxMin = OperationContextUtils.createMinimumOperationContext("cmis:objectId");
                Folder cmisFolder = (Folder) session.getObjectByPath(this.getMyRepositoryFolder(), opCtxMin);
                ObjectId cmisDocId = session.createDocument(properties, cmisFolder, cmisLocalContentStream, VersioningState.NONE);
                String id = cmisDocId.getId();
                log.debug("End CMIS save : {} -> {}", id, fileName);
                return id;
            }
        });
    }

    private Session getCmisRepositorySession() {
        SessionFactory sessionFactory = getCmisSessionFactory();
        return sessionFactory.createSession(cmisParameters);
    }

    private synchronized SessionFactory getCmisSessionFactory() {
        if (sessionfactory == null) {
            sessionfactory = SafeFunction.get( SessionFactoryFinder::find );
        }
        return sessionfactory;
    }

    private void addParameter(SessionParameterMap cmisParameters, OpencmisDataServiceConfig config, String configParameterName) {
        this.addParameter( cmisParameters, config, configParameterName, configParameterName );
    }
    private void addParameter(SessionParameterMap cmisParameters, OpencmisDataServiceConfig config,  String configParameterName, String cmisParameterName) {
        String parameterValue = config.getValue( configParameterName );
        if (StringUtils.isEmpty( parameterValue ) ) {
            throw new IllegalArgumentException( String.format( "Missing required parameter : '%s'", configParameterName ) );
        } else {
            log.info( "set parameter : {} - {}", cmisParameterName, parameterValue );
            cmisParameters.put( cmisParameterName, parameterValue );
        }
    }

    private SessionParameterMap setupCmisBinding(OpencmisDataServiceConfig config) {
        SessionParameterMap sessionParameterMap = new SessionParameterMap();

        BindingType cmisBindingType;
        try {
            cmisBindingType = BindingType.fromValue(config.getValue(KEY_BINDING_TYPE));
        } catch (Exception t) {
            throw new IllegalArgumentException("Invalid " + KEY_BINDING_TYPE, t);
        }

        sessionParameterMap.put(SessionParameter.BINDING_TYPE, cmisBindingType.value());

        switch (cmisBindingType) {
            case ATOMPUB:
                this.addParameter( sessionParameterMap, config, SessionParameter.ATOMPUB_URL );
                break;
            case BROWSER:
                this.addParameter( sessionParameterMap, config, KEY_BROWSER_URL, SessionParameter.BROWSER_URL );
                break;
            case WEBSERVICES:
                this.addParameter( sessionParameterMap, config, SessionParameter.WEBSERVICES_REPOSITORY_SERVICE );
                this.addParameter( sessionParameterMap, config, SessionParameter.WEBSERVICES_NAVIGATION_SERVICE );
                this.addParameter( sessionParameterMap, config, SessionParameter.WEBSERVICES_OBJECT_SERVICE );
                this.addParameter( sessionParameterMap, config, SessionParameter.WEBSERVICES_VERSIONING_SERVICE );
                this.addParameter( sessionParameterMap, config, SessionParameter.WEBSERVICES_DISCOVERY_SERVICE );
                this.addParameter( sessionParameterMap, config, SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE );
                this.addParameter( sessionParameterMap, config, SessionParameter.WEBSERVICES_MULTIFILING_SERVICE );
                this.addParameter( sessionParameterMap, config, SessionParameter.WEBSERVICES_POLICY_SERVICE );
                this.addParameter( sessionParameterMap, config, SessionParameter.WEBSERVICES_ACL_SERVICE );
                break;
            default:
                throw new IllegalArgumentException("Invalid " + KEY_BINDING_TYPE);
        }

        this.addParameter( sessionParameterMap, config, KEY_REPOSITORY_NAME, SessionParameter.REPOSITORY_ID );

        String user = config.getValue(KEY_USER);
        String password = config.getValue(KEY_PASSWORD);
        sessionParameterMap.setBasicAuthentication(user, password);

        return sessionParameterMap;
    }

}
