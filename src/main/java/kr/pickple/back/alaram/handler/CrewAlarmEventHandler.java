package kr.pickple.back.alaram.handler;

import kr.pickple.back.alaram.domain.CrewAlarm;
import kr.pickple.back.alaram.event.crew.CrewJoinRequestNotificationEvent;
import kr.pickple.back.alaram.event.crew.CrewMemberJoinedEvent;
import kr.pickple.back.alaram.event.crew.CrewMemberRejectedEvent;
import kr.pickple.back.alaram.service.CrewAlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CrewAlarmEventHandler {

    private final CrewAlarmService crewAlaramService;

    @Async
    @EventListener
    public void sendAlarmToCrewLeader(final CrewJoinRequestNotificationEvent crewJoinRequestNotificationEvent) {
        final CrewAlarm crewAlarm = crewAlaramService.createCrewJoinAlaram(crewJoinRequestNotificationEvent); // 알람 생성
        crewAlaramService.emitMessage(crewAlarm); // SSE로 알람 메시지 전송
    }

    @Async
    @EventListener
    public void sendAlarmToCrewMemberOnJoin(final CrewMemberJoinedEvent crewMemberJoinedEvent) {
        final CrewAlarm crewAlarm = crewAlaramService.createCrewMemberApproveAlaram(crewMemberJoinedEvent); // 알람 생성
        crewAlaramService.emitMessage(crewAlarm); // SSE로 알람 메시지 전송
    }

    @Async
    @EventListener
    public void sendAlarmToCrewMemberOnRejection(final CrewMemberRejectedEvent crewMemberRejectedEvent) {
        final CrewAlarm crewAlarm = crewAlaramService.createCrewMemberDeniedAlaram(crewMemberRejectedEvent); // 알람 생성
        crewAlaramService.emitMessage(crewAlarm); // SSE로 알람 메시지 전송
    }
}