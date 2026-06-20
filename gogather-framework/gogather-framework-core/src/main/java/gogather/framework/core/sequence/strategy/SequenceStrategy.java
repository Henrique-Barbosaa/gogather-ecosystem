package gogather.framework.core.sequence.strategy;

import gogather.framework.core.sequence.SequencedItem;
import java.util.List;
import java.util.Optional;

public interface SequenceStrategy<T extends SequencedItem> {
    
    Optional<T> getNext(T currentItem, List<T> orderedItems);
    
    Optional<T> getPrevious(T currentItem, List<T> orderedItems);
    
    void validateMove(T item, int newIndex, List<T> orderedItems);
}
