package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** REST controller for HelpRequest (index + create) */
@Tag(name = "HelpRequest")
@RestController
@RequestMapping("/api/helprequest")
@Slf4j
public class HelpRequestController extends ApiController {

  @Autowired private HelpRequestRepository helpRequestRepository;

  @Operation(summary = "List all help requests")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<HelpRequest> allHelpRequests() {
    return helpRequestRepository.findAll();
  }

  @Operation(summary = "Create a new help request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public HelpRequest postHelpRequest(
      @Parameter(name = "requesterEmail") @RequestParam String requesterEmail,
      @Parameter(name = "teamId") @RequestParam String teamId,
      @Parameter(name = "tableOrBreakoutRoom") @RequestParam String tableOrBreakoutRoom,
      @Parameter(name = "explanation") @RequestParam String explanation,
      @Parameter(name = "solved") @RequestParam boolean solved,
      @Parameter(name = "requestTime", description = "ISO 8601 e.g. 2022-01-03T00:00:00")
          @RequestParam("requestTime")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime requestTime) {

    HelpRequest hr =
        HelpRequest.builder()
            .requesterEmail(requesterEmail)
            .teamId(teamId)
            .tableOrBreakoutRoom(tableOrBreakoutRoom)
            .requestTime(requestTime)
            .explanation(explanation)
            .solved(solved)
            .build();

    return helpRequestRepository.save(hr);
  }

  @Operation(summary = "Get a single help request by id")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public HelpRequest getById(@Parameter(name = "id") @RequestParam Long id) {
    return helpRequestRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, id));
  }
}
