package com.wavemaker.leavemanagement.service.impl;

import com.wavemaker.leavemanagement.factory.EmployeeCookieRepositoryGlobalInstance;
import com.wavemaker.leavemanagement.repository.EmployeeCookieRepository;
import com.wavemaker.leavemanagement.service.EmployeeCookieService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeCookieServiceImpl implements EmployeeCookieService {
    private EmployeeCookieRepository employeeCookieRepository;

    public EmployeeCookieServiceImpl() {
        employeeCookieRepository = EmployeeCookieRepositoryGlobalInstance.getEmployeeCookieRepositoryInstance();

    }


    @Override
    public void addCookie(String cookieValue, int userId) {
        employeeCookieRepository.addCookie(cookieValue, userId);
    }

    @Override
    public int getloginIdByCookieValue(String cookieValue) {
        return employeeCookieRepository.getLoginIdByCookieValue(cookieValue);
    }

    @Override
    public void removeCookie(String cookieValue) {
        employeeCookieRepository.removeCookie(cookieValue);

    }
}
