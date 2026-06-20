package gogather.framework.core;

public interface Group {
	String getIdentifier();
	boolean hasMember(String userIdentifier);
	void addPendingParticipant(Participant participant, Participant invitedBy);
}
