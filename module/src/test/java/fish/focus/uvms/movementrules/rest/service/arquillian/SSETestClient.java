/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package fish.focus.uvms.movementrules.rest.service.arquillian;

import fish.focus.schema.movementrules.ticket.v1.TicketType;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.SseEventSource;
import java.io.Closeable;

import static org.junit.Assert.fail;

public class SSETestClient extends BuildRulesRestDeployment implements Closeable {

    private final SseEventSource source;
    private TicketType ticket;

    private int firstTwoMessagesAreStrings = 2;
    private String failMessage = "";

    public SSETestClient() {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:8080/test/rest/sse/subscribe");
        AuthorizationHeaderWebTarget jwtTarget = new AuthorizationHeaderWebTarget(target, getTokenExternal());

        source = SseEventSource.target(jwtTarget).build();
        source.register(inbound -> {
            try {
                if (firstTwoMessagesAreStrings > 0) {
                    firstTwoMessagesAreStrings--;
                    var message = inbound.readData();
                    if (message == null) {
                        failMessage = "Got a null SSE message";
                        return;
                    }
                    // two "hello"-type of messages are sent when subscribing
                    if (!(message.matches("UVMS SSE Ticket notifications") || message.matches("User .* is now registered"))) {
                        failMessage = "Got unknown SSE Message";
                        return;
                    }
                    return;
                }

                ticket = inbound.readData(TicketType.class, MediaType.APPLICATION_JSON_TYPE);
            } catch (ProcessingException e) {
                failMessage = "Could not parse the SEE message";
            }
        });
        source.open();
    }

    public TicketType getTicketAndReset() {
        checkFailMessage();
        TicketType returnTicket = ticket;
        ticket = null;
        return returnTicket;
    }

    public TicketType getTicket() {
        checkFailMessage();
        return ticket;
    }

    private void checkFailMessage() {
        if (failMessage.isEmpty()) {
            return;
        }
        fail(failMessage);
    }

    @Override
    public void close() {
        if (source != null) {
            source.close();
        }
    }
}
