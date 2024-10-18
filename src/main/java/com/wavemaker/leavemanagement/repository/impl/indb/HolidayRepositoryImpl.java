package com.wavemaker.leavemanagement.repository.impl.indb;

import com.wavemaker.leavemanagement.exception.ServerUnavailableException;
import com.wavemaker.leavemanagement.model.Holiday;
import com.wavemaker.leavemanagement.model.LoginCredential;
import com.wavemaker.leavemanagement.repository.HolidayRepository;
import com.wavemaker.leavemanagement.repository.LoginCredentialRepository;
import com.wavemaker.leavemanagement.util.DbConnection;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HolidayRepositoryImpl implements HolidayRepository {
    private static final String SELECT_HOLIDAYS_QUERY =
            "SELECT * FROM HOLIDAYS";

    @Override
    public List<Holiday> getUpcomingHolidays() throws ServerUnavailableException {
        List<Holiday> holidays = new ArrayList<>();
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_HOLIDAYS_QUERY);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String holidayName = resultSet.getString("HOLIDAY_NAME");
                Date holidayStartDate = resultSet.getDate("HOLIDAY_FROM_DATE");
                Date holidayEndDate = resultSet.getDate("HOLIDAY_TO_DATE");

                Holiday holiday = new Holiday();
                holiday.setHolidayName(holidayName);
                holiday.setHolidayStartDate(holidayStartDate.toLocalDate());
                holiday.setHolidayEndDate(holidayEndDate.toLocalDate());// Assuming you want to return date as a String
                holidays.add(holiday);
            }
        } catch (SQLException e) {
            throw new ServerUnavailableException("server is unavailable to fetch holidays", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);


            // Handle exceptions (e.g., logging)
        }

        return holidays;
    }

    public static class LoginCredentialRepositoryImpl implements LoginCredentialRepository {
        private static final Logger logger = LoggerFactory.getLogger(LoginCredentialRepositoryImpl.class);

        @Override
        public int isValidate(LoginCredential loginCredential) {
            String query = "SELECT * FROM LOGIN_CREDENTIAL WHERE EMAILID=? AND PASSWORD=?";
            try (Connection connection = DbConnection.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                preparedStatement.setString(1, loginCredential.getEmailId());
                preparedStatement.setString(2, loginCredential.getPassword());

                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getInt("LOGIN_ID");
                }

            } catch (SQLException e) {
                logger.debug("Error validating user", e);
            }

            return -1;
        }

        @Override
        public LoginCredential addEmployeeLogin(LoginCredential loginCredential) {
            String query = "INSERT INTO LOGIN_CREDENTIAL(EMAILID,PASSWORD) VALUES(?, ?)";
            try (Connection connection = DbConnection.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, loginCredential.getEmailId());
                preparedStatement.setString(2, loginCredential.getPassword());
                preparedStatement.executeUpdate();
                return loginCredential;  // Return the added user object

            } catch (SQLException e) {
                logger.debug("Error adding user", e);

            }
            return null;  // Return null if user addition fails
        }
    }
}
