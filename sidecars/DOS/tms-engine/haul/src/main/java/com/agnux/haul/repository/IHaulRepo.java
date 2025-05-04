package com.agnux.haul.repository;

import com.agnux.haul.errors.TmsException;
import com.agnux.haul.repository.model.CargoAssignment;
import com.agnux.haul.repository.model.Vehicle;
import com.agnux.haul.repository.model.Agreement;
import com.agnux.haul.repository.model.Customer;
import com.agnux.haul.repository.model.Driver;
import com.agnux.haul.repository.model.Patio;
import com.agnux.haul.repository.model.TransLogRecord;
import java.util.UUID;

public interface IHaulRepo {

    public Agreement getAvailableAgreement(UUID agreementId) throws TmsException;

    public UUID createAgreement(Agreement c) throws TmsException;

    public UUID editAgreement(Agreement c) throws TmsException;

    public void deleteAgreement(UUID agreementId) throws TmsException;

    public CargoAssignment getAvailableCargoAssignment(UUID cargoAssignmentId) throws TmsException;

    public UUID createCargoAssignment(CargoAssignment t) throws TmsException;

    public UUID editCargoAssignment(CargoAssignment t) throws TmsException;

    public void deleteCargoAssignment(UUID cargoAssignmentId) throws TmsException;

    public TransLogRecord getAvailableTransLogRecord(UUID transLogRecordId) throws TmsException;

    public UUID createTransLogRecord(TransLogRecord tlr) throws TmsException;

    public Vehicle getAvailableVehicule(UUID vehicleId) throws TmsException;

    public UUID createVehicle(Vehicle v) throws TmsException;

    public UUID editVehicle(Vehicle v) throws TmsException;

    public void deleteVehicle(UUID vehicleId) throws TmsException;

    public Driver getAvailableDriver(UUID driverId) throws TmsException;

    public UUID createDriver(Driver v) throws TmsException;

    public UUID editDriver(Driver v) throws TmsException;

    public void deleteDriver(UUID driverId) throws TmsException;

    public Patio getAvailablePatio(UUID patioId) throws TmsException;

    public UUID createPatio(Patio p) throws TmsException;

    public UUID editPatio(Patio p) throws TmsException;

    public void deletePatio(UUID patioId) throws TmsException;

    public Customer getAvailableCustomer(UUID customerId) throws TmsException;

    public UUID createCustomer(Customer c) throws TmsException;

    public UUID editCustomer(Customer c) throws TmsException;

    public void deleteCustomer(UUID customerId) throws TmsException;
}
