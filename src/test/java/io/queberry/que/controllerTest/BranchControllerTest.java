package io.queberry.que.controllerTest;

import io.queberry.que.controller.BranchController;
import io.queberry.que.dto.*;
import io.queberry.que.entity.Branch;
import io.queberry.que.exception.DataNotFoundException;
import io.queberry.que.service.BranchService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.RequestEntity.post;
import static org.springframework.http.RequestEntity.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BranchController.class)
public class BranchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BranchService branchService;

    @Test
    void getActiveBranches_ShouldReturnActiveBranchDTOs() throws Exception {
        BranchDTO dto1 = new BranchDTO();
        dto1.setId("123");
        dto1.setActive(true);
        BranchDTO dto2 = new BranchDTO();
        dto2.setId("111");
        dto2.setActive(true);
        Set<BranchDTO> activeBranches = Set.of(dto1, dto2);

        when(branchService.getActiveBranches()).thenReturn(activeBranches);

        mockMvc.perform(get("/branches/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
    @Test
    void getActiveWBranches_ReturnsWebPrinterBranches() throws Exception {
        BranchDTO dto1 = new BranchDTO();
        dto1.setActive(true);
        BranchDTO dto2 = new BranchDTO();
        dto2.setActive(false);
        Set<BranchDTO> activeWebPrinterBranches = Set.of(dto1, dto2);

        when(branchService.getActiveWebPrinterBranches()).thenReturn(activeWebPrinterBranches);

        mockMvc.perform(get("/branches/webPrinter/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
    @Test
    void getActiveBranchCount_ReturnsCount() throws Exception {
        String expectedCount = "5";

        when(branchService.getActiveBranchCount()).thenReturn(expectedCount);

        mockMvc.perform(get("/branches/activeCount")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
    @Test
    void getAllBranches_ReturnsListOfBranchDTOs() throws Exception {
        BranchDTO branch1 = new BranchDTO();
        branch1.setId("123");
        BranchDTO branch2 = new BranchDTO();
        branch2.setId("122");
        List<BranchDTO> branchList = List.of(branch1, branch2);

        when(branchService.getAllBranches()).thenReturn(branchList);

        mockMvc.perform(get("/branches")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
    @Test
    void getBranchById_ReturnsBranchDTO_WhenValidId() throws Exception {
        String branchId = "123";
        BranchDTO branchDTO = new BranchDTO();
        branchDTO.setActive(true);

        when(branchService.getBranchById(branchId)).thenReturn(branchDTO);

        mockMvc.perform(get("/branches/{id}", branchId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
    @Test
    void createBranch_ReturnsSuccessMessage() throws Exception {
        Branch branch = new Branch();
        branch.setName("New Branch");
        branch.setActive(true);
        when(branchService.createBranch(any(Branch.class))).thenReturn(branch);

        mockMvc.perform((RequestBuilder) post("/branches/save")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
    @Test
    void editBranch_ReturnsSuccessMessage_WhenBranchUpdated() throws Exception {
        String branchId = "123";
        BranchRequest request = new BranchRequest();
        request.setName("Updated Branch");

        Branch updatedBranch = new Branch();
        updatedBranch.setName("Updated Branch");

        when(branchService.updateBranch(eq(branchId), any(BranchRequest.class))).thenReturn(updatedBranch);

        mockMvc.perform((RequestBuilder) put("/branches/branch/{id}", branchId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
    @Test
    void deleteBranch_ReturnsTrue_WhenDeleted() throws Exception {
        String branchId = "123";
        when(branchService.deleteBranch(branchId)).thenReturn(true);

        mockMvc.perform(delete("/branches/{id}", branchId))
                .andExpect(status().isOk());
    }
    @Test
    void getBranchesByRegion_ReturnsPageOfBranches_WhenContentExists() throws Exception {
        String regionId = "north";
        int page = 0;
        int size = 2;

        Branch branch1 = new Branch();
        branch1.setName("Branch 1");
        Branch branch2 = new Branch();
        branch2.setName("Branch 2");

        List<Branch> branchList = List.of(branch1, branch2);
        Page<Branch> branchPage = new PageImpl<>(branchList);

        when(branchService.getBranchesByRegionId(eq(regionId), any(Pageable.class))).thenReturn(branchPage);

        mockMvc.perform(get("/branches/region")
                        .param("regionId", regionId)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
    @Test
    void activateBranch_ReturnsSuccessMessage() throws Exception {
        String branchId = "123";
        Branch branch = new Branch();
        branch.setName("Activated Branch");

        when(branchService.activateBranch(branchId)).thenReturn(branch);

        mockMvc.perform((RequestBuilder) put("/branch/{id}/activate", branchId))
                .andExpect(status().isOk());
    }

    @Test
    void activateBranch_ReturnsNotFound_WhenBranchNotFound() throws Exception {
        String branchId = "999";

        when(branchService.activateBranch(branchId)).thenThrow(new DataNotFoundException("Branch not found"));

        mockMvc.perform((RequestBuilder) put("/branch/{id}/activate", branchId))
                .andExpect(status().isNotFound());
    }

    @Test
    void activateBranch_ReturnsInternalServerError_OnException() throws Exception {
        String branchId = "123";

        when(branchService.activateBranch(branchId)).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform((RequestBuilder) put("/branch/{id}/activate", branchId))
                .andExpect(status().isInternalServerError());
    }
    @Test
    void deActivateBranch_ReturnsSuccessMessage() throws Exception {
        String branchId = "123";
        Branch branch = new Branch();
        branch.setName("Deactivated Branch");

        when(branchService.deActivateBranch(branchId)).thenReturn(branch);

        mockMvc.perform((RequestBuilder) put("/branch/{id}/deActivate", branchId))
                .andExpect(status().isOk());
    }

    @Test
    void deActivateBranch_ReturnsNotFound_WhenBranchNotFound() throws Exception {
        String branchId = "999";

        when(branchService.deActivateBranch(branchId)).thenThrow(new DataNotFoundException("Branch not found"));

        mockMvc.perform((RequestBuilder) put("/branch/{id}/deActivate", branchId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deActivateBranch_ReturnsInternalServerError_OnException() throws Exception {
        String branchId = "123";

        when(branchService.deActivateBranch(branchId)).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform((RequestBuilder) put("/branch/{id}/deActivate", branchId))
                .andExpect(status().isInternalServerError());
    }
    @Test
    void getFilterByName_ReturnsFilteredBranches() throws Exception {
        String regionId = "north";
        String branchName = "Central";

        BranchDTO branchDto1 = new BranchDTO();
        branchDto1.setName("Central Branch 1");
        BranchDTO branchDto2 = new BranchDTO();
        branchDto2.setName("Central Branch 2");

        List<BranchDTO> branchDtoList = List.of(branchDto1, branchDto2);
        Page<BranchDTO> branchPage = new PageImpl<>(branchDtoList);

        when(branchService.filterBranchesByName(any(HttpServletRequest.class), eq(regionId), eq(branchName), any(Pageable.class)))
                .thenReturn(branchPage);

        mockMvc.perform(get("/branches/{id}/filterByName", regionId)
                        .param("branch", branchName)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
    @Test
    void assignServiceGroup_ReturnsUpdatedBranch_WhenSuccessful() throws Exception {
        String branchId = "123";
        ServiceGroupRequest request = new ServiceGroupRequest();
        request.setDisplayName("New Service Group");

        Branch updatedBranch = new Branch();
        updatedBranch.setName("Branch 123");

        when(branchService.assignServiceGroup(eq(branchId), any(ServiceGroupRequest.class))).thenReturn(updatedBranch);

        mockMvc.perform((RequestBuilder) put("/branches/{id}/assignServiceGroup", branchId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void assignServiceGroup_ReturnsConflict_WhenExceptionThrown() throws Exception {
        String branchId = "123";

        when(branchService.assignServiceGroup(eq(branchId), any(ServiceGroupRequest.class)))
                .thenThrow(new RuntimeException("Conflict error"));

        mockMvc.perform((RequestBuilder) put("/branches/{id}/assignServiceGroup", branchId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }
    @Test
    void getAllServiceGroups_ReturnsServiceGroups() throws Exception {
        String branchKey = "branch123";

        ServiceGroupDTO sg1 = new ServiceGroupDTO();
        sg1.setName("Group A");
        ServiceGroupDTO sg2 = new ServiceGroupDTO();
        sg2.setName("Group B");

        Set<ServiceGroupDTO> serviceGroups = Set.of(sg1, sg2);

        when(branchService.getServiceGroupsByBranchKey(branchKey)).thenReturn(serviceGroups);

        mockMvc.perform(get("/branches/services/{key}", branchKey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
    @Test
    void getCapacity_ReturnsCapacity_WhenFound() throws Exception {
        String branchKey = "key123";
        Capacity capacity = new Capacity();
        capacity.setPeopleInBranch(100);
        capacity.setDeskAttending(40);

        when(branchService.getBranchCapacity(branchKey)).thenReturn(capacity);

        mockMvc.perform(get("/branches/branchCapacity/{key}", branchKey))
                .andExpect(status().isOk());
    }
}
