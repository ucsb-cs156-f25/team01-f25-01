package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = HelpRequestController.class)
@Import(TestConfig.class)
public class HelpRequestControllerTests extends ControllerTestCase {

  @MockBean private HelpRequestRepository helpRequestRepository;

  @MockBean private UserRepository userRepository;

  // --- GET /api/helprequest/all ---

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/helprequest/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    // arrange
    LocalDateTime t = LocalDateTime.parse("2022-01-03T00:00:00");
    HelpRequest hr =
        HelpRequest.builder()
            .requesterEmail("pdg@ucsb.edu")
            .teamId("s22-6pm-4")
            .tableOrBreakoutRoom("13")
            .requestTime(t)
            .explanation("Need help with setup")
            .solved(false)
            .build();

    var list = new ArrayList<HelpRequest>();
    list.add(hr);
    when(helpRequestRepository.findAll()).thenReturn(list);

    // act
    MvcResult response =
        mockMvc.perform(get("/api/helprequest/all")).andExpect(status().isOk()).andReturn();

    // assert
    verify(helpRequestRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(list);
    assertEquals(expectedJson, response.getResponse().getContentAsString());
  }

  // --- POST /api/helprequest/post ---

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/helprequest/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void regular_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/helprequest/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_post_new_helprequest() throws Exception {
    // arrange
    LocalDateTime t = LocalDateTime.parse("2022-01-03T00:00:00");
    HelpRequest toSave =
        HelpRequest.builder()
            .requesterEmail("pdg@ucsb.edu")
            .teamId("s22-6pm-4")
            .tableOrBreakoutRoom("13")
            .requestTime(t)
            .explanation("Need help with setup")
            .solved(false)
            .build();

    when(helpRequestRepository.save(eq(toSave))).thenReturn(toSave);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/helprequest/post")
                    .with(csrf())
                    .param("requesterEmail", "pdg@ucsb.edu")
                    .param("teamId", "s22-6pm-4")
                    .param("tableOrBreakoutRoom", "13")
                    .param("requestTime", "2022-01-03T00:00:00")
                    .param("explanation", "Need help with setup")
                    .param("solved", "false"))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(helpRequestRepository, times(1)).save(eq(toSave));
    String expectedJson = mapper.writeValueAsString(toSave);
    assertEquals(expectedJson, response.getResponse().getContentAsString());
  }

  // --- GET /api/helprequest?id=... ---

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc.perform(get("/api/helprequest?id=7")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_get_by_id_not_found() throws Exception {
    when(helpRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc.perform(get("/api/helprequest?id=7")).andExpect(status().isNotFound()).andReturn();

    verify(helpRequestRepository, times(1)).findById(eq(7L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("HelpRequest with id 7 not found", json.get("message"));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_get_by_id_success() throws Exception {
    LocalDateTime t = LocalDateTime.parse("2022-01-03T00:00:00");
    HelpRequest hr =
        HelpRequest.builder()
            .requesterEmail("pdg@ucsb.edu")
            .teamId("s22-6pm-4")
            .tableOrBreakoutRoom("13")
            .requestTime(t)
            .explanation("Need help with setup")
            .solved(false)
            .build();

    when(helpRequestRepository.findById(eq(7L))).thenReturn(Optional.of(hr));

    MvcResult response =
        mockMvc.perform(get("/api/helprequest?id=7")).andExpect(status().isOk()).andReturn();

    verify(helpRequestRepository, times(1)).findById(eq(7L));
    String expectedJson = mapper.writeValueAsString(hr);
    assertEquals(expectedJson, response.getResponse().getContentAsString());
  }
}
