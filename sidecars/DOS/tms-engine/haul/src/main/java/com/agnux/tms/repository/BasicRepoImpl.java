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
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor
public class BasicRepoImpl implements IHaulRepo {

    private static final String NOT_FOUND = " was not found";
    private static final String LOOKUP_FAILED = " lookup failed";
    private static final String CREATION_FAILED = " creation failed";
    private static final String UPDATE_FAILED = " update failed";
    private static final String DELETION_FAILED = " deletion failed";
    private static final String EDITION_FAILED = " edition faced an issue";
    private static final String CREATION_ISSUE = " creation faced an issue";

    @NonNull
    private DataSource ds;

    @NonNull
    private Boolean debugMode;

    @Override
    public CargoAssignment getCargoAssignment(UUID id) throws TmsException {
        try {
            return BasicRepoCargoAssignmentHelper.fetchById(ds.getConnection(), id)
                    .orElseThrow(() -> new TmsException("CargoAssignment " + id + NOT_FOUND, ErrorCodes.REPO_PROVIDEER_ISSUES));
        } catch (SQLException ex) {
            throw new TmsException("CargoAssignment" + LOOKUP_FAILED, ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public UUID createCargoAssignment(CargoAssignment t) throws TmsException {
        try {
            return BasicRepoCargoAssignmentHelper.update(ds.getConnection(), debugMode, t);
        } catch (SQLException ex) {
            throw new TmsException("CargoAssignment" + CREATION_FAILED, ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public UUID editCargoAssignment(CargoAssignment t) throws TmsException {
        try {
            return BasicRepoCargoAssignmentHelper.update(ds.getConnection(), debugMode, t);
        } catch (SQLException ex) {
            throw new TmsException("CargoAssignment" + UPDATE_FAILED, ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public void deleteCargoAssignment(UUID id) throws TmsException {
        try {
            BasicRepoCargoAssignmentHelper.block(ds.getConnection(), id);
        } catch (SQLException ex) {
            throw new TmsException("CargoAssignment" + DELETION_FAILED, ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public Customer getCustomer(UUID id) throws TmsException {
        try {
            return BasicRepoCustomerHelper.fetchById(ds.getConnection(), id)
                    .orElseThrow(() -> new TmsException("Customer " + id + NOT_FOUND, ErrorCodes.REPO_PROVIDEER_ISSUES));
        } catch (SQLException ex) {
            throw new TmsException("Customer" + LOOKUP_FAILED, ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public UUID createCustomer(Customer p) throws TmsException {
        try {
            return BasicRepoCustomerHelper.update(ds.getConnection(), debugMode, p);
        } catch (SQLException ex) {
            throw new TmsException("Customer" + CREATION_FAILED, ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public UUID editCustomer(Customer p) throws TmsException {
        try {
            return BasicRepoCustomerHelper.update(ds.getConnection(), debugMode, p);
        } catch (SQLException ex) {
            throw new TmsException("Customer" + UPDATE_FAILED, ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public void deleteCustomer(UUID id) throws TmsException {
        try {
            BasicRepoCustomerHelper.block(ds.getConnection(), id);
        } catch (SQLException ex) {
            throw new TmsException("Customer" + DELETION_FAILED, ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public Vehicle getVehicule(UUID id) throws TmsException {
        try {
            return BasicRepoVehicleHelper.fetchById(ds.getConnection(), id)
                    .orElseThrow(() -> new TmsException("Vehicule " + id + NOT_FOUND, ErrorCodes.REPO_PROVIDEER_ISSUES));
        } catch (SQLException ex) {
            throw new TmsException("Vehicule" + LOOKUP_FAILED, ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public UUID createVehicle(Vehicle v) throws TmsException {
        try {
            return BasicRepoVehicleHelper.update(ds.getConnection(), debugMode, v);
        } catch (SQLException ex) {
            throw new TmsException("Vehicule" + CREATION_ISSUE, ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public UUID editVehicle(Vehicle v) throws TmsException {
        try {
            return BasicRepoVehicleHelper.update(ds.getConnection(), debugMode, v);
        } catch (SQLException ex) {
            throw new TmsException("Vehicule" + EDITION_FAILED, ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public void deleteVehicle(UUID id) throws TmsException {
        try {
            BasicRepoVehicleHelper.block(ds.getConnection(), id);
        } catch (SQLException ex) {
            throw new TmsException("Vehicule" + DELETION_FAILED, ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public Driver getDriver(UUID id) throws TmsException {
        try {
            return BasicRepoDriverHelper.fetchById(ds.getConnection(), id)
                    .orElseThrow(() -> new TmsException("Driver " + id + NOT_FOUND, ErrorCodes.REPO_PROVIDEER_ISSUES));
        } catch (SQLException ex) {
            throw new TmsException("Driver" + LOOKUP_FAILED, ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public UUID createDriver(Driver d) throws TmsException {
        try {
            return BasicRepoDriverHelper.update(ds.getConnection(), debugMode, d);
        } catch (SQLException ex) {
            throw new TmsException("Driver" + CREATION_FAILED, ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public UUID editDriver(Driver d) throws TmsException {
        try {
            return BasicRepoDriverHelper.update(ds.getConnection(), debugMode, d);
        } catch (SQLException ex) {
            throw new TmsException("Driver" + UPDATE_FAILED, ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public void deleteDriver(UUID id) throws TmsException {
        try {
            BasicRepoDriverHelper.block(ds.getConnection(), id);
        } catch (SQLException ex) {
            throw new TmsException("Driver" + DELETION_FAILED, ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
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
