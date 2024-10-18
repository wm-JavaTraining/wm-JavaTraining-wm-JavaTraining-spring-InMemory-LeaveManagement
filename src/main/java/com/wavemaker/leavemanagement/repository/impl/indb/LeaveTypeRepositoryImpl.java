package com.wavemaker.leavemanagement.repository.impl.indb;

import com.wavemaker.leavemanagement.exception.ServerUnavailableException;
import com.wavemaker.leavemanagement.repository.LeaveTypeRepository;
import com.wavemaker.leavemanagement.util.DbConnection;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class LeaveTypeRepositoryImpl implements LeaveTypeRepository {
    private static final Logger logger = LoggerFactory.getLogger(LeaveTypeRepositoryImpl.class);
    private static final String GET_NUMBER_OF_LEAVES_ALLOCATED =
            "SELECT LIMIT_FOR_LEAVES " +
                    "FROM LEAVE_TYPE " +
                    "WHERE TYPE_NAME = ?";
    private static final String SELECT_LEAVE_TYPE_QUERY =
            "SELECT TYPE_NAME FROM LEAVE_TYPE WHERE LEAVE_TYPE_ID  = ?";

    @Override
    public int getNumberOfLeavesAllocated(String leaveType) {
        int leaveLimit = 0;
        String leaveTypeName = leaveType.trim(); // Ensure trimming

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(GET_NUMBER_OF_LEAVES_ALLOCATED)) {

            preparedStatement.setString(1, leaveTypeName);
            logger.info("Executing query to get leave limit for type: {}", leaveTypeName);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    leaveLimit = resultSet.getInt("LIMIT_FOR_LEAVES");
                    logger.info("Leave limit for type '{}' is: {} ", leaveTypeName, leaveLimit);
                } else {
                    logger.warn("No leave type found for: {}", leaveTypeName);
                }
            } catch (Exception e) {
                logger.error("Error while exeucuting Limit Leaves Query", e);
                throw e;
            }
        } catch (Exception e) {
            logger.error("SQL Error in getNumberOfLeavesAllocated", e);
        }
        logger.info("Final Leave limit for type '{}' is: {} ", leaveTypeName, leaveLimit);
        return leaveLimit;

    }

    @Override
    public String getLeaveType(int leaveTypeId) throws ServerUnavailableException {
        String leaveType = ""; // Default value if not found


        try (Connection connection = DbConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_LEAVE_TYPE_QUERY)) {

            preparedStatement.setInt(1, leaveTypeId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    leaveType = resultSet.getString("TYPE_NAME");
                }
            }
        } catch (SQLException e) {
            throw new ServerUnavailableException("Unable to retrieve leave type ID", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        return leaveType;
    }
}
