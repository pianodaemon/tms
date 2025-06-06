package com.agnux.tms.repository;

import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.model.*;

import java.util.Map;
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

    public PaginationSegment<Patio> listPatioPage(Map<String, String> filters, Map<String, String> pageParams) throws TmsException;

    public PaginationSegment<Driver> listDriverPage(Map<String, String> filters, Map<String, String> pageParams) throws TmsException;

    public PaginationSegment<Customer> listCustomerPage(Map<String, String> filters, Map<String, String> pageParams) throws TmsException;

    public PaginationSegment<Agreement> listAgreementPage(Map<String, String> filters, Map<String, String> pageParams) throws TmsException;

    public PaginationSegment<Vehicle> listVehiclePage(Map<String, String> filters, Map<String, String> pageParams) throws TmsException;

    public Customer getCustomer(UUID customerId) throws TmsException;

    public UUID createCustomer(Customer c) throws TmsException;

    public UUID editCustomer(Customer c) throws TmsException;

    public void deleteCustomer(UUID customerId) throws TmsException;

    public PaginationSegment<Box> listBoxPage(Map<String, String> filters, Map<String, String> pageParams) throws TmsException;

    public Box getBox(UUID BoxId) throws TmsException;

    public UUID createBox(Box c) throws TmsException;

    public UUID editBox(Box c) throws TmsException;

    public void deleteBox(UUID BoxId) throws TmsException;
}
