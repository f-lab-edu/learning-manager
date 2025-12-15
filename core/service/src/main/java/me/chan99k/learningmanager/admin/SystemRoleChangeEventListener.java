package me.chan99k.learningmanager.admin;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class SystemRoleChangeEventListener {

	private final SystemRoleAuditionPort systemRoleAuditionPort;

	public SystemRoleChangeEventListener(SystemRoleAuditionPort systemRoleAuditionPort) {
		this.systemRoleAuditionPort = systemRoleAuditionPort;
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleSystemRoleChange(SystemRoleChangeEvent event) {
		systemRoleAuditionPort.save(event);
	}
}
