package com.wavemaker.leavemanagement.login;

import com.google.gson.Gson;
import com.wavemaker.leavemanagement.model.LoginCredential;
import com.wavemaker.leavemanagement.service.EmployeeCookieService;
import com.wavemaker.leavemanagement.service.LoginCredentialService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.UUID;

@RestController
@RequestMapping("/login")
public class LoginCredentialServlet {
    private static Gson gson = new Gson();
    @Autowired
    private LoginCredentialService loginCredentialService;
    @Autowired
    private EmployeeCookieService employeeCookieService;


    @PostMapping()
    private void authenticate(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "emailId", required = true) String emailId, @RequestParam(value = "password", required = true) String password)
            throws IOException, ServletException {
        if (emailId == null || password == null) {
            writeResponse(response, gson.toJson("Missing required parameters."));
            return;
        }

        LoginCredential loginCredential = new LoginCredential();
        loginCredential.setEmailId(emailId);
        loginCredential.setPassword(password);

        try {

            int loginId = loginCredentialService.isValidate(loginCredential);
            if (loginId != -1) {
                String cookieValue = UUID.randomUUID().toString();
                String cookieName = "auth_cookie";
                Cookie cookie = new Cookie(cookieName, cookieValue);
                HttpSession adminSession = request.getSession(true);
                cookie.setMaxAge(172800); // 2 days * 24 hours * 60 minutes * 60 seconds
                adminSession.setAttribute(cookieValue, loginId);

                response.addCookie(cookie);
                loginCredential.setLoginId(loginId);
                employeeCookieService.addCookie(cookieValue, loginId);
                writeResponse(response, "User  login with correct authentication.");
                // response.sendRedirect("index.html");
            } else {
                writeResponse(response, "User  login with incorrect authentication.");
                response.sendRedirect("login.html?error=" + URLEncoder.encode("Invalid username or password.", "UTF-8"));
            }
        } catch (IOException e) {
            writeResponse(response, gson.toJson("An error occurred while processing your request."));
        }
    }

    private void writeResponse(HttpServletResponse response, String message) throws IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.println(message);
    }
}


