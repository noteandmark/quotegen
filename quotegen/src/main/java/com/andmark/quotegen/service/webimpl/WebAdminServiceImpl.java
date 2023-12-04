package com.andmark.quotegen.service.webimpl;

import com.andmark.quotegen.dto.AvailableDayResponseDTO;
import com.andmark.quotegen.dto.QuoteDTO;
import com.andmark.quotegen.service.QuoteService;
import com.andmark.quotegen.service.WebAdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WebAdminServiceImpl implements WebAdminService {
    private final QuoteService quoteService;

    public WebAdminServiceImpl(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @Override
    public void randomPublish(QuoteDTO pendingQuote) {
        log.debug("web admin service randomPublish");
        AvailableDayResponseDTO availableDayResponseDTO = quoteService.getAvailableDays();
        log.debug("availableDayResponseDTO = {}", availableDayResponseDTO);

        // Set the pending time for the quote
        pendingQuote.setPendingTime(availableDayResponseDTO.getAvailableDay());

        log.debug("try to save pendingQuote: {}", pendingQuote);
        quoteService.pendingQuote(pendingQuote);
        log.debug("success saved in randomPublish web admin service");
    }

    @Override
    public void chosenPublish(QuoteDTO pendingQuote) {
        log.debug("web admin service chosenPublish");
        quoteService.pendingQuote(pendingQuote);
        log.debug("success saved in chosenPublish web admin service");
    }

}
