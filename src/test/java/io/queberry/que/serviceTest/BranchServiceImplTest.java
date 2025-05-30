package io.queberry.que.serviceTest;

import io.queberry.que.dto.*;
import io.queberry.que.entity.*;
import io.queberry.que.exception.DataNotFoundException;
import io.queberry.que.mapper.BranchMapper;
import io.queberry.que.repository.*;
import io.queberry.que.service.impl.BranchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BranchServiceImplTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private BranchMapper branchMapper;

    @InjectMocks
    private BranchServiceImpl branchService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AssistanceRepository assistanceRepository;

    @Mock
    private CounterRepository counterRepository;

    @Mock
    private ServiceGroupRepository serviceGroupRepository;

    private Branch branch;
    private BranchDTO branchDTO;

    @BeforeEach
    void setUp() {
        branch = new Branch();
        branch.setName("Test Branch");
        branch.setActive(true);

        branchDTO = new BranchDTO();
    }

    @Test
    void getActiveBranches_ReturnsMappedDTOs() {
        when(branchRepository.findByActiveTrue()).thenReturn(Set.of(branch));
        when(branchMapper.entityToDto(branch)).thenReturn(branchDTO);

        Set<BranchDTO> result = branchService.getActiveBranches();

        assertEquals(1, result.size());
        assertTrue(result.contains(branchDTO));
    }
    @Test
    void getActiveWebPrinterBranches_ReturnsMappedBranchDTOs() {
        Branch branch1 = new Branch();
        branch1.setName("Branch A");

        Branch branch2 = new Branch();
        branch2.setName("Branch B");

        BranchDTO dto1 = new BranchDTO();
        dto1.setName("Branch A");

        BranchDTO dto2 = new BranchDTO();
        dto2.setName("Branch B");

        Set<Branch> branchSet = new LinkedHashSet<>(Set.of(branch1, branch2));

        when(branchRepository.findByActiveTrue()).thenReturn(branchSet);
        when(branchMapper.entityToDto(branch1)).thenReturn(dto1);
        when(branchMapper.entityToDto(branch2)).thenReturn(dto2);

        Set<BranchDTO> result = branchService.getActiveWebPrinterBranches();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(dto1));
        assertTrue(result.contains(dto2));

    }
    @Test
    void getActiveBranchCount_ReturnsCorrectCount() {
        String expectedCount = "5";

        when(branchRepository.countAllByActiveTrue()).thenReturn(expectedCount);
        String result = branchService.getActiveBranchCount();
        assertEquals(expectedCount, result);
        verify(branchRepository).countAllByActiveTrue();
    }
    @Test
    void getAllBranches_ReturnsMappedBranchDTOs() {
        Branch branch1 = new Branch();
        branch1.setName("Branch A");

        Branch branch2 = new Branch();
        branch2.setName("Branch B");

        List<Branch> branchList = List.of(branch1, branch2);

        BranchDTO dto1 = new BranchDTO();
        dto1.setName("Branch A");

        BranchDTO dto2 = new BranchDTO();
        dto2.setName("Branch B");

        List<BranchDTO> dtoList = List.of(dto1, dto2);

        when(branchRepository.findAll()).thenReturn(branchList);
        when(branchMapper.mapList(branchList)).thenReturn(dtoList);

        List<BranchDTO> result = branchService.getAllBranches();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Branch A", result.get(0).getName());
        assertEquals("Branch B", result.get(1).getName());

        verify(branchRepository).findAll();
        verify(branchMapper).mapList(branchList);
    }
    @Test
    void getBranchById_ReturnsBranchDTO_WhenFound() throws DataNotFoundException {
        String id = "branch123";
        Branch branch = new Branch();
        branch.setName("Branch 123");

        BranchDTO dto = new BranchDTO();
        dto.setName("Branch 123");

        when(branchRepository.findById(id)).thenReturn(Optional.of(branch));
        when(branchMapper.entityToDto(branch)).thenReturn(dto);

        BranchDTO result = branchService.getBranchById(id);
        assertNotNull(result);
        assertEquals("Branch 123", result.getName());

        verify(branchRepository).findById(id);
        verify(branchMapper).entityToDto(branch);
    }

    @Test
    void getBranchById_ThrowsException_WhenNotFound() {

        String id = "invalid-id";
        when(branchRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(DataNotFoundException.class, () -> branchService.getBranchById(id));
        verify(branchRepository).findById(id);
        verify(branchMapper, never()).entityToDto(any());
    }
    @Test
    void createBranch_SavesBranchSuccessfully() {

        Branch inputBranch = new Branch();
        inputBranch.setName("New Branch");

        Branch savedBranch = new Branch();
        savedBranch.setName("New Branch");

        when(branchRepository.save(inputBranch)).thenReturn(savedBranch);

        Branch result = branchService.createBranch(inputBranch);
        assertNotNull(result);
        assertEquals("New Branch", result.getName());

        verify(branchRepository).save(inputBranch);
    }
    @Test
    void updateBranch_UpdatesSuccessfully_WhenBranchExists() {
        String branchId = "branch123";

        Branch existingBranch = new Branch();
        existingBranch.setName(branchId);
        existingBranch.setAddress(new Address());

        BranchRequest request = new BranchRequest();
        request.setName("Updated Branch");
        request.setBusinessStart(LocalTime.of(9, 0));
        request.setBusinessEnd(LocalTime.of(18, 0));
        request.setRegion("Updated Region");
        request.setGlobalServices(true);
        request.setComPort("COM9");

        Address newAddress = new Address();
        newAddress.setZip("12345");
        newAddress.setArea("Downtown");
        newAddress.setBuilding("B1");
//        newAddress.setLocation("Main Street");
        newAddress.setStreet("1st Ave");
        newAddress.setCity("Testville");
        request.setAddress(newAddress);

        Branch updatedBranch = new Branch();
        updatedBranch.setName("Updated Branch");

        when(branchRepository.findById(branchId)).thenReturn(Optional.of(existingBranch));
        when(branchRepository.save(any(Branch.class))).thenReturn(updatedBranch);

        Branch result = branchService.updateBranch(branchId, request);

        assertNotNull(result);
        assertEquals("Updated Branch", result.getName());
        verify(branchRepository).findById(branchId);
        verify(branchRepository).save(existingBranch);
    }
    @Test
    void deleteBranch_ReturnsTrue_WhenBranchExists() {
        String branchId = "branch123";

        when(branchRepository.existsById(branchId)).thenReturn(true);

        boolean result = branchService.deleteBranch(branchId);

        assertTrue(result);
        verify(branchRepository).existsById(branchId);
        verify(branchRepository).deleteById(branchId);
    }
    @Test
    void getBranchesByRegionId_ReturnsPageOfBranches() {
        String regionId = "north-region";
        Pageable pageable = PageRequest.of(0, 10);

        Branch branch1 = new Branch();
        branch1.setName("Branch 1");
        branch1.setRegion(regionId);

        Branch branch2 = new Branch();
        branch2.setName("Branch 2");
        branch2.setRegion(regionId);

        Page<Branch> mockPage = new PageImpl<>(List.of(branch1, branch2));

        when(branchRepository.findByRegion(regionId, pageable)).thenReturn(mockPage);

        Page<Branch> result = branchService.getBranchesByRegionId(regionId, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("Branch 1", result.getContent().get(0).getName());
        assertEquals("Branch 2", result.getContent().get(1).getName());

        verify(branchRepository).findByRegion(regionId, pageable);
    }
    @Test
    void getBranchCapacity_ReturnsCorrectCapacity() {
        String branchKey = "branch-001";
        Branch branch = new Branch();
//        branch.se("123");
        branch.setBranchKey(branchKey);

        List<Assistance> assistanceList = List.of(new Assistance(), new Assistance()); // size = 2
        List<Counter> counterList = List.of(new Counter()); // size = 1

        when(branchRepository.findByBranchKey(branchKey)).thenReturn(branch);
        when(assistanceRepository.findByCreatedAtBetweenAndBranchAndStatusIn(
                any(LocalDateTime.class), any(LocalDateTime.class),
                eq(branchKey), anySet()
        )).thenReturn(assistanceList);

        when(counterRepository.findByBranchAndInUse(branch.getId(), true)).thenReturn(counterList);
        Capacity result = branchService.getBranchCapacity(branchKey);
        assertNotNull(result);
        assertEquals(branchKey, result.getBranchKey());
        assertEquals(2, result);
        assertEquals(1, result);

        verify(branchRepository).findByBranchKey(branchKey);
        verify(assistanceRepository).findByCreatedAtBetweenAndBranchAndStatusIn(any(), any(), eq(branchKey), anySet());
        verify(counterRepository).findByBranchAndInUse(branch.getId(), true);
    }
    @Test
    void getServiceGroupsByBranchKey_ReturnsDTOs_WhenBranchExists() {
        String branchKey = "branch-key-1";
        Set<String> serviceGroups = new HashSet<>(Set.of("GroupA", "GroupB"));

        Branch branch = new Branch();
        branch.setServiceGroup(serviceGroups);

        when(branchRepository.findByBranchKey(branchKey)).thenReturn(branch);

        Set<ServiceGroupDTO> result = branchService.getServiceGroupsByBranchKey(branchKey);
        assertNotNull(result);
        assertEquals(2, result.size());
        Set<String> names = result.stream().map(ServiceGroupDTO::getName).collect(Collectors.toSet());
        assertTrue(names.contains("GroupA"));
        assertTrue(names.contains("GroupB"));

        verify(branchRepository).findByBranchKey(branchKey);
    }
    @Test
    void assignServiceGroup_ShouldAddServiceGroupAndSaveBranch() {
        String branchId = "branch123";
        ServiceGroupRequest request = new ServiceGroupRequest();
        request.setName("SG1");
        request.setDisplayName("Service Group 1");
//        request.setNames(Set.of("ServiceA", "ServiceB"));

        Branch branch = new Branch();
        branch.setServiceGroup(new HashSet<>(Set.of("ExistingGroup")));

        ServiceGroup savedServiceGroup = new ServiceGroup();
        savedServiceGroup.setName(request.getName());
        savedServiceGroup.setDisplayName(request.getDisplayName());
        savedServiceGroup.setNames(request.getNames());

        when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));
        when(serviceGroupRepository.save(any(ServiceGroup.class))).thenReturn(savedServiceGroup);
        when(branchRepository.save(any(Branch.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Branch result = branchService.assignServiceGroup(branchId, request);

        // Then
        assertNotNull(result);
        assertTrue(result.getServiceGroup().contains("SG1"));
        assertTrue(result.getServiceGroup().contains("ExistingGroup"));

        verify(branchRepository).findById(branchId);
        verify(serviceGroupRepository).save(any(ServiceGroup.class));
        verify(branchRepository).save(branch);
    }
    @Test
    void activateBranch_ShouldSetActiveTrueAndSave() {

        String branchId = "branch123";
        Branch branch = new Branch();
        branch.setActive(false);

        when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));
        when(branchRepository.save(any(Branch.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Branch result = branchService.activateBranch(branchId);
        assertNotNull(result);
        assertTrue(result.isActive());

        verify(branchRepository).findById(branchId);
        verify(branchRepository).save(branch);
    }
    @Test
    void deActivateBranch_ShouldDeactivateBranchAndUpdateEmployees() {
        String branchId = "branch123";
        Branch branch = new Branch();
        branch.setActive(true);
        branch.setBranchKey("branch-key-123");

        Employee employee1 = mock(Employee.class);
        Employee employee2 = mock(Employee.class);

        Set<Employee> employees = Set.of(employee1, employee2);
        Set<String> empBranches1 = new HashSet<>(Set.of("branch-key-123", "other-branch"));
        Set<String> empBranches2 = new HashSet<>(Set.of("branch-key-123"));

        when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));
        when(branchRepository.save(any(Branch.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(employeeRepository.findByBranchesIn(Set.of(branch.getBranchKey()))).thenReturn(employees);

        when(employee1.getBranches()).thenReturn(empBranches1);
        when(employee2.getBranches()).thenReturn(empBranches2);

        Branch result = branchService.deActivateBranch(branchId);
        assertNotNull(result);
        assertFalse(result.isActive());

        verify(branchRepository).findById(branchId);
        verify(branchRepository).save(branch);
        verify(employeeRepository).findByBranchesIn(Set.of(branch.getBranchKey()));

        verify(employee1).getBranches();
        verify(employee1).setBranches(argThat(set -> !set.contains("branch-key-123")));
        verify(employeeRepository).save(employee1);

        verify(employee2).getBranches();
        verify(employee2).setBranches(argThat(set -> !set.contains("branch-key-123")));
        verify(employeeRepository).save(employee2);
    }
}
