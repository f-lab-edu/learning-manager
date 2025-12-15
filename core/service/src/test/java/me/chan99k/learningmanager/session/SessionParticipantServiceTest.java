package me.chan99k.learningmanager.session;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.session.SessionParticipantManagement.AddParticipantRequest;
import me.chan99k.learningmanager.session.SessionParticipantManagement.ChangeParticipantRoleRequest;
import me.chan99k.learningmanager.session.SessionParticipantManagement.LeaveSessionRequest;
import me.chan99k.learningmanager.session.SessionParticipantManagement.RemoveParticipantRequest;

@ExtendWith(MockitoExtension.class)
class SessionParticipantServiceTest {

	private final Long sessionId = 1L;
	private final Long requesterId = 200L;
	private final Long hostId = 300L;
	private final Long memberId = 400L;

	@InjectMocks
	private SessionParticipantService sessionParticipantService;

	@Mock
	private Clock clock;

	@Mock
	private SessionQueryRepository sessionQueryRepository;

	@Mock
	private SessionCommandRepository sessionCommandRepository;

	@Mock
	private Session session;

	@Test
	@DisplayName("[Success] 세션에 참여자를 추가할 수 있다")
	void addParticipant_Success() {
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.getId()).thenReturn(sessionId);
		when(session.getTitle()).thenReturn("테스트 세션");
		when(session.getParticipants()).thenReturn(List.of());
		when(sessionCommandRepository.save(session)).thenReturn(session);

		var request = new AddParticipantRequest(memberId, SessionParticipantRole.ATTENDEE);
		var response = sessionParticipantService.addParticipant(requesterId, sessionId, request);

		assertThat(response).isNotNull();
		assertThat(response.sessionId()).isEqualTo(sessionId);
		verify(session).addParticipant(memberId, SessionParticipantRole.ATTENDEE);
		verify(sessionCommandRepository).save(session);
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 세션에는 참여자를 추가할 수 없다")
	void addParticipant_SessionNotFound_ThrowsDomainException() {
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.empty());

		var request = new AddParticipantRequest(memberId, SessionParticipantRole.ATTENDEE);

		assertThatThrownBy(() -> sessionParticipantService.addParticipant(requesterId, sessionId, request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", SessionProblemCode.SESSION_NOT_FOUND);
	}

	@Test
	@DisplayName("[Success] 세션의 참여자를 제거할 수 있다")
	void removeParticipant_Success() {
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.getId()).thenReturn(sessionId);
		when(session.getTitle()).thenReturn("테스트 세션");
		when(session.getParticipants()).thenReturn(List.of());
		when(sessionCommandRepository.save(session)).thenReturn(session);

		var request = new RemoveParticipantRequest(sessionId, memberId);
		var response = sessionParticipantService.removeParticipant(requesterId, request);

		assertThat(response).isNotNull();
		assertThat(response.sessionId()).isEqualTo(sessionId);
		verify(session).removeParticipant(memberId);
		verify(sessionCommandRepository).save(session);
	}

	@Test
	@DisplayName("[Success] 세션의 참여자 역할을 변경할 수 있다")
	void changeParticipantRole_Success() {
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.getId()).thenReturn(sessionId);
		when(session.getTitle()).thenReturn("테스트 세션");
		when(session.getParticipants()).thenReturn(List.of());
		when(sessionCommandRepository.save(session)).thenReturn(session);

		var request = new ChangeParticipantRoleRequest(sessionId, memberId, SessionParticipantRole.SPEAKER);
		var response = sessionParticipantService.changeParticipantRole(requesterId, request);

