package com.wavemaker.leavemanagement.repository;


import com.wavemaker.leavemanagement.exception.ServerUnavailableException;

public interface LeaveTypeRepository {
    int getNumberOfLeavesAllocated(String leaveType);

    String getLeaveType(int leaveTypeId) throws ServerUnavailableException;
}
