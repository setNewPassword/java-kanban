package manager;

import manager.interfaces.HistoryManager;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private final CustomLinkedList<Task> requestHistory;

    public InMemoryHistoryManager() {
        this.requestHistory = new CustomLinkedList<>();
    }

    @Override
    public void add(Task task) {
        requestHistory.addLast(task);

    }

    @Override
    public void remove(int id) {
        requestHistory.removeNode(id);
    }

    @Override
    public List<Task> getHistory() {
        return requestHistory.getHistory();
    }

    private static class CustomLinkedList<T extends Task> {
        private final Map<Integer, Node<T>> memory = new HashMap<>();   // Хешмапа для хранения нод
        private Node<T> head;                                           // Ссылка на голову
        private Node<T> tail;                                           // Ссылка на хвост

        public void addLast(T element) {                                // Добавление элемента в историю
            if (memory.containsKey(element.getId())) {                  // Если хешмапа уже содержит такой элемент
                removeNode(memory.get(element.getId()));                // удаляем его из нее, чтоб не было дубликатов
            }
            final Node<T> oldTail = tail;                               // Сохраняем старый хвост
            final Node<T> newNode = new Node<>(oldTail, element, null); // Новая нода, prev это старый хвост
            tail = newNode;                                             // Хвост теперь новая нода
            if (oldTail == null) {                                      // Если предыдущего хвоста не было (мапа пустая)
                head = newNode;                                         // то новая нода теперь голова
            } else {                                                    // Если мапа не пустая
                oldTail.next = newNode;                                 // то в старый хвост кладем ссылку на новую ноду
            }
            memory.put(element.getId(), newNode);                       // Кладем ноду в хешмапу
        }

        private void removeNode(Node<T> node) {                         // Удаление ноды из списка
            final Node<T> prev = node.prev;
            final Node<T> next = node.next;
            if (prev == null) {                                         // Если нода была головой
                head = next;                                            // то голова теперь та, которая была за ней
            } else {                                                    // Если нода имела предыдущую
                prev.next = next;                                       // то исключаем текущую ноду из цепочки ссылок
            }
            if (next == null) {                                         // Если нода была хвостом
                tail = prev;                                            // то хвост теперь та, которая была перед ней
            } else {                                                    // Если нода имела следующую
                next.prev = prev;                                       // то исключаем текущую ноду из цепочки ссылок
            }
        }

        private void removeNode(int id) {                               // Удаление ноды по айди
            if (memory.get(id) != null) {                               // Если нода есть в хешмапе
                removeNode(memory.get(id));                             // удаляем ноду, вызывая метод выше
            }
        }

        private ArrayList<T> getHistory() {                             // Получить ArrayList с историей
            ArrayList<T> result = new ArrayList<>();
            for (Node<T> node = head; node != null; node = node.next) { // Начать с головы, дойти до хвоста
                result.add(node.data);                                  // Добавить все элементы
            }
            return result;
        }


        private static class Node<E> {
            private final E data;
            private Node<E> prev;
            private Node<E> next;


            public Node(Node<E> prev, E data, Node<E> next) {
                this.data = data;
                this.prev = prev;
                this.next = next;
            }
        }
    }

}