		assertThat(response).isNotNull();
		assertThat(response.sessionId()).isEqualTo(sessionId);
		verify(session).changeParticipantRole(memberId, SessionParticipantRole.SPEAKER, clock);
		verify(sessionCommandRepository).save(session);
	}

	@Test
	@DisplayName("[Success] 세션에는 여러 HOST가 허용된다")
	void changeParticipantRole_MultipleHostsAllowed() {
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.getId()).thenReturn(sessionId);
		when(session.getTitle()).thenReturn("테스트 세션");
		when(session.getParticipants()).thenReturn(List.of());
		when(sessionCommandRepository.save(session)).thenReturn(session);

		var request = new ChangeParticipantRoleRequest(sessionId, memberId, SessionParticipantRole.HOST);
		var response = sessionParticipantService.changeParticipantRole(requesterId, request);

		assertThat(response).isNotNull();
		verify(session).changeParticipantRole(memberId, SessionParticipantRole.HOST, clock);
		verify(sessionCommandRepository).save(session);
	}

	@Test
	@DisplayName("[Success] 세션에서 HOST 자신을 제거할 때 다른 HOST가 있으면 성공한다")
	void removeParticipant_HostSelfRemoval_SuccessWithOtherHost() {
		var hostParticipant1 = mock(SessionParticipant.class);
		when(hostParticipant1.getMemberId()).thenReturn(hostId);
		when(hostParticipant1.getRole()).thenReturn(SessionParticipantRole.HOST);

		var hostParticipant2 = mock(SessionParticipant.class);
		when(hostParticipant2.getMemberId()).thenReturn(999L);
		when(hostParticipant2.getRole()).thenReturn(SessionParticipantRole.HOST);

		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.getId()).thenReturn(sessionId);
		when(session.getTitle()).thenReturn("테스트 세션");
		when(session.getParticipants()).thenReturn(List.of(hostParticipant1, hostParticipant2));
		when(sessionCommandRepository.save(session)).thenReturn(session);

		var request = new RemoveParticipantRequest(sessionId, hostId);
		var response = sessionParticipantService.removeParticipant(hostId, request);

		assertThat(response).isNotNull();
		verify(session).removeParticipant(hostId);
	}

	@Test
	@DisplayName("[Failure] 세션에서 HOST 자신을 제거할 때 다른 HOST가 없으면 실패한다")
	void removeParticipant_HostSelfRemoval_FailWithoutOtherHost() {
		var hostParticipant = mock(SessionParticipant.class);
		when(hostParticipant.getMemberId()).thenReturn(hostId);
		when(hostParticipant.getRole()).thenReturn(SessionParticipantRole.HOST);

		var attendeeParticipant = mock(SessionParticipant.class);

		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.getParticipants()).thenReturn(List.of(hostParticipant, attendeeParticipant));

		var request = new RemoveParticipantRequest(sessionId, hostId);

		assertThatThrownBy(() -> sessionParticipantService.removeParticipant(hostId, request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", SessionProblemCode.HOST_CANNOT_LEAVE_ALONE);

		verify(session, never()).removeParticipant(hostId);
	}

	@Test
	@DisplayName("[Success] 세션에서 HOST가 아닌 사용자를 제거하는 것은 자유롭게 가능하다")
	void removeParticipant_NonHostRemoval_Success() {
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.getId()).thenReturn(sessionId);
		when(session.getTitle()).thenReturn("테스트 세션");
		when(session.getParticipants()).thenReturn(List.of());
		when(sessionCommandRepository.save(session)).thenReturn(session);

		var request = new RemoveParticipantRequest(sessionId, memberId);
		var response = sessionParticipantService.removeParticipant(requesterId, request);

		assertThat(response).isNotNull();
		verify(session).removeParticipant(memberId);
	}

	@Test
	@DisplayName("[Success] 세션의 하위 세션에서 자가 탈퇴가 성공한다")
	void leaveSession_ChildSession_Success() {
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.isRootSession()).thenReturn(false);
		when(session.getId()).thenReturn(sessionId);
		when(session.getTitle()).thenReturn("테스트 하위 세션");
		when(session.getParticipants()).thenReturn(List.of());
		when(sessionCommandRepository.save(session)).thenReturn(session);

		var request = new LeaveSessionRequest(sessionId);
		var response = sessionParticipantService.leaveSession(memberId, request);

		assertThat(response).isNotNull();
		assertThat(response.sessionId()).isEqualTo(sessionId);
		verify(session).removeParticipant(memberId);
		verify(sessionCommandRepository).save(session);
	}

	@Test
	@DisplayName("[Failure] 세션의 루트 세션에서 자가 탈퇴는 실패한다")
	void leaveSession_RootSession_ThrowsDomainException() {
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.isRootSession()).thenReturn(true);

		var request = new LeaveSessionRequest(sessionId);

		assertThatThrownBy(() -> sessionParticipantService.leaveSession(memberId, request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", SessionProblemCode.ROOT_SESSION_SELF_LEAVE_NOT_ALLOWED);

		verify(session, never()).removeParticipant(any());
		verify(sessionCommandRepository, never()).save(any());
	}

	@Test
	@DisplayName("[Failure] 세션의 하위 세션에서 HOST 자가 탈퇴시 다른 HOST가 없으면 실패한다")
	void leaveSession_ChildSession_HostWithoutOtherHost_ThrowsDomainException() {
		var hostParticipant = mock(SessionParticipant.class);
		when(hostParticipant.getMemberId()).thenReturn(hostId);
		when(hostParticipant.getRole()).thenReturn(SessionParticipantRole.HOST);

		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.isRootSession()).thenReturn(false);
		when(session.getParticipants()).thenReturn(List.of(hostParticipant));

		var request = new LeaveSessionRequest(sessionId);

		assertThatThrownBy(() -> sessionParticipantService.leaveSession(hostId, request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", SessionProblemCode.HOST_CANNOT_LEAVE_ALONE);

		verify(session, never()).removeParticipant(any());
		verify(sessionCommandRepository, never()).save(any());
	}

	@Test
	@DisplayName("[Success] 세션의 하위 세션에서 HOST 자가 탈퇴시 다른 HOST가 있으면 성공한다")
	void leaveSession_ChildSession_HostWithOtherHost_Success() {
		var hostParticipant1 = mock(SessionParticipant.class);
		when(hostParticipant1.getMemberId()).thenReturn(hostId);
		when(hostParticipant1.getRole()).thenReturn(SessionParticipantRole.HOST);

		var hostParticipant2 = mock(SessionParticipant.class);
		when(hostParticipant2.getMemberId()).thenReturn(999L);
		when(hostParticipant2.getRole()).thenReturn(SessionParticipantRole.HOST);

		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.of(session));
		when(session.isRootSession()).thenReturn(false);
		when(session.getId()).thenReturn(sessionId);
		when(session.getTitle()).thenReturn("테스트 하위 세션");
		when(session.getParticipants()).thenReturn(List.of(hostParticipant1, hostParticipant2));
		when(sessionCommandRepository.save(session)).thenReturn(session);

		var request = new LeaveSessionRequest(sessionId);
		var response = sessionParticipantService.leaveSession(hostId, request);

		assertThat(response).isNotNull();
		verify(session).removeParticipant(hostId);
		verify(sessionCommandRepository).save(session);
	}

	@Test
	@DisplayName("[Failure] 존재하지 않는 세션에서 자가 탈퇴는 실패한다")
	void leaveSession_SessionNotFound_ThrowsDomainException() {
		when(sessionQueryRepository.findById(sessionId)).thenReturn(Optional.empty());

		var request = new LeaveSessionRequest(sessionId);

		assertThatThrownBy(() -> sessionParticipantService.leaveSession(memberId, request))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("problemCode", SessionProblemCode.SESSION_NOT_FOUND);
	}
}
