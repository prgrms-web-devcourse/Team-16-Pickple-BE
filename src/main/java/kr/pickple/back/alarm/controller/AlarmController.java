package kr.pickple.back.alarm.controller;

import jakarta.validation.Valid;
import kr.pickple.back.alarm.domain.AlarmExistsStatus;
import kr.pickple.back.alarm.dto.response.AlarmResponse;
import kr.pickple.back.alarm.service.AlarmService;
import kr.pickple.back.alarm.util.CursorResult;
import kr.pickple.back.auth.config.resolver.Login;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/alarms")
public class AlarmController {

    private final AlarmService alarmService;

    //해당 사용자의 sse연결
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribeToSse(
            @Login final Long loggedInMemberId
    ) {
        final SseEmitter emitter = alarmService.subscribeToSse(loggedInMemberId);
        return ResponseEntity.status(OK)
                .header("X-Accel-Buffering", "no")
                .body(emitter);
    }

    //해당 사용자에게 미 알람이 있는지 체크
    @GetMapping("/unread")
    public ResponseEntity<AlarmExistsStatus> findUnreadAlarm(
            @Login final Long loggedInMemberId
    ) {
        AlarmExistsStatus alarmExistsStatus = alarmService.checkUnReadAlarms(loggedInMemberId);
        return ResponseEntity
                .status(OK)
                .body(alarmExistsStatus);

    }

    //모든 알람 조회시
    //처음에 cursorId 없으면 0,한번에 불러오는 size = 10
    @GetMapping
    public ResponseEntity<CursorResult<AlarmResponse>> findAllAlarms(
            @Login final Long loggedInMemberId,
            @RequestParam(defaultValue = "0") Long cursorId,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return ResponseEntity.status(OK)
                .body(alarmService.findAllAlarms(loggedInMemberId, cursorId, size));
    }

    //알람 수정
    @PatchMapping("/{alarmId}")
    public ResponseEntity<Void> updateAlarmStatus(
            @Login final Long loggedInMemberId,
            @PathVariable final Long alarmId,
            @Valid @RequestBody final String isRead
    ) {
        alarmService.updateAlarmById(loggedInMemberId, alarmId, isRead);
        return ResponseEntity.status(NO_CONTENT)
                .build();
    }

    //모든 알람 삭제
    @DeleteMapping
    public ResponseEntity<Void> deleteAllAlarms(
            @Login final Long loggedInMemberId
    ) {
        alarmService.deleteAllAlarms(loggedInMemberId);
        return ResponseEntity.status(NO_CONTENT)
                .build();
    }
}