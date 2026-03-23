package com.garemobilegb.search.controller;

import com.garemobilegb.search.dto.DestinationSuggestionResponse;
import com.garemobilegb.search.service.SearchService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/destinations")
public class DestinationController {

  private final SearchService searchService;

  public DestinationController(SearchService searchService) {
    this.searchService = searchService;
  }

  /** Autocomplétion sur les libellés de ligne / destination (distinct). */
  @GetMapping("/suggest")
  public List<DestinationSuggestionResponse> suggest(@RequestParam("q") String q) {
    return searchService.suggestDestinations(q);
  }
}
