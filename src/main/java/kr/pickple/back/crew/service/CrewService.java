package kr.pickple.back.crew.service;

import static kr.pickple.back.crew.exception.CrewExceptionCode.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.pickple.back.address.dto.response.MainAddressResponse;
import kr.pickple.back.address.service.AddressService;
import kr.pickple.back.common.config.S3Config;
import kr.pickple.back.crew.domain.Crew;
import kr.pickple.back.crew.dto.request.CrewCreateRequest;
import kr.pickple.back.crew.dto.response.CrewIdResponse;
import kr.pickple.back.crew.exception.CrewException;
import kr.pickple.back.crew.repository.CrewRepository;
import kr.pickple.back.member.domain.Member;
import kr.pickple.back.member.exception.MemberException;
import kr.pickple.back.member.exception.MemberExceptionCode;
import kr.pickple.back.member.repository.MemberRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CrewService {

    private final S3Config s3Config;
    private final CrewRepository crewRepository;
    private final AddressService addressService;
    private final MemberRepository memberRepository;

    @Transactional
    public CrewIdResponse createCrew(final CrewCreateRequest crewCreateRequest) {
        validateIsDuplicatedCrewInfo(crewCreateRequest.getName());

        final Member leader = memberRepository.findById(crewCreateRequest.getLeaderId())
                .orElseThrow(() -> new MemberException(MemberExceptionCode.MEMBER_NOT_FOUND));

        final MainAddressResponse mainAddressResponse = addressService.findMainAddressByNames(
                crewCreateRequest.getAddressDepth1(),
                crewCreateRequest.getAddressDepth2()
        );

        Crew crew = crewCreateRequest.toEntity(leader, mainAddressResponse);
        crew.addCrewDefaultProfileImage(s3Config.getProfile());
        crew.addCrewDefaultBackgroundImage(s3Config.getBackground());
        Long crewId = crewRepository.save(crew).getId();

        return CrewIdResponse.from(crewId);
    }

    private void validateIsDuplicatedCrewInfo(final String name) {
        if (crewRepository.existsByName(name)) {
            throw new CrewException(CREW_IS_EXISTED, name);
        }
    }
}