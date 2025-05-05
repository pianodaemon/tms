package com.agnux.tms.repository;

import com.agnux.tms.errors.ErrorCodes;
import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.model.Agreement;
import com.agnux.tms.repository.model.CargoAssignment;
import com.agnux.tms.repository.model.Customer;
import com.agnux.tms.repository.model.Driver;
import com.agnux.tms.repository.model.Patio;
import com.agnux.tms.repository.model.TransLogRecord;
import com.agnux.tms.repository.model.Vehicle;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import javax.sql.DataSource;

@AllArgsConstructor
public class BasicRepoImpl implements IHaulRepo {

    @NonNull
    private DataSource ds;

    @NonNull
    private Boolean debugMode;

    @Override
    public CargoAssignment getCargoAssignment(UUID cargoAssignmentId) throws TmsException {
        try {
            Optional<CargoAssignment> customer = BasicRepoCargoAssignmentHelper.fetchById(this.ds.getConnection(), cargoAssignmentId);
            return customer.orElseThrow(()
                    -> new TmsException("CargoAssignment " + cargoAssignmentId.toString() + " was not found", ErrorCodes.REPO_PROVIDEER_ISSUES));
        } catch (SQLException ex) {
            throw new TmsException("CargoAssignment lookup failed", ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public UUID createCargoAssignment(CargoAssignment t) throws TmsException {
        try {
            return BasicRepoCargoAssignmentHelper.update(this.ds.getConnection(), this.debugMode, t);
        } catch (SQLException ex) {
            throw new TmsException("CargoAssignment creation failed", ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public UUID editCargoAssignment(CargoAssignment t) throws TmsException {
        try {
            return BasicRepoCargoAssignmentHelper.update(this.ds.getConnection(), this.debugMode, t);
        } catch (SQLException ex) {
            throw new TmsException("CargoAssignment update failed", ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public void deleteCargoAssignment(UUID cargoId) throws TmsException {
        try {
            BasicRepoCargoAssignmentHelper.block(this.ds.getConnection(), cargoId);
        } catch (SQLException ex) {
            throw new TmsException("CargoAssignment deletion failed", ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public Customer getCustomer(UUID customerId) throws TmsException {
        try {
            Optional<Customer> customer = BasicRepoCustomerHelper.fetchById(this.ds.getConnection(), customerId);
            return customer.orElseThrow(()
                    -> new TmsException("Customer " + customerId.toString() + " was not found", ErrorCodes.REPO_PROVIDEER_ISSUES));
        } catch (SQLException ex) {
            throw new TmsException("Customer lookup failed", ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public UUID createCustomer(Customer p) throws TmsException {
        try {
            return BasicRepoCustomerHelper.update(this.ds.getConnection(), this.debugMode, p);
        } catch (SQLException ex) {
            throw new TmsException("Customer creation failed", ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public UUID editCustomer(Customer p) throws TmsException {
        try {
            return BasicRepoCustomerHelper.update(this.ds.getConnection(), this.debugMode, p);
        } catch (SQLException ex) {
            throw new TmsException("Customer update failed", ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public void deleteCustomer(UUID customerId) throws TmsException {
        try {
            BasicRepoCustomerHelper.block(this.ds.getConnection(), customerId);
        } catch (SQLException ex) {
            throw new TmsException("Customer deletion failed", ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public Vehicle getVehicule(UUID vehicleId) throws TmsException {
        try {
            Optional<Vehicle> v = BasicRepoVehicleHelper.fetchById(this.ds.getConnection(), vehicleId);
            return v.orElseThrow(() -> new TmsException("Vehicule " + vehicleId.toString() + " was not found", ErrorCodes.REPO_PROVIDEER_ISSUES));
        } catch (SQLException ex) {
            throw new TmsException("Vehicule lookup failed", ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public UUID createVehicle(Vehicle v) throws TmsException {
        try {
            return BasicRepoVehicleHelper.update(this.ds.getConnection(), this.debugMode, v);
        } catch (SQLException ex) {
            throw new TmsException("Vehicule creation faced an issue", ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public UUID editVehicle(Vehicle v) throws TmsException {
        try {
            return BasicRepoVehicleHelper.update(this.ds.getConnection(), this.debugMode, v);
        } catch (SQLException ex) {
            throw new TmsException("Vehicule edition faced an issue", ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public void deleteVehicle(UUID vehicleId) throws TmsException {
        try {
            BasicRepoVehicleHelper.block(this.ds.getConnection(), vehicleId);
        } catch (SQLException ex) {
            throw new TmsException("Vehicule deletion faced an issue", ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public Driver getDriver(UUID driverId) throws TmsException {
        try {
            Optional<Driver> d = BasicRepoDriverHelper.fetchById(this.ds.getConnection(), driverId);
            return d.orElseThrow(() -> new TmsException("Driver " + driverId.toString() + " was not found", ErrorCodes.REPO_PROVIDEER_ISSUES));
        } catch (SQLException ex) {
            throw new TmsException("Driver lookup failed", ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public UUID createDriver(Driver d) throws TmsException {
        try {
            return BasicRepoDriverHelper.update(this.ds.getConnection(), this.debugMode, d);
        } catch (SQLException ex) {
            throw new TmsException("Driver creation failed", ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public UUID editDriver(Driver d) throws TmsException {
        try {
            return BasicRepoDriverHelper.update(this.ds.getConnection(), this.debugMode, d);
        } catch (SQLException ex) {
            throw new TmsException("Driver update failed", ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public void deleteDriver(UUID driverId) throws TmsException {
        try {
            BasicRepoDriverHelper.block(this.ds.getConnection(), driverId);
        } catch (SQLException ex) {
            throw new TmsException("Driver deletion failed", ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public Patio getPatio(UUID patioId) throws TmsException {
        try {
            Optional<Patio> patio = BasicRepoPatioHelper.fetchById(this.ds.getConnection(), patioId);
            return patio.orElseThrow(()
                    -> new TmsException("Patio " + patioId.toString() + " was not found", ErrorCodes.REPO_PROVIDEER_ISSUES));
        } catch (SQLException ex) {
            throw new TmsException("Patio lookup failed", ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public UUID createPatio(Patio p) throws TmsException {
        try {
            return BasicRepoPatioHelper.update(this.ds.getConnection(), this.debugMode, p);
        } catch (SQLException ex) {
            throw new TmsException("Patio creation failed", ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public UUID editPatio(Patio p) throws TmsException {
        try {
            return BasicRepoPatioHelper.update(this.ds.getConnection(), this.debugMode, p);
        } catch (SQLException ex) {
            throw new TmsException("Patio update failed", ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public void deletePatio(UUID patioId) throws TmsException {
        try {
            BasicRepoPatioHelper.block(this.ds.getConnection(), patioId);
        } catch (SQLException ex) {
            throw new TmsException("Patio deletion failed", ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public Agreement getAgreement(UUID agreementId) throws TmsException {
        try {
            Optional<Agreement> agreement = BasicRepoAgreementHelper.fetchById(this.ds.getConnection(), agreementId);
            return agreement.orElseThrow(() -> new TmsException(
                    "Agreement " + agreementId.toString() + " was not found",
                    ErrorCodes.REPO_PROVIDEER_ISSUES
            ));
        } catch (SQLException ex) {
            throw new TmsException("Agreement lookup failed", ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public UUID createAgreement(Agreement c) throws TmsException {
        try {
            return BasicRepoAgreementHelper.update(this.ds.getConnection(), this.debugMode, c);
        } catch (SQLException ex) {
            throw new TmsException("Agreement creation failed", ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public UUID editAgreement(Agreement c) throws TmsException {
        try {
            return BasicRepoAgreementHelper.update(this.ds.getConnection(), this.debugMode, c);
        } catch (SQLException ex) {
            throw new TmsException("Agreement update failed", ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public void deleteAgreement(UUID agreementId) throws TmsException {
        try {
            BasicRepoAgreementHelper.block(this.ds.getConnection(), agreementId);
        } catch (SQLException ex) {
            throw new TmsException("Agreement deletion failed", ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public TransLogRecord getTransLogRecord(UUID transLogRecordId) throws TmsException {
        try {
            Optional<TransLogRecord> agreement = BasicRepoTransLogRecordHelper.fetchById(this.ds.getConnection(), transLogRecordId);
            return agreement.orElseThrow(() -> new TmsException(
                    "TransLogRecord " + transLogRecordId.toString() + " was not found",
                    ErrorCodes.REPO_PROVIDEER_ISSUES
            ));
        } catch (SQLException ex) {
            throw new TmsException("TransLogRecord lookup failed", ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public UUID createTransLogRecord(TransLogRecord tlr) throws TmsException {
        try {
            return BasicRepoTransLogRecordHelper.update(this.ds.getConnection(), this.debugMode, tlr);
        } catch (SQLException ex) {
            throw new TmsException("TransLogRecord creation failed", ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }
}
