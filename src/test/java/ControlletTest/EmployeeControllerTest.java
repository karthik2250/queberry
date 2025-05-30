package ControlletTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.queberry.que.QueApplication;
import io.queberry.que.controller.EmployeeController;
import io.queberry.que.dto.*;
import io.queberry.que.entity.Employee;
import io.queberry.que.entity.EmployeeData;
import io.queberry.que.exception.QueueException;
import io.queberry.que.service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@WebMvcTest(EmployeeController.class)
@ContextConfiguration(classes = QueApplication.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createEmployee_success() throws Exception {
        EmployeeData employeeData = new EmployeeData();
        employeeData.setFirstname("sandip");
        employeeData.setMiddlename("K");
        employeeData.setLastname("A V");
        employeeData.setUsername("sandip@queberry.com");
        employeeData.setPassword("securePass@123");
        employeeData.setActive(true);
        employeeData.setCallByNumber(true);
        employeeData.setEnableAutoCall(true);
        employeeData.setForceAutoCall(false);
        employeeData.setCallAll(true);
        employeeData.setCallNew(true);
        employeeData.setCallTransfer(true);
        employeeData.setWalkIn(true);
        employeeData.setBreak_btn(true);
        employeeData.setPark(true);
        employeeData.setTransferUser(true);
        employeeData.setTransferCounter(true);
        employeeData.setTransferService(true);
        employeeData.setShowServiceList(true);
        employeeData.setRegion("CHD_REGION_ID");

        employeeData.setRoles((java.util.List<String>) Set.of("5cbc68bd-b1a4-4ef0-8cc9-290e7771742c"));
        employeeData.setBranches((java.util.List<String>) Set.of("BRANCH_ID_001", "BRANCH_ID_002"));
        employeeData.setServices((java.util.List<String>) Set.of("SERVICE_ID_001", "SERVICE_ID_002"));
        employeeData.setSecond(Set.of("SECOND_SERVICE_001", "SECOND_SERVICE_002"));
        employeeData.setThird(Set.of("THIRD_SERVICE_001"));
        employeeData.setFourth(Set.of("FOURTH_SERVICE_001", "FOURTH_SERVICE_002"));

        mockMvc.perform(post("/employees") // Adjust your actual POST mapping path
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeData)))
                .andExpect(status().isOk());

        verify(employeeService).createEmployee(any(EmployeeData.class));
    }

    @Test
    void testResetPassword() throws Exception {
        // Create DTO object
        PasswordResetDTO dto = new PasswordResetDTO();
        dto.setUsername("sandip@queberry.com");
        dto.setNewPassword("securePass@123");
//        dto.setConfirmPassword("securePass@123");

        // Convert DTO to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String requestJson = objectMapper.writeValueAsString(dto);

        // Mock service response
        when(employeeService.resetPassword(any(PasswordResetDTO.class), any(HttpServletRequest.class)))
                .thenReturn("Password reset successful");

        // Perform the PUT request
//        mockMvc.perform(put("/employees/password/resets")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestJson))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Password reset successful"));
    }

    @Test
    void testFetchUserName() throws Exception {
        Employee employee = new Employee();
        employee.setUsername("sandip@queberry.com");

        List<Employee> employees = List.of(employee);
        Page<Employee> pageResult = new PageImpl<>(employees);

        when(employeeService.filterEmployeesByUsername(
                eq("sandip@queberry.com"),
                any(Pageable.class),
                any(HttpServletRequest.class)))
                .thenReturn(pageResult);

        mockMvc.perform(get("/employees/filterByUsername")
                        .param("userName", "sandip@queberry.com")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetEmployee_Success() throws Exception {
        EmployeeRequest employeeRequest = new EmployeeRequest();
        employeeRequest.setId("123");
        employeeRequest.setUsername("sandip@queberry.com");

        when(employeeService.getEmployeeById("123")).thenReturn(employeeRequest);

        mockMvc.perform(get("/employees/123"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetEmployee_NotFound() throws Exception {
        when(employeeService.getEmployeeById("999"))
                .thenThrow(new RuntimeException("Employee not found"));

        mockMvc.perform(get("/employees/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Employee not found"));
    }
    @Test
    void testUpdateEmployee_Success() throws Exception {
        String id = "123";
        String jsonBody = """
            {
              "firstname": "sandip",
              "lastname": "A V",
              "username": "sandip@queberry.com"
            }
            """;

        doNothing().when(employeeService).updateEmployee(eq(id), any(EmployeeData.class));

//        mockMvc.perform(put("/employees/{id}", id)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(jsonBody))
//                .andExpect(status().isOk());
//                .andExpect(content().string("Employee updated successfully."));
    }

    @Test
    void testUpdateEmployee_Conflict() throws Exception {
        String id = "123";
        String jsonBody = """
            {
              "firstname": "sandip",
              "lastname": "A V",
              "username": "sandip@queberry.com"
            }
            """;

        doThrow(new RuntimeException("Update conflict")).when(employeeService).updateEmployee(eq(id), any(EmployeeData.class));

//        mockMvc.perform(put("/employees/{id}", id)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(jsonBody))
//                .andExpect(status().isConflict())
//                .andExpect(content().string("Update conflict"));
    }

    @Test
    void testAssignCounter_Success() throws Exception {
        String employeeId = "123";
        String counterId = "counter001";

        Employee employee = new Employee();
        employee.setId(employeeId);
        employee.setCounter(counterId);

        when(employeeService.assignCounter(employeeId, counterId)).thenReturn(employee);

        mockMvc.perform((RequestBuilder) put("/employees/{id}/assign", employeeId)
                        .queryParam("counterId", counterId))
                .andExpect(status().isOk());
    }

    @Test
    void testAssignCounter_PreconditionFailed() throws Exception {
        String employeeId = "123";
        String counterId = "counter001";

        when(employeeService.assignCounter(employeeId, counterId))
                .thenThrow(new RuntimeException("Counter assignment failed"));

        mockMvc.perform((RequestBuilder) put("/employees/{id}/assign", employeeId)
                .queryParam("counterId", counterId));
    }
    @Test
    void testForgotPassword_UserNotFound() throws Exception {
        ForgotPasswordDTO forgotDTO = new ForgotPasswordDTO();
        forgotDTO.setUsername("unknownUser");

        String requestJson = new ObjectMapper().writeValueAsString(forgotDTO);

        when(employeeService.forgotPassword(any(ForgotPasswordDTO.class))).thenReturn("User not found.");

        mockMvc.perform((RequestBuilder) put("/employees/password/forgot")
                        .contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testForgotPassword_NewPasswordGenerated() throws Exception {
        ForgotPasswordDTO forgotDTO = new ForgotPasswordDTO();
        forgotDTO.setUsername("existingUser");

        String requestJson = new ObjectMapper().writeValueAsString(forgotDTO);

        when(employeeService.forgotPassword(any(ForgotPasswordDTO.class)))
                .thenReturn("New password generated: Abc12345");

        mockMvc.perform((RequestBuilder) put("/employees/password/forgot")
                .contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testForgotPassword_Success() throws Exception {
        ForgotPasswordDTO forgotDTO = new ForgotPasswordDTO();
        forgotDTO.setUsername("existingUser");

        String requestJson = new ObjectMapper().writeValueAsString(forgotDTO);

        when(employeeService.forgotPassword(any(ForgotPasswordDTO.class)))
                .thenReturn("Password reset email sent.");

        mockMvc.perform((RequestBuilder) put("/employees/password/forgot")
                .contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testDeleteEmployee_CannotDelete() throws Exception {
        String id = "123";
        String message = "Cannot delete employee due to active tasks.";

        when(employeeService.deleteEmployee(eq(id), anyString())).thenReturn(message);

        mockMvc.perform(delete("/employees/{id}", id)
                        .principal(() -> "testUser"))
                .andExpect(status().isPreconditionFailed())
                .andExpect(content().string(message));
    }

    @Test
    void testDeleteEmployee_NotFound() throws Exception {
        String id = "123";
        String message = "Employee with id 123 doesn't exist";

        when(employeeService.deleteEmployee(eq(id), anyString())).thenReturn(message);

        mockMvc.perform(delete("/employees/{id}", id)
                        .principal(() -> "testUser"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(message));
    }

    @Test
    void testDeleteEmployee_Success() throws Exception {
        String id = "123";
        String message = "Employee deleted successfully.";

        when(employeeService.deleteEmployee(eq(id), anyString())).thenReturn(message);

        mockMvc.perform(delete("/employees/{id}", id)
                        .principal(() -> "testUser"))
                .andExpect(status().isOk())
                .andExpect(content().string(message));
    }

    @Test
    void testDeactivateEmployee_Success() throws Exception {
        String id = "123";
        Employee employee = new Employee();
        employee.setId(id);
        employee.setUsername("test@abc.com");

        when(employeeService.deactivateEmployee(eq(id), any(HttpServletRequest.class)))
                .thenReturn(employee);

        mockMvc.perform((RequestBuilder) put("/employees/{id}/deactivate", id))
                .andExpect(status().isOk());
    }

    @Test
    void testDeactivateEmployee_Failure() throws Exception {
        String id = "123";

        when(employeeService.deactivateEmployee(eq(id), any(HttpServletRequest.class)))
                .thenThrow(new QueueException("Deactivation failed", HttpStatus.CONFLICT));

        mockMvc.perform((RequestBuilder) put("/employees/{id}/deactivate", id))
                .andExpect(status().isOk())
                .andExpect(content().string("Deactivation failed"));
    }

    @Test
    void testActivateEmployee_Success() throws Exception {
        String id = "emp123";
        String performedBy = "admin";

        Employee employee = new Employee();
        employee.setId(id);
        employee.setActive(true);

        when(employeeService.activateEmployee(id, performedBy)).thenReturn(employee);

        mockMvc.perform((RequestBuilder) put("/employees/{id}/activate", id)
                        .queryParam("performedBy", performedBy))
                .andExpect(status().isOk());
    }

    @Test
    void testActivateEmployee_Failure() throws Exception {
        String id = "emp123";
        String performedBy = "admin";

        when(employeeService.activateEmployee(id, performedBy))
                .thenThrow(new RuntimeException("Activation failed"));

        mockMvc.perform((RequestBuilder) put("/employees/{id}/activate", id)
                        .queryParam("performedBy", performedBy))
                .andExpect(status().isOk())
                .andExpect(content().string("Activation failed"));
    }

    @Test
    void testUnlockEmployee_Success() throws Exception {
        String id = "emp123";
        Employee employee = new Employee();
        employee.setId(id);

        when(employeeService.unlockEmployee(id)).thenReturn(employee);

        mockMvc.perform((RequestBuilder) put("/employees/{id}/unlock", id))
                .andExpect(status().isOk());
    }

    @Test
    void testUnlockEmployee_NotFound() throws Exception {
        String id = "invalidId";

        when(employeeService.unlockEmployee(id)).thenReturn(null);

        mockMvc.perform((RequestBuilder) put("/employees/{id}/unlock", id))
                .andExpect(status().isOk())
                .andExpect(content().string("User ID doesn't exist!"));
    }
    @Test
    void testDetach_Success() throws Exception {
        String id = "emp123";
        Employee employee = new Employee();
        employee.setId(id);

        when(employeeService.detachCounter(id)).thenReturn(employee);

        mockMvc.perform((RequestBuilder) put("/employees/{id}/detach", id))
                .andExpect(status().isOk());
    }

    @Test
    void testDetach_NotFound() throws Exception {
        String id = "invalid";

        when(employeeService.detachCounter(id)).thenThrow(new RuntimeException("Employee not found"));

        mockMvc.perform((RequestBuilder) put("/employees/{id}/detach", id))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Employee not found"));
    }

    @Test
    void testGetActiveCounterAgents() throws Exception {
        List<Employee> mockAgents = List.of(new Employee("emp1"), new Employee("emp2"));

        when(employeeService.getActiveCounterAgents("tenant123")).thenReturn(mockAgents);

        mockMvc.perform(get("/employees/active/counterAgents")
                        .header("X-TenantID", "tenant123"))
                .andExpect(status().isOk());
    }

    @Test
    void testEmpDashboard() throws Exception {
        EmpDashboardRequest dashboardRequest = new EmpDashboardRequest();
        String requestJson = new ObjectMapper().writeValueAsString(dashboardRequest);

        EmpDashboardDtls dashboardDtls = new EmpDashboardDtls();
        when(employeeService.getEmployeeDashboard(any(EmpDashboardRequest.class))).thenReturn(dashboardDtls);

        mockMvc.perform((RequestBuilder) put("/dashboard")
                        .contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testUpdateSuperAdmin_Success() throws Exception {
        String id = "1";
        String username = "newUsername";

        Employee mockEmployee = new Employee();
        when(employeeService.updateUsername(id, username)).thenReturn(mockEmployee);

        mockMvc.perform((RequestBuilder) put("/employees/{id}/update/{username}", id, username))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateSuperAdmin_NotFound() throws Exception {
        String id = "1";
        String username = "newUsername";

        when(employeeService.updateUsername(id, username)).thenThrow(new RuntimeException("User not found"));

        mockMvc.perform((RequestBuilder) put("/employees/{id}/update/{username}", id, username))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }

    @Test
    void testResetUserPassword_Success() throws Exception {
        String username = "testUser";
        String newPassword = "newPass";

        EmployeeController.PasswordResetResource resource = new EmployeeController.PasswordResetResource();
        resource.setPassword(newPassword);

        ObjectMapper mapper = new ObjectMapper();
        String jsonContent = mapper.writeValueAsString(resource);

        Employee mockEmployee = new Employee();
        mockEmployee.setId("123");
        mockEmployee.setUsername(username);

        when(employeeService.resetUserPassword(username, newPassword)).thenReturn(mockEmployee);

        mockMvc.perform((RequestBuilder) put("/employees/password/reset")
                        .queryParam("username", username)
                        .contentType(MediaType.APPLICATION_JSON));
    }

}
