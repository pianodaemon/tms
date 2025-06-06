package com.agnux.tms.core.mgmt;

import com.agnux.tms.errors.ErrorCodes;
import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.model.Agreement;
import com.agnux.tms.repository.model.CargoAssignment;
import com.agnux.tms.repository.model.Customer;
import com.agnux.tms.repository.IHaulRepo;
import com.agnux.tms.repository.model.TransLogRecord;
import com.agnux.tms.repository.model.Vehicle;
import com.agnux.tms.reference.qualitative.VehicleColor;
import com.agnux.tms.reference.qualitative.VehicleType;
import com.agnux.tms.reference.quantitative.VolUnit;
import com.agnux.tms.reference.quantitative.DistUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
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
    private Customer customer;
    private final UUID driverUuid = UUID.fromString("4a232802-d6e8-458f-9eca-6a8c2b980979");
    private final UUID vehicleUuid = UUID.fromString("4a232802-d6e8-458f-9eca-6a8c2b980980");
    private final UUID agreementUuid = UUID.fromString("4a232802-d6e8-458f-9eca-6a8c2b980981");
    private final UUID tenantUuid = UUID.fromString("4a232802-d6e8-458f-9eca-6a8c2b980982");
    private final UUID customerUuid = UUID.fromString("4a232802-d6e8-458f-9eca-6a8c2b980983");
    private final UUID cargorUuid = UUID.fromString("4a232802-d6e8-458f-9eca-6a8c2b980984");
    private final UUID transLogRecordUuid = UUID.fromString("4a232802-d6e8-458f-9eca-6a8c2b980985");

    @BeforeEach
    void setUpData() {

        tenantDetails = new TenantDetailsDto(tenantUuid, "gerald");
        customer = new Customer(customerUuid, tenantUuid, "quintanilla");
        tripDetails = new TripDetailsDto(vehicleUuid, agreementUuid, driverUuid);
        agreement = new Agreement(agreementUuid, tenantUuid, customerUuid, "Soriana", 0, 0, 0, 0, DistUnit.KM, new BigDecimal("100"));
        ship = new Vehicle(vehicleUuid, tenantDetails.getTenantId(), "GAS9500", new Date(),"AXD000000001", 1, VehicleType.CAR, VehicleColor.LIGHT_BLUE, 1980, new Date(), "VL", DistUnit.KM, VolUnit.LT, BigDecimal.ZERO);
    }

    @Test
    void assignTrip_ShouldReturnCargoId_WhenDataIsValid() throws TmsException {
        // Arrange
        when(repo.getVehicle(vehicleUuid)).thenReturn(ship);
        when(repo.getAgreement(agreementUuid)).thenReturn(agreement);
        when(repo.createCargoAssignment(any(CargoAssignment.class))).thenReturn(cargorUuid);
        when(repo.createTransLogRecord(any(TransLogRecord.class))).thenReturn(transLogRecordUuid);

        // Act
        UUID cargoId = haulMgmt.assignTrip(tenantDetails, tripDetails);

        // Assert
        assertEquals(cargorUuid, cargoId);

        // Verify methods called
        verify(repo).getVehicle(vehicleUuid);

        ArgumentCaptor<CargoAssignment> assignmentCaptor = ArgumentCaptor.forClass(CargoAssignment.class);
        verify(repo).createCargoAssignment(assignmentCaptor.capture());

        CargoAssignment capturedAssignment = assignmentCaptor.getValue();
        assertEquals(tenantUuid, capturedAssignment.getTenantId());
        assertEquals(ship.getId().get(), capturedAssignment.getVehicleId());
    }

    @Test
    void assignTrip_ShouldThrowTmsException_WhenTenantMismatch() throws TmsException {
        // Arrange
        Vehicle mismatchedVehicle = new Vehicle(vehicleUuid, UUID.fromString("0a232802-d6e8-458f-9eca-6a8c2b980900"), "GAS9500", new Date(),"AXD000000001", 1,VehicleType.CAR, VehicleColor.ORANGE, 1980, new Date(),"VL", DistUnit.KM, VolUnit.LT, BigDecimal.ZERO);
        when(repo.getVehicle(vehicleUuid)).thenReturn(mismatchedVehicle);

        // Act & Assert
        TmsException ex = assertThrows(TmsException.class, ()
                -> haulMgmt.assignTrip(tenantDetails, tripDetails)
        );

        assertEquals(ErrorCodes.LACK_OF_DATA_INTEGRITY.getCode(), ex.getErrorCode());
        assertTrue(ex.getMessage().contains("does not pertain to tenant"));
    }
}
