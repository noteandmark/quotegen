package com.andmark.quotegen.service;

import com.andmark.quotegen.domain.ScheduledActionStatus;
import com.andmark.quotegen.dto.ScheduledActionStatusDTO;
import com.andmark.quotegen.repository.ScheduledActionStatusRepository;
import com.andmark.quotegen.util.MapperConvert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulledActionStatusServiceTest {
    @Mock
    private ScheduledActionStatusRepository scheduledActionStatusRepository;
    @Mock
    private MapperConvert<ScheduledActionStatus, ScheduledActionStatusDTO> mapper;
    @InjectMocks
    private SchedulledActionStatusService schedulledActionStatusService;

    @Test
    void testGetSchedulledActionStatus_Found() {
        LocalDateTime now = LocalDateTime.now();
        ScheduledActionStatus mockStatus = new ScheduledActionStatus();
        mockStatus.setLastExecuted(now);
        ScheduledActionStatusDTO mockStatusDTO = new ScheduledActionStatusDTO();
        mockStatusDTO.setLastExecuted(now);

        when(scheduledActionStatusRepository.findById(1L)).thenReturn(Optional.of(mockStatus));
        when(schedulledActionStatusService.convertToDTO(mockStatus)).thenReturn(mockStatusDTO);

        ScheduledActionStatusDTO result = schedulledActionStatusService.getSchedulledActionStatus();

        assertNotNull(result);
        assertEquals(mockStatus.getLastExecuted(), result.getLastExecuted());
    }

    @Test
    void testGetSchedulledActionStatus_NotFound() {
        when(scheduledActionStatusRepository.findById(1L)).thenReturn(Optional.empty());

        ScheduledActionStatusDTO result = schedulledActionStatusService.getSchedulledActionStatus();

        assertNull(result);
    }

    @Test
    void testUpdateSchedulledActionStatus_CreateNew() {
        ScheduledActionStatusDTO mockDTO = new ScheduledActionStatusDTO();
        mockDTO.setLastExecuted(LocalDateTime.now());

        ScheduledActionStatus result = new ScheduledActionStatus();

        when(scheduledActionStatusRepository.save(any())).thenReturn(result);

        assertDoesNotThrow(() -> schedulledActionStatusService.updateSchedulledActionStatus(mockDTO));

        verify(scheduledActionStatusRepository).save(any());
    }

    @Test
    void testUpdateSchedulledActionStatus_UpdateExisting() {
        ScheduledActionStatusDTO mockDTO = new ScheduledActionStatusDTO();
        mockDTO.setId(1L);
        mockDTO.setLastExecuted(LocalDateTime.now());

        ScheduledActionStatus mockStatus = new ScheduledActionStatus();

        when(scheduledActionStatusRepository.findById(1L)).thenReturn(Optional.of(mockStatus));
        when(scheduledActionStatusRepository.save(any())).thenReturn(mockStatus);

        assertDoesNotThrow(() -> schedulledActionStatusService.updateSchedulledActionStatus(mockDTO));

        verify(scheduledActionStatusRepository).save(any());
    }

}