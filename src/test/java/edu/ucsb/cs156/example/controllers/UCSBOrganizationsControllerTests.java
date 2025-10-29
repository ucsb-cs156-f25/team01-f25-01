package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganizations;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationsRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UCSBOrganizationsController.class)
@Import(TestConfig.class)
public class UCSBOrganizationsControllerTests extends ControllerTestCase {
  @MockBean UCSBOrganizationsRepository ucsbOrganizationsRepository;

  @MockBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/ucsborganizations/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/ucsborganizations/all")).andExpect(status().is(200)); // logged
  }

  // Authorization tests for /api/ucsborganizations/post
  // (Perhaps should also have these for put and delete)

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/ucsborganizations/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(post("/api/ucsborganizations/post"))
        .andExpect(status().is(403)); // only admins can post
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_ucsborganizations() throws Exception {

    // arrange

    UCSBOrganizations org1 =
        UCSBOrganizations.builder()
            .orgCode("org1")
            .orgTranslationShort("Org1")
            .orgTranslation("Organization1")
            .inactive(false)
            .build();

    UCSBOrganizations org2 =
        UCSBOrganizations.builder()
            .orgCode("org2")
            .orgTranslationShort("Org2")
            .orgTranslation("Organization2")
            .inactive(false)
            .build();

    ArrayList<UCSBOrganizations> expectedOrganizations = new ArrayList<>();
    expectedOrganizations.addAll(Arrays.asList(org1, org2));

    when(ucsbOrganizationsRepository.findAll()).thenReturn(expectedOrganizations);

    // act
    MvcResult response =
        mockMvc.perform(get("/api/ucsborganizations/all")).andExpect(status().isOk()).andReturn();

    // assert
    verify(ucsbOrganizationsRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedOrganizations);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_commons() throws Exception {
    // arrange

    UCSBOrganizations org1 =
        UCSBOrganizations.builder()
            .orgCode("org")
            .orgTranslationShort("Org")
            .orgTranslation("Organization")
            .inactive(false)
            .build();

    when(ucsbOrganizationsRepository.save(eq(org1))).thenReturn(org1);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/ucsborganizations/post?orgCode=org&orgTranslationShort=Org&orgTranslation=Organization&inactive=false")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationsRepository, times(1)).save(org1);
    String expectedJson = mapper.writeValueAsString(org1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }
}
