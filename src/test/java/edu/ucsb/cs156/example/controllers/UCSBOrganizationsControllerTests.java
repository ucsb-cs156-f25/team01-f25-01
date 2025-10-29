package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganizations;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationsRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UCSBOrganizationsController.class)
@Import(TestConfig.class)
public class UCSBOrganizationsControllerTests extends ControllerTestCase {
  @MockBean UCSBOrganizationsRepository ucsbOrganizationsRepository;

  @MockBean UserRepository userRepository;

  // Authorization tests for /api/ucsborganizations/all

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

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/ucsborganizations?orgCode=org1"))
        .andExpect(status().is(403)); // logged out users can't get by id
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

  // Tests with mocks for database actions

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    // arrange

    UCSBOrganizations org1 =
        UCSBOrganizations.builder()
            .orgCode("org1")
            .orgTranslationShort("Org1")
            .orgTranslation("Organization1")
            .inactive(true)
            .build();

    when(ucsbOrganizationsRepository.findById(eq("org1"))).thenReturn(Optional.of(org1));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsborganizations?orgCode=org1"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(ucsbOrganizationsRepository, times(1)).findById(eq("org1"));
    String expectedJson = mapper.writeValueAsString(org1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    // arrange

    when(ucsbOrganizationsRepository.findById(eq("noorgg"))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsborganizations?orgCode=noorgg"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert

    verify(ucsbOrganizationsRepository, times(1)).findById(eq("noorgg"));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("UCSBOrganizations with id noorgg not found", json.get("message"));
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
            .inactive(true)
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
            .inactive(true)
            .build();

    when(ucsbOrganizationsRepository.save(eq(org1))).thenReturn(org1);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/ucsborganizations/post?orgCode=org&orgTranslationShort=Org&orgTranslation=Organization&inactive=true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationsRepository, times(1)).save(org1);
    String expectedJson = mapper.writeValueAsString(org1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_commons() throws Exception {
    // arrange

    UCSBOrganizations org1 =
        UCSBOrganizations.builder()
            .orgCode("org1")
            .orgTranslationShort("Org1")
            .orgTranslation("Organization1")
            .inactive(true)
            .build();

    UCSBOrganizations org2 =
        UCSBOrganizations.builder()
            .orgCode("org1")
            .orgTranslationShort("Org2")
            .orgTranslation("Organization2")
            .inactive(false)
            .build();

    String requestBody = mapper.writeValueAsString(org2);

    when(ucsbOrganizationsRepository.findById(eq("org2"))).thenReturn(Optional.of(org1));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsborganizations?orgCode=org2")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationsRepository, times(1)).findById("org2");
    verify(ucsbOrganizationsRepository, times(1)).save(org2); // should be saved with updated info
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_commons_that_does_not_exist() throws Exception {
    // arrange

    UCSBOrganizations org1 =
        UCSBOrganizations.builder()
            .orgCode("org1")
            .orgTranslationShort("Org1")
            .orgTranslation("Organization1")
            .inactive(true)
            .build();

    String requestBody = mapper.writeValueAsString(org1);

    when(ucsbOrganizationsRepository.findById(eq("org1"))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsborganizations?orgCode=org1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbOrganizationsRepository, times(1)).findById("org1");
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganizations with id org1 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_a_date() throws Exception {
    // arrange

    UCSBOrganizations org1 =
        UCSBOrganizations.builder()
            .orgCode("org1")
            .orgTranslationShort("Org1")
            .orgTranslation("Organization1")
            .inactive(true)
            .build();

    when(ucsbOrganizationsRepository.findById(eq("org1"))).thenReturn(Optional.of(org1));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsborganizations?orgCode=org1").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationsRepository, times(1)).findById("org1");
    verify(ucsbOrganizationsRepository, times(1)).delete(any());

    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganizations with id org1 deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_tries_to_delete_non_existant_commons_and_gets_right_error_message()
      throws Exception {
    // arrange

    when(ucsbOrganizationsRepository.findById(eq("noorg"))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsborganizations?orgCode=noorg").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbOrganizationsRepository, times(1)).findById("noorg");
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganizations with id noorg not found", json.get("message"));
  }
}
