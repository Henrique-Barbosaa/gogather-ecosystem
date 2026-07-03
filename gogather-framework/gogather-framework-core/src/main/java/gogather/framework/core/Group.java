package gogather.framework.core;

public interface Group {
	String getIdentifier();
	boolean hasMember(String userIdentifier);
	void addMember(Participant participant, Participant addedBy);
}
