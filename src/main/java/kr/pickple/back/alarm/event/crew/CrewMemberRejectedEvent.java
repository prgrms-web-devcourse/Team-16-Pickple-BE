package kr.pickple.back.alarm.event.crew;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class CrewMemberRejectedEvent {

    private final Long crewId;
    private final Long memberId;
}
