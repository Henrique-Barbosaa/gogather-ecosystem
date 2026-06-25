package gogather.framework.sequence.strategy;

import java.util.List;
import java.util.Optional;

import gogather.framework.sequence.SequencedItem;

public interface SequenceStrategy<T extends SequencedItem> {
    
    Optional<T> getNext(T currentItem, List<T> orderedItems);
    
    Optional<T> getPrevious(T currentItem, List<T> orderedItems);
    
    void validateMove(T item, int newIndex, List<T> orderedItems);
}
