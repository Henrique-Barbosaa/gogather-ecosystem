package gogather.framework.sequence.strategy;

import java.util.List;
import java.util.Optional;

import gogather.framework.sequence.SequencedItem;

public class LinearSequenceStrategy<T extends SequencedItem> implements SequenceStrategy<T> {

    @Override
    public Optional<T> getNext(T currentItem, List<T> orderedItems) {
        int index = orderedItems.indexOf(currentItem);
        if (index >= 0 && index < orderedItems.size() - 1) {
            return Optional.of(orderedItems.get(index + 1));
        }
        return Optional.empty();
    }

    @Override
    public Optional<T> getPrevious(T currentItem, List<T> orderedItems) {
        int index = orderedItems.indexOf(currentItem);
        if (index > 0) {
            return Optional.of(orderedItems.get(index - 1));
        }
        return Optional.empty();
    }

    @Override
    public void validateMove(T item, int newIndex, List<T> orderedItems) {
        // Linear sequence allows any structural movement
    }
}
