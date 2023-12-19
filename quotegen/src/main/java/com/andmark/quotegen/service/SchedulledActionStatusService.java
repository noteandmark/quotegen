package com.andmark.quotegen.service;

import com.andmark.quotegen.domain.ScheduledActionStatus;
import com.andmark.quotegen.dto.ScheduledActionStatusDTO;
import com.andmark.quotegen.repository.ScheduledActionStatusRepository;
import com.andmark.quotegen.util.MapperConvert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class SchedulledActionStatusService {
    private final ScheduledActionStatusRepository scheduledActionStatusRepository;
    private final MapperConvert<ScheduledActionStatus, ScheduledActionStatusDTO> mapper;

    public SchedulledActionStatusService(ScheduledActionStatusRepository scheduledActionStatusRepository, MapperConvert<ScheduledActionStatus, ScheduledActionStatusDTO> mapper) {
        this.scheduledActionStatusRepository = scheduledActionStatusRepository;
        this.mapper = mapper;
    }

    public ScheduledActionStatusDTO getSchedulledActionStatus() {
        log.debug("schedulledActionStatusService: getSchedulledActionStatus");
        ScheduledActionStatus scheduledActionStatus = scheduledActionStatusRepository.findById(1L).orElse(new ScheduledActionStatus());
        log.debug("return scheduledActionStatus = {}", scheduledActionStatus.getLastExecuted());
        return convertToDTO(scheduledActionStatus);
    }

    ScheduledActionStatusDTO convertToDTO(ScheduledActionStatus scheduledActionStatus) {
        return mapper.convertToDTO(scheduledActionStatus, ScheduledActionStatusDTO.class);
    }

    ScheduledActionStatus convertToEntity(ScheduledActionStatusDTO scheduledActionStatusDTO) {
        return mapper.convertToEntity(scheduledActionStatusDTO, ScheduledActionStatus.class);
    }

    @Transactional
    public void updateSchedulledActionStatus(ScheduledActionStatusDTO scheduledActionStatusDTO) {
        log.debug("schedulledActionStatusService: update with time: {}", scheduledActionStatusDTO.getLastExecuted());

        ScheduledActionStatus scheduledActionStatus = new ScheduledActionStatus();
        if (scheduledActionStatusDTO.getId() != null) {
            log.debug("id scheduled action: {}", scheduledActionStatusDTO.getId());
            Optional<ScheduledActionStatus> optionalStatus = scheduledActionStatusRepository.findById(scheduledActionStatusDTO.getId());
            scheduledActionStatus = optionalStatus.orElseGet(ScheduledActionStatus::new);
        }

        scheduledActionStatus.setLastExecuted(scheduledActionStatusDTO.getLastExecuted());
        scheduledActionStatusRepository.save(scheduledActionStatus);
        log.debug("scheduledActionStatus updated or saved");
    }
}
