package com.role.net.gogather.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "poll_options")
public class PollOption extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    private Poll poll;

    @Column(nullable = false)
    private String text;

    @Column(name = "place_id")
    private String placeId;

    @Builder.Default
    @Column(nullable = false)
    private int votes = 0;

    @Builder.Default
    @OneToMany(mappedBy = "pollOption", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<PollVote> userVotes = new java.util.ArrayList<>();
}
