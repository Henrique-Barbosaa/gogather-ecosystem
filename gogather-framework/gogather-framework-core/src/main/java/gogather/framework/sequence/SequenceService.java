package gogather.framework.sequence;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import gogather.framework.sequence.strategy.LinearSequenceStrategy;
import gogather.framework.sequence.strategy.SequenceStrategy;

/**
 * Serviço stateless para gerenciar sequências matemáticas de itens.
 * Recebe a lista de itens a ser manipulada por parâmetro para suportar Inversão de Dependência
 * e evitar estado interno, podendo ser injetado como Singleton.
 */
public class SequenceService {

    private final SequenceStrategy<?> defaultStrategy;

    public SequenceService(SequenceStrategy<?> defaultStrategy) {
        this.defaultStrategy = defaultStrategy;
    }

    public SequenceService() {
        this(new LinearSequenceStrategy<>());
    }

    @SuppressWarnings("unchecked")
    private <T extends SequencedItem> SequenceStrategy<T> getStrategy() {
        return (SequenceStrategy<T>) this.defaultStrategy;
    }

    public <T extends SequencedItem> Optional<T> getNext(T current, List<T> items) {
        return getNext(current, items, getStrategy());
    }

    public <T extends SequencedItem> Optional<T> getNext(T current, List<T> items, SequenceStrategy<T> strategy) {
        return strategy.getNext(current, getOrderedItems(items));
    }

    public <T extends SequencedItem> Optional<T> getPrevious(T current, List<T> items) {
        return getPrevious(current, items, getStrategy());
    }

    public <T extends SequencedItem> Optional<T> getPrevious(T current, List<T> items, SequenceStrategy<T> strategy) {
        return strategy.getPrevious(current, getOrderedItems(items));
    }

    public <T extends SequencedItem> List<T> getOrderedItems(List<T> items) {
        items.sort(Comparator.comparingInt(item -> item.getSequenceOrder() != null ? item.getSequenceOrder() : Integer.MAX_VALUE));
        return items;
    }

    public <T extends SequencedItem> void normalizeSequence(List<T> items) {
        List<T> ordered = getOrderedItems(items);
        for (int i = 0; i < ordered.size(); i++) {
            ordered.get(i).setSequenceOrder(i);
        }
    }

    public <T extends SequencedItem> void appendItem(T item, List<T> items) {
        item.setSequenceOrder(items.size());
        items.add(item);
    }

    public <T extends SequencedItem> void insertItemAt(T item, int targetIndex, List<T> items) {
        insertItemAt(item, targetIndex, items, getStrategy());
    }

    public <T extends SequencedItem> void insertItemAt(T item, int targetIndex, List<T> items, SequenceStrategy<T> strategy) {
        if (targetIndex < 0 || targetIndex > items.size()) {
            throw new IllegalArgumentException("Index fora dos limites da sequência.");
        }
        
        List<T> ordered = getOrderedItems(items);
        strategy.validateMove(item, targetIndex, ordered);
        
        ordered.add(targetIndex, item);
        
        items.clear();
        items.addAll(ordered);
        normalizeSequence(items);
    }

    public <T extends SequencedItem> void removeItem(T item, List<T> items) {
        if (items.remove(item)) {
            normalizeSequence(items);
        }
    }

    public <T extends SequencedItem> void swap(T item1, T item2, List<T> items) {
        swap(item1, item2, items, getStrategy());
    }

    public <T extends SequencedItem> void swap(T item1, T item2, List<T> items, SequenceStrategy<T> strategy) {
        if (!items.contains(item1) || !items.contains(item2)) {
            throw new IllegalArgumentException("Ambos os itens devem pertencer à mesma sequência.");
        }

        List<T> ordered = getOrderedItems(items);
        int index1 = ordered.indexOf(item1);
        int index2 = ordered.indexOf(item2);
        
        strategy.validateMove(item1, index2, ordered);
        strategy.validateMove(item2, index1, ordered);

        Integer order1 = item1.getSequenceOrder();
        Integer order2 = item2.getSequenceOrder();

        item1.setSequenceOrder(order2);
        item2.setSequenceOrder(order1);
        
        getOrderedItems(items);
    }

    public <T extends SequencedItem> void moveToIndex(T item, int newIndex, List<T> items) {
        moveToIndex(item, newIndex, items, getStrategy());
    }

    public <T extends SequencedItem> void moveToIndex(T item, int newIndex, List<T> items, SequenceStrategy<T> strategy) {
        if (!items.contains(item)) {
            throw new IllegalArgumentException("O item deve pertencer à sequência atual.");
        }
        if (newIndex < 0 || newIndex >= items.size()) {
            throw new IllegalArgumentException("Index fora dos limites da sequência.");
        }

        List<T> ordered = getOrderedItems(items);
        strategy.validateMove(item, newIndex, ordered);

        ordered.remove(item);
        ordered.add(newIndex, item);

        items.clear();
        items.addAll(ordered);
        normalizeSequence(items);
    }
}
