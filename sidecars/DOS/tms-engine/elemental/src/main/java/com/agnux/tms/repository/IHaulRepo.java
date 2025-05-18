package com.agnux.tms.repository;

import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.model.CargoAssignment;
import com.agnux.tms.repository.model.Vehicle;
import com.agnux.tms.repository.model.Agreement;
import com.agnux.tms.repository.model.Customer;
import com.agnux.tms.repository.model.Driver;
import com.agnux.tms.repository.model.Patio;
import com.agnux.tms.repository.model.TransLogRecord;
import java.util.UUID;

public interface IHaulRepo {

    public Agreement getAgreement(UUID agreementId) throws TmsException;

    public UUID createAgreement(Agreement c) throws TmsException;

    public UUID editAgreement(Agreement c) throws TmsException;

    public void deleteAgreement(UUID agreementId) throws TmsException;

    public CargoAssignment getCargoAssignment(UUID cargoAssignmentId) throws TmsException;

    public UUID createCargoAssignment(CargoAssignment t) throws TmsException;

    public UUID editCargoAssignment(CargoAssignment t) throws TmsException;

    public void deleteCargoAssignment(UUID cargoAssignmentId) throws TmsException;

    public TransLogRecord getTransLogRecord(UUID transLogRecordId) throws TmsException;

    public UUID createTransLogRecord(TransLogRecord tlr) throws TmsException;

    public Vehicle getVehicle(UUID vehicleId) throws TmsException;

    public UUID createVehicle(Vehicle v) throws TmsException;

    public UUID editVehicle(Vehicle v) throws TmsException;

    public void deleteVehicle(UUID vehicleId) throws TmsException;

    public Driver getDriver(UUID driverId) throws TmsException;

    public UUID createDriver(Driver v) throws TmsException;

    public UUID editDriver(Driver v) throws TmsException;

    public void deleteDriver(UUID driverId) throws TmsException;

    public Patio getPatio(UUID patioId) throws TmsException;

    public UUID createPatio(Patio p) throws TmsException;

    public UUID editPatio(Patio p) throws TmsException;

    public void deletePatio(UUID patioId) throws TmsException;

    public Customer getCustomer(UUID customerId) throws TmsException;

    public UUID createCustomer(Customer c) throws TmsException;

    public UUID editCustomer(Customer c) throws TmsException;

    public void deleteCustomer(UUID customerId) throws TmsException;
}
