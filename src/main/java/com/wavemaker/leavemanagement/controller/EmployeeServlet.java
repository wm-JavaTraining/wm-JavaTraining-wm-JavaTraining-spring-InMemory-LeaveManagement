package com.wavemaker.leavemanagement.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wavemaker.leavemanagement.exception.ServerUnavailableException;
import com.wavemaker.leavemanagement.model.*;
import com.wavemaker.leavemanagement.service.EmployeeService;
import com.wavemaker.leavemanagement.service.impl.EmployeeServiceImpl;
import com.wavemaker.leavemanagement.util.LocalDateAdapter;
import com.wavemaker.leavemanagement.util.LocalTimeAdapter;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;

@WebServlet("/employee/leave/employeeDetails/*")
public class EmployeeServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeServlet.class);
    private static Gson gson;
    private static EmployeeService employeeService;
    private static ApplicationContext context;

    @Override
    public void init() {
        context = new ClassPathXmlApplicationContext("configmetadata.xml");
        employeeService = context.getBean(EmployeeService.class);
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
                .create();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.equals("/getEmployeeName")) {
            getEmployeeName(request, response);
        } else if (pathInfo != null && pathInfo.equals("/getEmployeeAndManagerDetails")) {
            getEmployeeAndManagerDetails(request, response);
        } else if (pathInfo != null && pathInfo.equals("/getEmployeeDetailsAndLeaveSummary")) {
            getEmployeeDetailsAndLeaveSummary(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writeResponse(response, "The requested resource [" + pathInfo + "] is not available.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String jsonResponse = "";
        Employee employee = null;
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                writeResponse(response, "User is not logged");
                return;
            }
            Integer loginId = (Integer) session.getAttribute("loginId");
            if (loginId == null) {
                writeResponse(response, "User is not present.");
                return;
            }
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            try (BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
            }
            String requestBody = stringBuilder.toString();
            employee = gson.fromJson(requestBody, Employee.class);
            employee = employeeService.addEmployee(employee);
            if (employee == null) {
                writeResponse(response, "Employee not addes found.");
                return;
            }
            jsonResponse = gson.toJson(employee);
            writeResponse(response, jsonResponse);

        } catch (Exception e) {
            jsonResponse = "An error occurred while inserting the leave request: \" + e.getMessage()";
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeResponse(response, jsonResponse);

        }


    }

    private void getEmployeeName(HttpServletRequest request, HttpServletResponse response) {
        Employee employee = null;
        String jsonResponse = "";
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                Integer loginId = (Integer) session.getAttribute("loginId");
                if (loginId != null) {
                    employee = employeeService.getEmployeeByLoginId(loginId);
                    if (employee != null) {
                        jsonResponse = gson.toJson(employee);
                        writeResponse(response, jsonResponse);
                    } else {
                        writeResponse(response, "Employee not found.");
                    }
                } else {
                    writeResponse(response, "User is not present.");
                }
            } else {
                writeResponse(response, "Session is not valid.");
            }
        } catch (Exception e) {
            jsonResponse = "An error occurred while fetching leaves for the logged-in employee: " + e.getMessage();
            writeResponse(response, jsonResponse);

        }

    }

    private void getEmployeeAndManagerDetails(HttpServletRequest request, HttpServletResponse response) {
        EmployeeManager employeeManager = null;
        Employee employee = null;
        String jsonResponse = "";
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                Integer loginId = (Integer) session.getAttribute("loginId");
                if (loginId != null) {
                    employee = employeeService.getEmployeeByLoginId(loginId);
                    if (employee != null) {
                        int employeeId = employee.getEmployeeId();
                        int managerId = employee.getManagerId();
                        if (managerId != 0) {
                            employeeManager = employeeService.getEmployeeManagerDetails(employeeId);
                            jsonResponse = gson.toJson(employeeManager);
                            writeResponse(response, jsonResponse);
                        } else {
                            employee = employeeService.getEmployeeByLoginId(loginId);
                            if (employee != null) {
                                jsonResponse = gson.toJson(employee);
                                writeResponse(response, jsonResponse);
                            } else {
                                writeResponse(response, "Employee not found.");
                            }
                        }
                    }
                } else {
                    writeResponse(response, "Employee not found.");
                }
            } else {
                writeResponse(response, "User is not present.");
            }

        } catch (Exception e) {
            jsonResponse = "\"An error occurred while fetching leaves for the logged-in employee: \" + e.getMessage()";
            writeResponse(response, jsonResponse);
        }

    }

    private void getEmployeeDetailsAndLeaveSummary(HttpServletRequest request, HttpServletResponse response) {
        EmployeeLeave employeeLeave = null;
        String jsonResponse = null;
        String empIdStr = request.getParameter("employeeId");
        if (empIdStr == null || empIdStr.isEmpty()) {
            writeResponse(response, "employeeId is empty");
            return;
        }
        try {
            int empId = Integer.parseInt(empIdStr);
            employeeLeave = employeeService.getEmployeeDetailsAndLeaveSummary(empId);
            jsonResponse = gson.toJson(employeeLeave);
            writeResponse(response, jsonResponse);

        } catch (NumberFormatException e) {
            writeResponse(response, "Invalid employee ID format");
        } catch (Exception e) {// Log the exception
            writeResponse(response, "Error fetching employee details");
        }
    }

    private void writeResponse(HttpServletResponse response, String jsonResponse) {
        PrintWriter printWriter = null;
        try {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            printWriter = response.getWriter();
            printWriter.print(jsonResponse);
            printWriter.flush();
        } catch (IOException e) {
            jsonResponse = "server Unavailable";
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            printWriter.print(jsonResponse);
            printWriter.flush();

        }
    }
}
