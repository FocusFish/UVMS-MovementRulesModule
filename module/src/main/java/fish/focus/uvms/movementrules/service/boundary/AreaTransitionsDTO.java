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
package fish.focus.uvms.movementrules.service.boundary;

import java.util.List;
import fish.focus.uvms.spatial.model.schemas.AreaExtendedIdentifierType;
import fish.focus.uvms.spatial.model.schemas.SpatialEnrichmentRS;

public class AreaTransitionsDTO {

    private List<AreaExtendedIdentifierType> enteredAreas;
    private List<AreaExtendedIdentifierType> exitedAreas;
    private SpatialEnrichmentRS spatialEnrichmentRS;

    public List<AreaExtendedIdentifierType> getEnteredAreas() {
        return enteredAreas;
    }

    public void setEnteredAreas(List<AreaExtendedIdentifierType> enteredAreas) {
        this.enteredAreas = enteredAreas;
    }

    public List<AreaExtendedIdentifierType> getExitedAreas() {
        return exitedAreas;
    }

    public void setExitedAreas(List<AreaExtendedIdentifierType> exitedAreas) {
        this.exitedAreas = exitedAreas;
    }

    public SpatialEnrichmentRS getSpatialEnrichmentRS() {
        return spatialEnrichmentRS;
    }

    public void setSpatialEnrichmentRS(SpatialEnrichmentRS spatialEnrichmentRS) {
        this.spatialEnrichmentRS = spatialEnrichmentRS;
    }
}
