package com.wavemaker.leavemanagement.service.impl;

import com.wavemaker.leavemanagement.model.LoginCredential;
import com.wavemaker.leavemanagement.repository.LoginCredentialRepository;
import com.wavemaker.leavemanagement.service.LoginCredentialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginCredentialServiceImpl implements LoginCredentialService {

    @Autowired
    private LoginCredentialRepository loginCredentialRepository;


    @Override
    public int isValidate(LoginCredential loginCredential) {
        return loginCredentialRepository.isValidate(loginCredential);

    }

    @Override
    public LoginCredential addEmployeeLogin(LoginCredential loginCredential) {
        return loginCredentialRepository.addEmployeeLogin(loginCredential);
    }
}
