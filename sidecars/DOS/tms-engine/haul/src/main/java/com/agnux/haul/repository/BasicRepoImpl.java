package com.agnux.haul.repository;

import com.agnux.haul.errors.ErrorCodes;
import com.agnux.haul.errors.TmsException;
import com.agnux.haul.repository.model.Agreement;
import com.agnux.haul.repository.model.CargoAssignment;
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
    public Vehicle getAvailableVehicule(UUID vehicleId) throws TmsException {
        try {
            Optional<Vehicle> v = BasicRepoVehiculeHelper.fetchById(this.ds.getConnection(), vehicleId);
            return v.orElseThrow(() -> new TmsException("Vehicule " + vehicleId.toString() + " was not found", ErrorCodes.UNKNOWN_ISSUE));
        } catch (SQLException ex) {
            throw new TmsException("Vehicule creation faced an issue", ex, ErrorCodes.UNKNOWN_ISSUE);
        }
    }

    @Override
    public Agreement getAvailableAgreement(UUID agreementId) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public UUID createVehicle(Vehicle v) throws TmsException {
        try {
            return BasicRepoVehiculeHelper.update(this.ds.getConnection(), this.debugMode, v);
        } catch (SQLException ex) {
            throw new TmsException("Vehicule creation faced an issue", ex, ErrorCodes.UNKNOWN_ISSUE);
        }
    }

    @Override
    public void editVehicle(Vehicle v) throws TmsException {
        try {
            BasicRepoVehiculeHelper.update(this.ds.getConnection(), this.debugMode, v);
        } catch (SQLException ex) {
            throw new TmsException("Vehicule edition faced an issue", ex, ErrorCodes.UNKNOWN_ISSUE);
        }
    }
}
