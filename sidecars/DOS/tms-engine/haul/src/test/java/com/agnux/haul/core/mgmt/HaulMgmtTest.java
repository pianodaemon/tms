package com.agnux.haul.core.mgmt;

import com.agnux.haul.errors.ErrorCodes;
import com.agnux.haul.errors.TmsException;
import com.agnux.haul.repositories.Agreement;
import com.agnux.haul.repositories.CargoAssignment;
import com.agnux.haul.repositories.DistUnit;
import com.agnux.haul.repositories.IHaulRepo;
import com.agnux.haul.repositories.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class HaulMgmtTest {

    @Mock
    private IHaulRepo repo;

    @InjectMocks
    private HaulMgmt haulMgmt;

    private TenantDetailsDto tenantDetails;
    private TripDetailsDto tripDetails;
    private Vehicle ship;
    private Agreement agreement;

    @BeforeEach
    void setUpData() {
        tenantDetails = new TenantDetailsDto("tenant001", "gerald");
        tripDetails = new TripDetailsDto("ship001", "agreement001");
        agreement = new Agreement("agreement001", "tenant001", "customer001", 0, 0, 0, 0, DistUnit.KM, new BigDecimal("100"));
        ship = new Vehicle("ship001", tenantDetails.getTenantId());
    }

    @Test
    void assignTrip_ShouldReturnCargoId_WhenDataIsValid() throws TmsException {
        // Arrange
        when(repo.getAvailableVehicule("ship001")).thenReturn(ship);
        when(repo.getAvailableAgreement("agreement001")).thenReturn(agreement);
        when(repo.createCargoAssignment(any(CargoAssignment.class))).thenReturn("cargo001");

        // Act
        String cargoId = haulMgmt.assignTrip(tenantDetails, tripDetails);

        // Assert
        assertEquals("cargo001", cargoId);

        // Verify methods called
        verify(repo).getAvailableVehicule("ship001");

        ArgumentCaptor<CargoAssignment> assignmentCaptor = ArgumentCaptor.forClass(CargoAssignment.class);
        verify(repo).createCargoAssignment(assignmentCaptor.capture());

        CargoAssignment capturedAssignment = assignmentCaptor.getValue();
        assertEquals("tenant001", capturedAssignment.getTenantId());
        assertEquals(ship, capturedAssignment.getVehicle());
        assertNotNull(capturedAssignment.getTlRecord());
    }

    @Test
    void assignTrip_ShouldThrowTmsException_WhenTenantMismatch() throws TmsException {
        // Arrange
        Vehicle mismatchedVehicle = new Vehicle("ship001", "other-tenant");
        when(repo.getAvailableVehicule("ship001")).thenReturn(mismatchedVehicle);

        // Act & Assert
        TmsException ex = assertThrows(TmsException.class, ()
                -> haulMgmt.assignTrip(tenantDetails, tripDetails)
        );

        assertEquals(ErrorCodes.LACKOF_DATA_INTEGRITY.getCode(), ex.getErrorCode());
        assertTrue(ex.getMessage().contains("does not pertain to tenant"));
    }
}
