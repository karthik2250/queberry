package ServiceImplTest;

import io.queberry.que.dto.EmpDashboardDtls;
import io.queberry.que.dto.EmpDashboardRequest;
import io.queberry.que.dto.EmployeeRequest;
import io.queberry.que.dto.PasswordResetDTO;
import io.queberry.que.entity.Employee;
import io.queberry.que.entity.EmployeeData;
import io.queberry.que.exception.QueueException;
import io.queberry.que.repository.EmployeeRepository;
import io.queberry.que.service.impl.EmployeeServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceImplTest {

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createEmployee_whenUserDoesNotExist_createsSuccessfully() {
        EmployeeData data = new EmployeeData();
        data.setUsername("sandip@queberry.com");
        data.setPassword("securePass@123");
        data.setFirstname("Sandip");
        data.setLastname("A V");
        data.setActive(true);
        data.setRoles((java.util.List<String>) Set.of("role-id"));

        when(employeeRepository.findByUsername(data.getUsername())).thenReturn(null);
        when(passwordEncoder.encode(data.getPassword())).thenReturn("encodedPass");

        Employee savedEmployee = new Employee();
        savedEmployee.setUsername(data.getUsername());
        savedEmployee.setPassword("encodedPass");

        when(employeeRepository.save(any(Employee.class))).thenReturn(savedEmployee);

        employeeService.createEmployee(data);

        verify(employeeRepository).findByUsername(data.getUsername());
        verify(passwordEncoder).encode(data.getPassword());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void createEmployee_whenUserExists_throwsException() {
        EmployeeData data = new EmployeeData();
        data.setUsername("existing@user.com");

        when(employeeRepository.findByUsername(data.getUsername()))
                .thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                employeeService.createEmployee(data));

        assertEquals("Username already exists", exception.getMessage());
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void testResetPassword_Success() {
        PasswordResetDTO dto = new PasswordResetDTO();
        dto.setUsername("sandip@queberry.com");
        dto.setNewPassword("securePass@123");
        Employee mockEmployee = new Employee();
        mockEmployee.setUsername(dto.getUsername());

        when(employeeRepository.findByUsername(dto.getUsername()))
                .thenReturn((mockEmployee));

        String result = employeeService.resetPassword(dto, request);

        assertEquals("Password reset successful", result);
        verify(employeeRepository).save(mockEmployee);
    }

    @Test
    void testResetPassword_PasswordMismatch() {
        PasswordResetDTO dto = new PasswordResetDTO();
        dto.setUsername("sandip@queberry.com");
        dto.setNewPassword("pass1");

        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.resetPassword(dto, request);
        });

        verify(employeeRepository, never()).save(any());
    }

    @Test
    void testResetPassword_UserNotFound() {
        PasswordResetDTO dto = new PasswordResetDTO();
        dto.setUsername("unknown@user.com");
        dto.setNewPassword("securePass@123");

        when(employeeRepository.findByUsername(dto.getUsername()))
                .thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> {
            employeeService.resetPassword(dto, request);
        });

        verify(employeeRepository, never()).save(any());
    }
    @Test
    void testFilterEmployeesByUsername() {
        String username = "sandip@queberry.com";
        Pageable pageable = PageRequest.of(0, 10);

        Employee employee = new Employee();
        employee.setUsername(username);

        List<Employee> employees = List.of(employee);
        Page<Employee> page = new PageImpl<>(employees, pageable, employees.size());

       //when(employeeRepository.findByUsername(eq(username), eq(pageable))).thenReturn(page);

        Page<Employee> result = employeeService.filterEmployeesByUsername(username, pageable, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(username, result.getContent().get(0).getUsername());
    }

    @Test
    void testGetEmployeeById_Found() {
        String id = "a92bc5ee-4e3a-40a9-810f-5b28ad64ff1c";
        Employee employee = new Employee();
        employee.setId(id);
        employee.setUsername("sandip@queberry.com");

        when(employeeRepository.findById(id)).thenReturn(Optional.of(employee));

        EmployeeRequest result = employeeService.getEmployeeById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("sandip@queberry.com", result.getUsername());
    }

    @Test
    void testGetEmployeeById_NotFound() {
        String id = "999";
        when(employeeRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            employeeService.getEmployeeById(id);
        });

        assertEquals("Employee not found", exception.getMessage());
    }

    @Test
    void testUpdateEmployee_Success() {
        String id = "a92bc5ee-4e3a-40a9-810f-5b28ad64ff1c";
        EmployeeData employeeData = new EmployeeData();
        employeeData.setFirstname("sandip");
        employeeData.setLastname("A V");
        employeeData.setUsername("sandip@queberry.com");

        Employee existingEmployee = new Employee();
        existingEmployee.setId(id);

        when(employeeRepository.findById(id)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(existingEmployee);

        assertDoesNotThrow(() -> employeeService.updateEmployee(id, employeeData));

        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployee_NotFound() {
        String id = "999";
        EmployeeData employeeData = new EmployeeData();

        when(employeeRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            employeeService.updateEmployee(id, employeeData);
        });

        assertEquals("Employee not found", exception.getMessage());
    }

    @Test
    void testAssignCounter_Success() {
        String employeeId = "a92bc5ee-4e3a-40a9-810f-5b28ad64ff1c";
        String counterId = "counter001";

        Employee employee = new Employee();
        employee.setId(employeeId);

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));

        Employee updatedEmployee = employeeService.assignCounter(employeeId, counterId);

        assertEquals(counterId, updatedEmployee.getCounter());
        verify(employeeRepository).save(employee);
    }

    @Test
    void testAssignCounter_EmployeeNotFound() {
        String employeeId = "999";
        String counterId = "counter001";

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            employeeService.assignCounter(employeeId, counterId);
        });

        assertEquals("Employee not found", exception.getMessage());
    }

    @Test
    void testDeleteEmployee_EmployeeDoesNotExist() {
        String id = "a92bc5ee-4e3a-40a9-810f-5b28ad64ff1c";
        String username = "testUser";

        when(employeeRepository.existsById(id)).thenReturn(false);

        String result = employeeService.deleteEmployee(id, username);

        assertTrue(result.contains("doesn't exist"));
    }

    @Test
    void testDeleteEmployee_CannotDelete() {
        String id = "a92bc5ee-4e3a-40a9-810f-5b28ad64ff1c";
        String username = "testUser";

        when(employeeRepository.existsById(id)).thenReturn(true);

        // Mock your logic that causes "Cannot delete" scenario
        // e.g., employee is linked to active tasks

        String result = employeeService.deleteEmployee(id, username);

        assertTrue(result.contains("Cannot delete"));
    }

    @Test
    void testDeleteEmployee_Success() {
        String id = "a92bc5ee-4e3a-40a9-810f-5b28ad64ff1c";
        String username = "testUser";

        when(employeeRepository.existsById(id)).thenReturn(true);
        doNothing().when(employeeRepository).deleteById(id);

        String result = employeeService.deleteEmployee(id, username);

        assertEquals("Employee deleted successfully.", result);
    }

    @Test
    void testDeactivateEmployee_Success() {
        String id = "a92bc5ee-4e3a-40a9-810f-5b28ad64ff1c";
        Employee emp = new Employee();
        emp.setId(id);
        emp.setActive(true);

        when(employeeRepository.findById(id)).thenReturn(Optional.of(emp));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));

        Employee result = employeeService.deactivateEmployee(id, mock(HttpServletRequest.class));

        assertFalse(result.isActive());
    }

    @Test
    void testDeactivateEmployee_NotFound() {
        String id = "999";
        when(employeeRepository.findById(id)).thenReturn(Optional.empty());

        QueueException ex = assertThrows(QueueException.class, () ->
                employeeService.deactivateEmployee(id, mock(HttpServletRequest.class))
        );

        assertEquals("Employee not found", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void testActivateEmployee_Success() {
        String id = "a92bc5ee-4e3a-40a9-810f-5b28ad64ff1c";
        String performedBy = "admin";

        Employee emp = new Employee();
        emp.setId(id);
        emp.setActive(false);

        when(employeeRepository.findById(id)).thenReturn(Optional.of(emp));
        when(employeeRepository.save(any(Employee.class))).thenAnswer
                (inv -> inv.getArgument(0));

        Employee result = employeeService.activateEmployee(id, performedBy);

        assertTrue(result.isActive());
    }

    @Test
    void testActivateEmployee_NotFound() {
        when(employeeRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            employeeService.activateEmployee("invalid", "admin");
        });
    }

    @Test
    void testUnlockEmployee_Success() {
        String id = "a92bc5ee-4e3a-40a9-810f-5b28ad64ff1c";
        Employee employee = new Employee();
        employee.setId(id);
        employee.setLocked(true);

        when(employeeRepository.findById(id)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).
                thenAnswer(inv -> inv.getArgument(0));

        Employee result = employeeService.unlockEmployee(id);

        assertNotNull(result);
        assertFalse(result.isLocked());
    }

    @Test
    void testUnlockEmployee_NotFound() {
        when(employeeRepository.findById("invalid")).thenReturn(Optional.empty());

        Employee result = employeeService.unlockEmployee("invalid");

        assertNull(result);
    }

    @Test
    void testGetActiveCounterAgents() {
        List<Employee> mockAgents = List.of(new Employee("emp1"), new Employee("emp2"));

       // when(employeeRepository.findByActiveTrueAndTenantId("tenant123")).thenReturn(mockAgents);

        List<Employee> result = employeeService.getActiveCounterAgents("tenant123");

        assertEquals(2, result.size());
    }

    @Test
    void testGetEmployeeDashboard() {
        EmpDashboardRequest request = new EmpDashboardRequest();
        EmpDashboardDtls expected = new EmpDashboardDtls();

        EmpDashboardDtls actual = employeeService.getEmployeeDashboard(request);

        assertNotNull(actual);
    }
    @Test
    void testUpdateUsername_Success() {
        String id = "1";
        String newUsername = "updatedUser";

        Employee employee = new Employee();
        employee.setId(id);
        employee.setUsername("oldUser");

        when(employeeRepository.findById(id)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee updated = employeeService.updateUsername(id, newUsername);

        assertEquals(newUsername, updated.getUsername());
    }

    @Test
    void testUpdateUsername_NotFound() {
        String id = "1";
        when(employeeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> employeeService.updateUsername(id, "any"));
    }
    @Test
    void testResetUserPassword_Success() {
        String username = "testUser";
        String newPassword = "newPass";
        String encodedPassword = "encodedPass";

        Employee employee = new Employee();
        employee.setId("123");
        employee.setUsername(username);

        when(employeeRepository.findByUsername(username)).thenReturn((employee));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee result = employeeService.resetUserPassword(username, newPassword);

        assertEquals(employee, result);
        assertEquals(encodedPassword, employee.getPassword());
    }

    @Test
    void testResetUserPassword_UserNotFound() {
        String username = "unknown";

        when(employeeRepository.findByUsername(username)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> employeeService.resetUserPassword(username, "newPass"));
    }
}
