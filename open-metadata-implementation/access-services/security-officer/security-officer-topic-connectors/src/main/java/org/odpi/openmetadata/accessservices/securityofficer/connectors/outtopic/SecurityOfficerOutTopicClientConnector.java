/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */

package org.odpi.openmetadata.accessservices.securityofficer.connectors.outtopic;

import org.odpi.openmetadata.accessservices.securityofficer.api.SecurityOfficerEventInterface;
import org.odpi.openmetadata.accessservices.securityofficer.api.SecurityOfficerEventListener;
import org.odpi.openmetadata.accessservices.securityofficer.api.events.SecurityOfficerEvent;
import org.odpi.openmetadata.accessservices.securityofficer.api.ffdc.SecurityOfficerErrorCode;
import org.odpi.openmetadata.frameworks.connectors.ffdc.*;
import org.odpi.openmetadata.repositoryservices.connectors.openmetadatatopic.OpenMetadataTopicListenerConnectorBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * SecurityOfficerOutTopicClientConnector is the java base class implementation of the
 * the client side connector that receives events from the Security Officer OMAS's OutTopic.
 */
public class SecurityOfficerOutTopicClientConnector extends OpenMetadataTopicListenerConnectorBase implements SecurityOfficerEventInterface
{
    private static final Logger log = LoggerFactory.getLogger(SecurityOfficerOutTopicClientConnector.class);

    private List<SecurityOfficerEventListener> internalEventListeners = new ArrayList<>();


    /**
     * Register a listener object that will be passed each of the events published by
     * the Security Officer OMAS.
     *
     * @param userId calling user
     * @param listener listener object
     *
     * @throws InvalidParameterException one of the parameters is null or invalid.
     */
    @Override
    public  synchronized void registerListener(String                       userId,
                                               SecurityOfficerEventListener listener) throws InvalidParameterException
    {
        final String methodName = "registerListener";
        final String parameterName = "listener";

        if (listener == null)
        {
            throw new InvalidParameterException(SecurityOfficerErrorCode.NULL_LISTENER.getMessageDefinition(userId, methodName),
                                                this.getClass().getName(),
                                                methodName,
                                                parameterName);
        }

        internalEventListeners.add(listener);
    }


    /**
     * Method to pass an event received on topic.
     *
     * @param event inbound event
     */
    @Override
    public synchronized void processEvent(String event)
    {
        if (event != null)
        {
            try
            {
                SecurityOfficerEvent eventObject = super.getEventBean(event, SecurityOfficerEvent.class);

                for (SecurityOfficerEventListener listener : internalEventListeners)
                {
                    try
                    {
                        listener.processEvent(eventObject);
                    }
                    catch (Exception error)
                    {
                        log.error("Listener: " + listener.getClass().getName() + " is unable to process event: " + event, error);
                    }
                }
            }
            catch (Exception error)
            {
                log.error("Unable to read event: " + event, error);
            }
        }
    }
}
