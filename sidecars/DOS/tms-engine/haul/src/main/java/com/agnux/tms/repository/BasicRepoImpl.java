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
import java.sql.Connection;

import java.sql.SQLException;
import java.util.Map;
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

    @NonNull
    private DataSource ds;

    @NonNull
    private Boolean debugMode;

    @FunctionalInterface
    public interface ListPage<T> {

        PaginationSegment<T> list(Connection conn, Map<String, String> filters, Map<String, String> pagination) throws TmsException;
    }

    @FunctionalInterface
    public interface FetchById<T> {

        Optional<T> fetch(Connection conn, UUID id) throws SQLException;
    }

    @FunctionalInterface
    public interface UpdateEntity<T> {

        UUID update(Connection conn, boolean debug, T entity) throws SQLException;
    }

    @FunctionalInterface
    public interface BlockById {

        void block(Connection conn, UUID id) throws TmsException;
    }

    private <T> PaginationSegment<T> listEntityPage(Map<String, String> filters, Map<String, String> pagination, String name, ListPage<T> lister) throws TmsException {
        try (var conn = ds.getConnection()) {
            return lister.list(conn, filters, pagination);
        } catch (SQLException ex) {
            throw new TmsException(name + " page failed", ex, ErrorCodes.REPO_PROVIDER_ISSUES);
        }
    }

    private <T> T fetchEntity(UUID id, String name, FetchById<T> fetcher) throws TmsException {
        try (var conn = ds.getConnection()) {
            return fetcher.fetch(conn, id)
                    .orElseThrow(() -> new TmsException(name + " " + id + NOT_FOUND, ErrorCodes.REPO_PROVIDER_NONPRESENT_DATA));
        } catch (SQLException ex) {
            throw new TmsException(name + LOOKUP_FAILED, ex, ErrorCodes.REPO_PROVIDER_ISSUES);
        }
    }

    private <T> UUID saveOrUpdateEntity(T entity, String name, UpdateEntity<T> updater, boolean isCreation) throws TmsException {
        try (var conn = ds.getConnection()) {
            return updater.update(conn, debugMode, entity);
        } catch (SQLException ex) {
            String errorMessage = name + (isCreation ? CREATION_FAILED : UPDATE_FAILED);
            throw new TmsException(errorMessage, ex, ErrorCodes.REPO_PROVIDER_ISSUES);
        }
    }

    private void deleteEntity(UUID id, String name, BlockById blocker) throws TmsException {
        try (var conn = ds.getConnection()) {
            blocker.block(conn, id);
        } catch (SQLException ex) {
            throw new TmsException(name + DELETION_FAILED, ex, ErrorCodes.REPO_PROVIDER_ISSUES);
        }
    }

    @Override
    public CargoAssignment getCargoAssignment(UUID id) throws TmsException {
        return fetchEntity(id, "CargoAssignment", BasicRepoCargoAssignmentHelper::fetchById);
    }

    @Override
    public UUID createCargoAssignment(CargoAssignment t) throws TmsException {
        return saveOrUpdateEntity(t, "CargoAssignment", BasicRepoCargoAssignmentHelper::update, true);
    }

    @Override
    public UUID editCargoAssignment(CargoAssignment t) throws TmsException {
        return saveOrUpdateEntity(t, "CargoAssignment", BasicRepoCargoAssignmentHelper::update, false);
    }

    @Override
    public void deleteCargoAssignment(UUID id) throws TmsException {
        deleteEntity(id, "CargoAssignment", BasicRepoCargoAssignmentHelper::block);
    }

    // Customer
    @Override
    public Customer getCustomer(UUID id) throws TmsException {
        return fetchEntity(id, "Customer", BasicRepoCustomerHelper::fetchById);
    }

    @Override
    public UUID createCustomer(Customer p) throws TmsException {
        return saveOrUpdateEntity(p, "Customer", BasicRepoCustomerHelper::update, true);
    }

    @Override
    public UUID editCustomer(Customer p) throws TmsException {
        return saveOrUpdateEntity(p, "Customer", BasicRepoCustomerHelper::update, false);
    }

    @Override
    public void deleteCustomer(UUID id) throws TmsException {
        deleteEntity(id, "Customer", BasicRepoCustomerHelper::block);
    }

    @Override
    public PaginationSegment<Customer> listCustomerPage(Map<String, String> filters, Map<String, String> pagination) throws TmsException {
        return listEntityPage(filters, pagination, "Customer", BasicRepoCustomerHelper::list);
    }

    // Vehicle
    @Override
    public Vehicle getVehicle(UUID id) throws TmsException {
        return fetchEntity(id, BasicRepoVehicleHelper.ENTITY_NAME, BasicRepoVehicleHelper::fetchById);
    }

    @Override
    public UUID createVehicle(Vehicle v) throws TmsException {
        return saveOrUpdateEntity(v, "Vehicule", BasicRepoVehicleHelper::update, true);
    }

    @Override
    public UUID editVehicle(Vehicle v) throws TmsException {
        return saveOrUpdateEntity(v, "Vehicule", BasicRepoVehicleHelper::update, false);
    }

    @Override
    public void deleteVehicle(UUID id) throws TmsException {
        deleteEntity(id, "Vehicule", BasicRepoVehicleHelper::block);
    }

    @Override
    public Driver getDriver(UUID id) throws TmsException {
        return fetchEntity(id, "Driver", BasicRepoDriverHelper::fetchById);
    }

    @Override
    public UUID createDriver(Driver d) throws TmsException {
        return saveOrUpdateEntity(d, "Driver", BasicRepoDriverHelper::update, true);
    }

    @Override
    public UUID editDriver(Driver d) throws TmsException {
        return saveOrUpdateEntity(d, "Driver", BasicRepoDriverHelper::update, false);
    }

    @Override
    public void deleteDriver(UUID id) throws TmsException {
        deleteEntity(id, "Driver", BasicRepoDriverHelper::block);
    }

    // Patio
    @Override
    public Patio getPatio(UUID id) throws TmsException {
        return fetchEntity(id, BasicRepoPatioHelper.ENTITY_NAME, BasicRepoPatioHelper::fetchById);
    }

    @Override
    public UUID createPatio(Patio p) throws TmsException {
        return saveOrUpdateEntity(p, "Patio", BasicRepoPatioHelper::update, true);
    }

    @Override
    public UUID editPatio(Patio p) throws TmsException {
        return saveOrUpdateEntity(p, "Patio", BasicRepoPatioHelper::update, false);
    }

    @Override
    public void deletePatio(UUID id) throws TmsException {
        deleteEntity(id, "Patio", BasicRepoPatioHelper::block);
    }

    // Agreement
    @Override
    public Agreement getAgreement(UUID id) throws TmsException {
        return fetchEntity(id, "Agreement", BasicRepoAgreementHelper::fetchById);
    }

    @Override
    public UUID createAgreement(Agreement a) throws TmsException {
        return saveOrUpdateEntity(a, "Agreement", BasicRepoAgreementHelper::update, true);
    }

    @Override
    public UUID editAgreement(Agreement a) throws TmsException {
        return saveOrUpdateEntity(a, "Agreement", BasicRepoAgreementHelper::update, false);
    }

    @Override
    public void deleteAgreement(UUID id) throws TmsException {
        deleteEntity(id, "Agreement", BasicRepoAgreementHelper::block);
    }

    @Override
    public TransLogRecord getTransLogRecord(UUID id) throws TmsException {
        return fetchEntity(id, "TransLogRecord", BasicRepoTransLogRecordHelper::fetchById);
    }

    @Override
    public UUID createTransLogRecord(TransLogRecord tlr) throws TmsException {
        return saveOrUpdateEntity(tlr, "TransLogRecord", BasicRepoTransLogRecordHelper::update, true);
    }
}
