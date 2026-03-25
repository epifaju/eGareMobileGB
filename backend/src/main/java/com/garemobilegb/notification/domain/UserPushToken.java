package com.garemobilegb.notification.domain;

import com.garemobilegb.auth.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(
    name = "user_push_tokens",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "expo_push_token"}))
public class UserPushToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "expo_push_token", nullable = false, length = 512)
  private String expoPushToken;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  protected UserPushToken() {}

  public UserPushToken(User user, String expoPushToken) {
    this.user = user;
    this.expoPushToken = expoPushToken;
    this.updatedAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public String getExpoPushToken() {
    return expoPushToken;
  }

  public void touch() {
    this.updatedAt = Instant.now();
  }
}
