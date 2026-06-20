package gogather.framework.core;

public interface Group {
	String getIdentifier();
	boolean hasMember(String userIdentifier);
	void addPendindParticipant(Participant participant, Participant invitedBy);
}
