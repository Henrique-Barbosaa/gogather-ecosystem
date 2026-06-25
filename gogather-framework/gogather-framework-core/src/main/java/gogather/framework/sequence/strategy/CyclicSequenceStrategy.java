package gogather.framework.sequence.strategy;

import java.util.List;
import java.util.Optional;

import gogather.framework.sequence.SequencedItem;

public class CyclicSequenceStrategy<T extends SequencedItem> implements SequenceStrategy<T> {

    @Override
    public Optional<T> getNext(T currentItem, List<T> orderedItems) {
        if (orderedItems.isEmpty()) return Optional.empty();
        
        int index = orderedItems.indexOf(currentItem);
        if (index < 0) return Optional.empty();
        
        int nextIndex = (index + 1) % orderedItems.size();
        return Optional.of(orderedItems.get(nextIndex));
    }

    @Override
    public Optional<T> getPrevious(T currentItem, List<T> orderedItems) {
        if (orderedItems.isEmpty()) return Optional.empty();
        
        int index = orderedItems.indexOf(currentItem);
        if (index < 0) return Optional.empty();
        
        int prevIndex = (index - 1 + orderedItems.size()) % orderedItems.size();
        return Optional.of(orderedItems.get(prevIndex));
    }

    @Override
    public void validateMove(T item, int newIndex, List<T> orderedItems) {
        // Cyclic allows any internal structural movement
    }
}
