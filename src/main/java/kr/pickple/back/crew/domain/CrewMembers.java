package kr.pickple.back.crew.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.OneToMany;
import kr.pickple.back.common.domain.RegistrationStatus;
import kr.pickple.back.crew.exception.CrewException;
import kr.pickple.back.member.domain.Member;

import java.util.ArrayList;
import java.util.List;

import static kr.pickple.back.crew.exception.CrewExceptionCode.CREW_MEMBER_ALREADY_EXISTED;
import static kr.pickple.back.crew.exception.CrewExceptionCode.CREW_MEMBER_NOT_FOUND;

@Embeddable
public class CrewMembers {

    @OneToMany(mappedBy = "crew", cascade = CascadeType.PERSIST)
    private List<CrewMember> crewMembers = new ArrayList<>();

    public List<Member> getCrewMembers(final RegistrationStatus status) {

        if (status != RegistrationStatus.WAITING && status != RegistrationStatus.CONFIRMED) {
            throw new CrewException(CREW_MEMBER_NOT_FOUND);
        }

        return crewMembers.stream()
                .filter(crewMember -> crewMember.equalsStatus(status))
                .map(CrewMember::getMember)
                .toList();
    }

    public void addCrewMember(final Crew crew, final Member member) {
        validateIsAlreadyRegisteredCrewMember(member);

        final CrewMember crewMember = buildCrewMember(crew, member);
        if (member.equals(crew.getLeader())) {
            crewMember.confirmRegistration();
        }
        crewMembers.add(crewMember);
    }

    private void validateIsAlreadyRegisteredCrewMember(final Member member) {
        if (isAlreadyRegistered(member)) {
            throw new CrewException(CREW_MEMBER_ALREADY_EXISTED, member.getId());
        }
    }

    private boolean isAlreadyRegistered(final Member member) {
        return crewMembers.stream()
                .anyMatch(crewMember -> member.equals(crewMember.getMember()));
    }

    private CrewMember buildCrewMember(final Crew crew, final Member member) {
        return CrewMember.builder()
                .member(member)
                .crew(crew)
                .build();
    }
}