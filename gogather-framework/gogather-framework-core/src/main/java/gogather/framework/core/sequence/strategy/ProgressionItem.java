package gogather.framework.core.sequence.strategy;

import gogather.framework.core.sequence.SequencedItem;

public interface ProgressionItem extends SequencedItem {
    boolean isCompleted();
}
