package com.wavemaker.leavemanagement.service.impl;

import com.wavemaker.leavemanagement.repository.EmployeeNumberOfLeavesRepository;
import com.wavemaker.leavemanagement.service.EmployeeNumberOfLeavesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmployeeNumberOfLeavesServiceImpl implements EmployeeNumberOfLeavesService {
    @Autowired
    private EmployeeNumberOfLeavesRepository employeeNumberOfLeavesRepository;


    @Override
    public int getNoOfLeaves(int employeeId) {
        return employeeNumberOfLeavesRepository.getNoOfLeaves(employeeId);
    }
}
