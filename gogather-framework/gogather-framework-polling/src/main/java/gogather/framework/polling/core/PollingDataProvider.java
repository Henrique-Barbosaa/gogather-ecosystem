package gogather.framework.polling.core;

import gogather.framework.polling.dto.PollVoteRecord;
import java.util.Optional;

public interface PollingDataProvider {
    Optional<PollVoteRecord> getUserVote(String pollId, String userId);
    void deleteVote(String voteId);
    void updateVoteOption(String voteId, String newOptionId);
    void createVote(String pollId, String optionId, String userId);
    void incrementOption(String optionId);
    void decrementOption(String optionId);
}
