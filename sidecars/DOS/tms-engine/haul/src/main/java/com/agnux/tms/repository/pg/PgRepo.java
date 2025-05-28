package com.agnux.tms.repository.pg;

import com.agnux.tms.errors.ErrorCodes;
import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.IHaulRepo;
import com.agnux.tms.repository.PaginationSegment;
import com.agnux.tms.repository.model.Agreement;
import com.agnux.tms.repository.model.Box;
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
public class PgRepo implements IHaulRepo {

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
        return fetchEntity(id, PgRepoCargoAssignmentHelper.ENTITY_NAME, PgRepoCargoAssignmentHelper::fetchById);
    }

    @Override
    public UUID createCargoAssignment(CargoAssignment t) throws TmsException {
        return saveOrUpdateEntity(t, PgRepoCargoAssignmentHelper.ENTITY_NAME, PgRepoCargoAssignmentHelper::update, true);
    }

    @Override
    public UUID editCargoAssignment(CargoAssignment t) throws TmsException {
        return saveOrUpdateEntity(t, PgRepoCargoAssignmentHelper.ENTITY_NAME, PgRepoCargoAssignmentHelper::update, false);
    }

    @Override
    public void deleteCargoAssignment(UUID id) throws TmsException {
        deleteEntity(id, PgRepoCargoAssignmentHelper.ENTITY_NAME, PgRepoCargoAssignmentHelper::block);
    }

    // Customer
    @Override
    public Customer getCustomer(UUID id) throws TmsException {
        return fetchEntity(id, PgRepoCustomerHelper.ENTITY_NAME, PgRepoCustomerHelper::fetchById);
    }

    @Override
    public UUID createCustomer(Customer p) throws TmsException {
        return saveOrUpdateEntity(p, PgRepoCustomerHelper.ENTITY_NAME, PgRepoCustomerHelper::update, true);
    }

    @Override
    public UUID editCustomer(Customer p) throws TmsException {
        return saveOrUpdateEntity(p, PgRepoCustomerHelper.ENTITY_NAME, PgRepoCustomerHelper::update, false);
    }

    @Override
    public void deleteCustomer(UUID id) throws TmsException {
        deleteEntity(id, PgRepoCustomerHelper.ENTITY_NAME, PgRepoCustomerHelper::block);
    }

    @Override
    public PaginationSegment<Customer> listCustomerPage(Map<String, String> filters, Map<String, String> pageParams) throws TmsException {
        return listEntityPage(filters, pageParams, PgRepoCustomerHelper.ENTITY_NAME, PgRepoCustomerHelper::list);
    }

    @Override
    public PaginationSegment<Vehicle> listVehiclePage(Map<String, String> filters, Map<String, String> pageParams) throws TmsException {
        return listEntityPage(filters, pageParams, PgRepoVehicleHelper.ENTITY_NAME, PgRepoVehicleHelper::list);
    }

    // Vehicle
    @Override
    public Vehicle getVehicle(UUID id) throws TmsException {
        return fetchEntity(id, PgRepoVehicleHelper.ENTITY_NAME, PgRepoVehicleHelper::fetchById);
    }

    @Override
    public UUID createVehicle(Vehicle v) throws TmsException {
        return saveOrUpdateEntity(v, PgRepoVehicleHelper.ENTITY_NAME, PgRepoVehicleHelper::update, true);
    }

    @Override
    public UUID editVehicle(Vehicle v) throws TmsException {
        return saveOrUpdateEntity(v, PgRepoVehicleHelper.ENTITY_NAME, PgRepoVehicleHelper::update, false);
    }

    @Override
    public void deleteVehicle(UUID id) throws TmsException {
        deleteEntity(id, PgRepoVehicleHelper.ENTITY_NAME, PgRepoVehicleHelper::block);
    }

    @Override
    public Driver getDriver(UUID id) throws TmsException {
        return fetchEntity(id, PgRepoDriverHelper.ENTITY_NAME, PgRepoDriverHelper::fetchById);
    }

    @Override
    public UUID createDriver(Driver d) throws TmsException {
        return saveOrUpdateEntity(d, PgRepoDriverHelper.ENTITY_NAME, PgRepoDriverHelper::update, true);
    }

    @Override
    public UUID editDriver(Driver d) throws TmsException {
        return saveOrUpdateEntity(d, PgRepoDriverHelper.ENTITY_NAME, PgRepoDriverHelper::update, false);
    }

    @Override
    public void deleteDriver(UUID id) throws TmsException {
        deleteEntity(id, PgRepoDriverHelper.ENTITY_NAME, PgRepoDriverHelper::block);
    }

    @Override
    public PaginationSegment<Driver> listDriverPage(Map<String, String> filters, Map<String, String> pageParams) throws TmsException {
        return listEntityPage(filters, pageParams, PgRepoDriverHelper.ENTITY_NAME, PgRepoDriverHelper::list);
    }

    // Patio
    @Override
    public Patio getPatio(UUID id) throws TmsException {
        return fetchEntity(id, PgRepoPatioHelper.ENTITY_NAME, PgRepoPatioHelper::fetchById);
    }

    @Override
    public UUID createPatio(Patio p) throws TmsException {
        return saveOrUpdateEntity(p, PgRepoPatioHelper.ENTITY_NAME, PgRepoPatioHelper::update, true);
    }

    @Override
    public UUID editPatio(Patio p) throws TmsException {
        return saveOrUpdateEntity(p, PgRepoPatioHelper.ENTITY_NAME, PgRepoPatioHelper::update, false);
    }

    @Override
    public void deletePatio(UUID id) throws TmsException {
        deleteEntity(id, PgRepoPatioHelper.ENTITY_NAME, PgRepoPatioHelper::block);
    }

    @Override
    public PaginationSegment<Patio> listPatioPage(Map<String, String> filters, Map<String, String> pageParams) throws TmsException {
        return listEntityPage(filters, pageParams, PgRepoPatioHelper.ENTITY_NAME, PgRepoPatioHelper::list);
    }

    // Agreement
    @Override
    public Agreement getAgreement(UUID id) throws TmsException {
        return fetchEntity(id, PgRepoAgreementHelper.ENTITY_NAME, PgRepoAgreementHelper::fetchById);
    }

    @Override
    public UUID createAgreement(Agreement a) throws TmsException {
        return saveOrUpdateEntity(a, PgRepoAgreementHelper.ENTITY_NAME, PgRepoAgreementHelper::update, true);
    }

    @Override
    public UUID editAgreement(Agreement a) throws TmsException {
        return saveOrUpdateEntity(a, PgRepoAgreementHelper.ENTITY_NAME, PgRepoAgreementHelper::update, false);
    }

    @Override
    public void deleteAgreement(UUID id) throws TmsException {
        deleteEntity(id, PgRepoAgreementHelper.ENTITY_NAME, PgRepoAgreementHelper::block);
    }

    @Override
    public PaginationSegment<Agreement> listAgreementPage(Map<String, String> filters, Map<String, String> pageParams) throws TmsException {
        return listEntityPage(filters, pageParams, PgRepoAgreementHelper.ENTITY_NAME, PgRepoAgreementHelper::list);
    }

    @Override
    public TransLogRecord getTransLogRecord(UUID id) throws TmsException {
        return fetchEntity(id, PgRepoTransLogRecordHelper.ENTITY_NAME, PgRepoTransLogRecordHelper::fetchById);
    }

    @Override
    public UUID createTransLogRecord(TransLogRecord tlr) throws TmsException {
        return saveOrUpdateEntity(tlr, PgRepoTransLogRecordHelper.ENTITY_NAME, PgRepoTransLogRecordHelper::update, true);
    }

    // Box
    @Override
    public Box getBox(UUID id) throws TmsException {
        return fetchEntity(id, PgRepoBoxHelper.ENTITY_NAME, PgRepoBoxHelper::fetchById);
    }

    @Override
    public UUID createBox(Box p) throws TmsException {
        return saveOrUpdateEntity(p, PgRepoBoxHelper.ENTITY_NAME, PgRepoBoxHelper::update, true);
    }

    @Override
    public UUID editBox(Box p) throws TmsException {
        return saveOrUpdateEntity(p, PgRepoBoxHelper.ENTITY_NAME, PgRepoBoxHelper::update, false);
    }

    @Override
    public void deleteBox(UUID id) throws TmsException {
        deleteEntity(id, PgRepoBoxHelper.ENTITY_NAME, PgRepoBoxHelper::block);
    }

    @Override
    public PaginationSegment<Box> listBoxPage(Map<String, String> filters, Map<String, String> pageParams) throws TmsException {
        return listEntityPage(filters, pageParams, PgRepoBoxHelper.ENTITY_NAME, PgRepoBoxHelper::list);
    }
}
