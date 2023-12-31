package kr.pickple.back.chat.service;

import static kr.pickple.back.chat.exception.ChatExceptionCode.*;

import java.util.Optional;

import org.springframework.stereotype.Component;

import kr.pickple.back.chat.domain.ChatRoom;
import kr.pickple.back.chat.exception.ChatException;
import kr.pickple.back.chat.repository.ChatRoomMemberRepository;
import kr.pickple.back.common.util.DateTimeUtil;
import kr.pickple.back.crew.domain.Crew;
import kr.pickple.back.crew.repository.CrewRepository;
import kr.pickple.back.game.domain.Game;
import kr.pickple.back.game.repository.GameRepository;
import kr.pickple.back.member.domain.Member;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatValidator {

    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final CrewRepository crewRepository;
    private final GameRepository gameRepository;

    public void validateIsSelfChat(Member receiver, Member sender) {
        if (sender.equals(receiver)) {
            throw new ChatException(CHAT_MEMBER_CANNOT_CHAT_SELF, sender.getId());
        }
    }

    public void validateIsNotExistedRoomMember(final ChatRoom chatRoom, final Member member) {
        if (isExistedRoomMember(chatRoom, member)) {
            throw new ChatException(CHAT_MEMBER_IS_ALREADY_IN_ROOM, member.getId(), chatRoom.getId());
        }
    }

    public void validateIsExistedRoomMember(final Member member, final ChatRoom chatRoom) {
        if (!isExistedRoomMember(chatRoom, member)) {
            throw new ChatException(CHAT_MEMBER_IS_NOT_IN_ROOM, member.getId(), chatRoom.getId());
        }
    }

    private Boolean isExistedRoomMember(final ChatRoom chatRoom, final Member member) {
        return chatRoomMemberRepository.existsByActiveTrueAndChatRoomAndMember(chatRoom, member);
    }

    public void validateChatRoomLeavingConditions(final Member member, final ChatRoom chatRoom) {
        switch (chatRoom.getType()) {
            case CREW -> validateCrewChatRoomLeavingConditions(member, chatRoom);
            case GAME -> validateGameChatRoomLeavingConditions(member, chatRoom);
        }
    }

    private void validateCrewChatRoomLeavingConditions(final Member member, final ChatRoom chatRoom) {
        final Optional<Crew> optionalCrew = crewRepository.findByChatRoom(chatRoom);

        if (optionalCrew.isEmpty()) {
            return;
        }

        final Crew crew = optionalCrew.get();

        if (crew.isConfirmedCrewMember(member)) {
            throw new ChatException(CHAT_CREW_CHATROOM_NOT_ALLOWED_TO_LEAVE, member.getId(), chatRoom.getId());
        }
    }

    private void validateGameChatRoomLeavingConditions(final Member member, final ChatRoom chatRoom) {
        final Optional<Game> optionalGame = gameRepository.findByChatRoom(chatRoom);

        if (optionalGame.isEmpty()) {
            return;
        }

        final Game game = optionalGame.get();

        if (isGameNotOver(game)) {
            throw new ChatException(CHAT_GAME_CHATROOM_NOT_ALLOWED_TO_LEAVE, member.getId(), game.getId());
        }
    }

    private Boolean isGameNotOver(final Game game) {
        return DateTimeUtil.isAfterThanNow(game.getPlayEndDatetime());
    }
}
