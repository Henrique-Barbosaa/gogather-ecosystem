package gogather.framework.sequence;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import gogather.framework.sequence.strategy.LinearSequenceStrategy;
import gogather.framework.sequence.strategy.SequenceStrategy;

/**
 * Gerenciador matemático abstrato para lidar com sequências lógicas de itens.
 * Utiliza o Padrão Strategy para suportar diferentes comportamentos de navegação.
 */
public class SequenceManager<T extends SequencedItem> {

    private final List<T> items;
    private final SequenceStrategy<T> strategy;

    public SequenceManager(List<T> initialItems, SequenceStrategy<T> strategy) {
        this.items = new ArrayList<>(initialItems);
        this.strategy = strategy;
        normalizeSequence();
    }

    public SequenceManager(List<T> initialItems) {
        this(initialItems, new LinearSequenceStrategy<>());
    }

    public SequenceManager() {
        this(new ArrayList<>(), new LinearSequenceStrategy<>());
    }

    public Optional<T> getNext(T current) {
        return strategy.getNext(current, getOrderedItems());
    }

    public Optional<T> getPrevious(T current) {
        return strategy.getPrevious(current, getOrderedItems());
    }

    public List<T> getOrderedItems() {
        this.items.sort(Comparator.comparingInt(item -> item.getSequenceOrder() != null ? item.getSequenceOrder() : Integer.MAX_VALUE));
        return this.items;
    }

    public void normalizeSequence() {
        List<T> ordered = getOrderedItems();
        for (int i = 0; i < ordered.size(); i++) {
            ordered.get(i).setSequenceOrder(i);
        }
    }

    public void appendItem(T item) {
        item.setSequenceOrder(this.items.size());
        this.items.add(item);
    }

    public void insertItemAt(T item, int targetIndex) {
        if (targetIndex < 0 || targetIndex > this.items.size()) {
            throw new IllegalArgumentException("Index fora dos limites da sequência.");
        }
        
        List<T> ordered = getOrderedItems();
        strategy.validateMove(item, targetIndex, ordered);
        
        ordered.add(targetIndex, item);
        
        this.items.clear();
        this.items.addAll(ordered);
        normalizeSequence();
    }

    public void removeItem(T item) {
        if (this.items.remove(item)) {
            normalizeSequence();
        }
    }

    public void swap(T item1, T item2) {
        if (!this.items.contains(item1) || !this.items.contains(item2)) {
            throw new IllegalArgumentException("Ambos os itens devem pertencer à mesma sequência.");
        }

        List<T> ordered = getOrderedItems();
        int index1 = ordered.indexOf(item1);
        int index2 = ordered.indexOf(item2);
        
        strategy.validateMove(item1, index2, ordered);
        strategy.validateMove(item2, index1, ordered);

        Integer order1 = item1.getSequenceOrder();
        Integer order2 = item2.getSequenceOrder();

        item1.setSequenceOrder(order2);
        item2.setSequenceOrder(order1);
        
        getOrderedItems();
    }

    public void moveToIndex(T item, int newIndex) {
        if (!this.items.contains(item)) {
            throw new IllegalArgumentException("O item deve pertencer à sequência atual.");
        }
        if (newIndex < 0 || newIndex >= this.items.size()) {
            throw new IllegalArgumentException("Index fora dos limites da sequência.");
        }

        List<T> ordered = getOrderedItems();
        strategy.validateMove(item, newIndex, ordered);

        ordered.remove(item);
        ordered.add(newIndex, item);

        this.items.clear();
        this.items.addAll(ordered);
        normalizeSequence();
    }
}
