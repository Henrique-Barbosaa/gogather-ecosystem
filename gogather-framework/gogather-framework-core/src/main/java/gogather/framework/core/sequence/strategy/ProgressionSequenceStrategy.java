package gogather.framework.core.sequence.strategy;

import java.util.List;
import java.util.Optional;

public class ProgressionSequenceStrategy<T extends ProgressionItem> implements SequenceStrategy<T> {

    @Override
    public Optional<T> getNext(T currentItem, List<T> orderedItems) {
        int index = orderedItems.indexOf(currentItem);
        
        if (index >= 0 && index < orderedItems.size() - 1) {
            T next = orderedItems.get(index + 1);
            if (!currentItem.isCompleted()) {
                throw new IllegalStateException("Não é possível avançar para o próximo item, pois o atual não está concluído.");
            }
            return Optional.of(next);
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
        int oldIndex = orderedItems.indexOf(item);
        if (newIndex < oldIndex && item.isCompleted()) {
            throw new IllegalStateException("Não é possível retroceder um item que já foi concluído na progressão.");
        }
    }
}
