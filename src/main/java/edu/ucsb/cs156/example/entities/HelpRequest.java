package edu.ucsb.cs156.example.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "HELPREQUESTS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HelpRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "REQUESTER_EMAIL")
  private String requesterEmail;

  @Column(name = "TEAM_ID")
  private String teamId;

  @Column(name = "TABLE_OR_BREAKOUT_ROOM")
  private String tableOrBreakoutRoom;

  @Column(name = "REQUEST_TIME")
  private LocalDateTime requestTime;

  @Column(name = "EXPLANATION")
  private String explanation;

  @Column(name = "SOLVED")
  private boolean solved;
}
