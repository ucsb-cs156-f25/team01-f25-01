package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBOrganizations;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for UCSBOrganizations */
@Tag(name = "UCSBOrganizations")
@RequestMapping("/api/ucsborganizations")
@RestController
@Slf4j
public class UCSBOrganizationsController extends ApiController {
  @Autowired UCSBOrganizationsRepository ucsbOrganizationsRepository;

  /**
   * TThis method returns a list of all ucsb organizations.
   *
   * @return a list of all ucsb organizations
   */
  @Operation(summary = "List all ucsb organizations")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<UCSBOrganizations> allOrganizations() {
    Iterable<UCSBOrganizations> organizations = ucsbOrganizationsRepository.findAll();
    return organizations;
  }

  /**
   * This method creates a new organization. Accessible only to users with the role "ROLE_ADMIN".
   *
   * @param orgCode code of the organization
   * @param orgTranslationShort organization translation short
   * @param orgTranslation organization translation
   * @param Inactive whether or not the organization is inactive
   * @return the saved organization
   */
  @Operation(summary = "Create a new organization")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public UCSBOrganizations postCommons(
      @Parameter(name = "orgCode") @RequestParam String orgCode,
      @Parameter(name = "orgTranslationShort") @RequestParam String orgTranslationShort,
      @Parameter(name = "orgTranslation") @RequestParam String orgTranslation,
      @Parameter(name = "inactive") @RequestParam boolean inactive) {
    UCSBOrganizations commons = new UCSBOrganizations();
    commons.setOrgCode(orgCode);
    commons.setOrgTranslationShort(orgTranslationShort);
    commons.setOrgTranslation(orgTranslation);
    commons.setInactive(inactive);

    UCSBOrganizations savedOrganizations = ucsbOrganizationsRepository.save(commons);

    return savedOrganizations;
  }

  /**
   * This method returns a single organization.
   *
   * @param orgCode code of the organization
   * @return a single organization
   */
  @Operation(summary = "Get a single organization")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public UCSBOrganizations getById(@Parameter(name = "orgCode") @RequestParam String orgCode) {
    UCSBOrganizations org =
        ucsbOrganizationsRepository
            .findById(orgCode)
            .orElseThrow(() -> new EntityNotFoundException(UCSBOrganizations.class, orgCode));

    return org;
  }
}
