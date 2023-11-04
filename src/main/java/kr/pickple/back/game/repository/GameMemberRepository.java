package kr.pickple.back.game.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.pickple.back.game.domain.GameMember;

public interface GameMemberRepository extends JpaRepository<GameMember, Long> {

    Optional<GameMember> findByMemberIdAndGameId(final Long memberId, final Long gameId);
}
