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
package fish.focus.uvms.movementrules.longpolling.service;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import fish.focus.uvms.movementrules.longpolling.service.LongPollingHttpServlet;

public class TestLongPollingHttpServlet {

    LongPollingHttpServlet longPolling;
    
    @Before
    public void setup() {
        longPolling = new LongPollingHttpServlet();
    }

    @Test
    public void testNoAction() {
        String jsonMessage = longPolling.createJsonMessage("abc123");
        assertEquals("{\"ids\":[\"abc123\"]}", jsonMessage);
    }

    @Test
    public void testNoGuid() {
        String jsonMessage = longPolling.createJsonMessage(null);
        assertEquals("{\"ids\":[]}", jsonMessage);
    }

    @Test
    public void testUpdated() {
        String jsonMessage = longPolling.createJsonMessage("abc123", "updated");
        assertEquals("{\"updated\":{\"ids\":[\"abc123\"]}}", jsonMessage);
    }

    @Test
    public void testUpdatedNoGuid() {
        String jsonMessage = longPolling.createJsonMessage(null, "updated");
        assertEquals("{\"updated\":{\"ids\":[]}}", jsonMessage);
    }
    
    @Test
    public void testCountTrue() {
        String jsonMessage = longPolling.createJsonMessageCount(true);
        assertEquals("{\"updated\":true}", jsonMessage);
    }

    @Test
    public void testCountFalse() {
        String jsonMessage = longPolling.createJsonMessageCount(false);
        assertEquals("{\"updated\":false}", jsonMessage);
    }

}