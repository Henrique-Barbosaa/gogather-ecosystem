package gogather.framework.sequence.strategy;

import gogather.framework.sequence.SequencedItem;

public interface ProgressionItem extends SequencedItem {
    boolean isCompleted();
}
