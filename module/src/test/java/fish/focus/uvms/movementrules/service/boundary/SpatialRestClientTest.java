package fish.focus.uvms.movementrules.service.boundary;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import fish.focus.schema.movementrules.movement.v1.MovementSourceType;
import fish.focus.uvms.spatial.model.schemas.SpatialEnrichmentRS;
import fish.focus.uvms.movementrules.model.dto.MovementDetails;
import fish.focus.uvms.movementrules.service.boundary.AreaTransitionsDTO;
import fish.focus.uvms.movementrules.service.boundary.SpatialRestClient;

@RunWith(MockitoJUnitRunner.class)
public class SpatialRestClientTest {

    @Test
    public void populateAreasAndAreaTransitionsAISPosition() {
        MovementDetails movementDetails = new MovementDetails();
        movementDetails.setLongitude(1d);
        movementDetails.setLatitude(1d);
        movementDetails.setSource(MovementSourceType.AIS.value());

        SpatialRestClient spatialRestSpy = Mockito.spy(new SpatialRestClient());
        Mockito.doReturn(getAreaTransitionsDto()).when(spatialRestSpy).getEnrichmentAndTransitions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        spatialRestSpy.populateAreasAndAreaTransitions(movementDetails);

        Mockito.verify(spatialRestSpy, Mockito.times(1)).getEnrichmentAndTransitions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void populateAreasAndAreaTransitionsNAFPosition() {
        MovementDetails movementDetails = new MovementDetails();
        movementDetails.setLongitude(1d);
        movementDetails.setLatitude(1d);
        movementDetails.setSource(MovementSourceType.NAF.value());

        SpatialRestClient spatialRestSpy = Mockito.spy(new SpatialRestClient());
        Mockito.doReturn(getAreaTransitionsDto()).when(spatialRestSpy).getEnrichmentAndTransitions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        spatialRestSpy.populateAreasAndAreaTransitions(movementDetails);

        Mockito.verify(spatialRestSpy, Mockito.times(1)).getEnrichmentAndTransitions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void populateAreasAndAreaTransitionsNAFIdenticalPreviousPosition() {
        MovementDetails movementDetails = new MovementDetails();
        movementDetails.setLongitude(1d);
        movementDetails.setLatitude(1d);
        movementDetails.setPreviousVMSLongitude(1d);
        movementDetails.setPreviousVMSLatitude(1d);
        movementDetails.setSource(MovementSourceType.NAF.value());

        SpatialRestClient spatialRestSpy = Mockito.spy(new SpatialRestClient());
        Mockito.doReturn(getAreaTransitionsDto()).when(spatialRestSpy).getEnrichmentAndTransitions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        spatialRestSpy.populateAreasAndAreaTransitions(movementDetails);

        Mockito.verify(spatialRestSpy, Mockito.times(1)).getEnrichmentAndTransitions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void populateAreasAndAreaTransitionsNAFDifferentPreviousPosition() {
        MovementDetails movementDetails = new MovementDetails();
        movementDetails.setLongitude(1d);
        movementDetails.setLatitude(1d);
        movementDetails.setPreviousVMSLongitude(2d);
        movementDetails.setPreviousVMSLatitude(2d);
        movementDetails.setSource(MovementSourceType.NAF.value());

        SpatialRestClient spatialRestSpy = Mockito.spy(new SpatialRestClient());
        Mockito.doReturn(getAreaTransitionsDto()).when(spatialRestSpy).getEnrichmentAndTransitions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        spatialRestSpy.populateAreasAndAreaTransitions(movementDetails);

        Mockito.verify(spatialRestSpy, Mockito.times(2)).getEnrichmentAndTransitions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    private AreaTransitionsDTO getAreaTransitionsDto() {
        AreaTransitionsDTO areaTransitions = new AreaTransitionsDTO();
        SpatialEnrichmentRS spatialEnrichmentRS = new SpatialEnrichmentRS();
        areaTransitions.setSpatialEnrichmentRS(spatialEnrichmentRS);
        areaTransitions.setEnteredAreas(new ArrayList<>());
        areaTransitions.setExitedAreas(new ArrayList<>());
        return areaTransitions;
    }

}
