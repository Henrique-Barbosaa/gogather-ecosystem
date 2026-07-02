package gogather.framework.polling.orchestrator;

import gogather.framework.polling.core.PollingDataProvider;
import gogather.framework.polling.dto.PollVoteRecord;
import java.util.Optional;

public class PollingOrchestrator {
    private final PollingDataProvider dataProvider;

    public PollingOrchestrator(PollingDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    public void processVote(String pollId, String optionId, String userId) {
        Optional<PollVoteRecord> existingVoteOpt = dataProvider.getUserVote(pollId, userId);

        if (existingVoteOpt.isPresent()) {
            PollVoteRecord existingVote = existingVoteOpt.get();
            if (existingVote.optionId().equals(optionId)) {
                dataProvider.deleteVote(existingVote.voteId());
                dataProvider.decrementOption(optionId);
            } else {
                dataProvider.decrementOption(existingVote.optionId());
                dataProvider.updateVoteOption(existingVote.voteId(), optionId);
                dataProvider.incrementOption(optionId);
            }
        } else {
            dataProvider.createVote(pollId, optionId, userId);
            dataProvider.incrementOption(optionId);
        }
    }
}
