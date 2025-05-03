package com.agnux.haul.repository;

import com.agnux.haul.errors.ErrorCodes;
import com.agnux.haul.errors.TmsException;
import com.agnux.haul.repository.model.Agreement;
import com.agnux.haul.repository.model.CargoAssignment;
import com.agnux.haul.repository.model.Driver;
import com.agnux.haul.repository.model.Patio;
import com.agnux.haul.repository.model.Vehicle;

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
    public String createCargoAssignment(CargoAssignment t) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Agreement getAvailableAgreement(UUID agreementId) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Vehicle getAvailableVehicule(UUID vehicleId) throws TmsException {
        try {
            Optional<Vehicle> v = BasicRepoVehiculeHelper.fetchById(this.ds.getConnection(), vehicleId);
            return v.orElseThrow(() -> new TmsException("Vehicule " + vehicleId.toString() + " was not found", ErrorCodes.REPO_PROVIDEER_ISSUES));
        } catch (SQLException ex) {
            throw new TmsException("Vehicule lookup failed", ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public UUID createVehicle(Vehicle v) throws TmsException {
        try {
            return BasicRepoVehiculeHelper.update(this.ds.getConnection(), this.debugMode, v);
        } catch (SQLException ex) {
            throw new TmsException("Vehicule creation faced an issue", ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public UUID editVehicle(Vehicle v) throws TmsException {
        try {
            return BasicRepoVehiculeHelper.update(this.ds.getConnection(), this.debugMode, v);
        } catch (SQLException ex) {
            throw new TmsException("Vehicule edition faced an issue", ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public void deleteVehicle(UUID vehicleId) throws TmsException {
        try {
            BasicRepoVehiculeHelper.block(this.ds.getConnection(), vehicleId);
        } catch (SQLException ex) {
            throw new TmsException("Vehicule deletion faced an issue", ex, ErrorCodes.REPO_PROVIDEER_ISSUES);
        }
    }

    @Override
    public Driver getAvailableDriver(UUID driverId) throws TmsException {
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
    public Patio getAvailablePatio(UUID patioId) throws TmsException {
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

}
