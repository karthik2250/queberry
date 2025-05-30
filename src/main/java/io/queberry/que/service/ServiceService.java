package io.queberry.que.service;

import io.queberry.que.dto.ApptServiceResource;
import io.queberry.que.dto.ServiceDTO;
import io.queberry.que.dto.ServiceResource;
import io.queberry.que.dto.ServiceResponse;
import io.queberry.que.entity.Branch;
import io.queberry.que.entity.Service;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface ServiceService {


    Set<Service> getAllActiveServices(boolean active);
    Set<Service> getRegionActiveServices(String regionId);
    Service activate(String regionId);

    Service deactivate(String id);
    Service updateService(String id, ServiceResource resource);
    Service createService(Service service,HttpServletRequest request);
    Service subServices(String serviceId, Set<String> subServices);
    Service deactivateSubServices(String serviceId, String subServiceId);
    Service getAllRegionServices(String regionId, Pageable pageable);
    Page<Service> getAllServices(Pageable pageable);
    Page<Service> filterByName(String regionId, String serviceName, Pageable pageable);
    Set<ServiceDTO> findBySubServiceGroup(String subTransactionGroup);
    Set<ServiceResponse> getBranchServices(Branch branch);
    Set<ServiceResponse> getBranchService(ApptServiceResource apptServiceResource);
}
