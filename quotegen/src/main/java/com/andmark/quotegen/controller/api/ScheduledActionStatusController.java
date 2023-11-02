package com.andmark.quotegen.controller.api;

import com.andmark.quotegen.dto.ScheduledActionStatusDTO;
import com.andmark.quotegen.service.SchedulledActionStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scheduled")
@Slf4j
public class ScheduledActionStatusController {
    private final SchedulledActionStatusService scheduledActionStatusService;

    public ScheduledActionStatusController(SchedulledActionStatusService scheduledActionStatusService) {
        this.scheduledActionStatusService = scheduledActionStatusService;
    }

    @GetMapping("/random")
    public ResponseEntity<ScheduledActionStatusDTO> getSchedulledActionStatus() {
        log.debug("schedulled controller: getSchedulledActionStatus");
        ScheduledActionStatusDTO schedulledActionStatusDTO = scheduledActionStatusService.getSchedulledActionStatus();
        log.debug("get schedulledActionStatusDTO: {}", schedulledActionStatusDTO);
        return ResponseEntity.ok(schedulledActionStatusDTO);
    }

    @PostMapping("/update")
    public ResponseEntity<Void> updateSchedulledActionStatus(@RequestBody ScheduledActionStatusDTO scheduledActionStatusDTO) {
        log.debug("schedulled controller: updateSchedulledActionStatus");
        scheduledActionStatusService.updateSchedulledActionStatus(scheduledActionStatusDTO);
        log.debug("updated schedulledActionStatusDTO");
        return ResponseEntity.ok().build();
    }

}
