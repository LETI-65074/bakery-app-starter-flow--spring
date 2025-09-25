package com.vaadin.starter.bakery.backend.data.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.BatchSize;

import com.vaadin.starter.bakery.backend.data.OrderState;
/**
 * Represents an order in the bakery application.
 * Stores information about due date, time, pickup location, customer, items, state, and history.
 * <p>
 * This entity is mapped to the database as "OrderInfo" to avoid reserved word conflicts.
 * </p>
 *
 * @author LETI-65074
 */
@Entity(name = "OrderInfo")
@NamedEntityGraphs({
        @NamedEntityGraph(name = Order.ENTITY_GRAPTH_BRIEF, attributeNodes = {
                @NamedAttributeNode("customer"),
                @NamedAttributeNode("pickupLocation")
        }),
        @NamedEntityGraph(name = Order.ENTITY_GRAPTH_FULL, attributeNodes = {
                @NamedAttributeNode("customer"),
                @NamedAttributeNode("pickupLocation"),
                @NamedAttributeNode("items"),
                @NamedAttributeNode("history")
        })
})// ver se linha de comandos funicona git status
@Table(indexes = @Index(columnList = "dueDate"))
public class Order extends AbstractEntity implements OrderSummary {

    /** Entity graph name for brief order info. */
    public static final String ENTITY_GRAPTH_BRIEF = "Order.brief";
    /** Entity graph name for full order info. */
    public static final String ENTITY_GRAPTH_FULL = "Order.full";

    /** Date when the order is due. */
    @NotNull(message = "{bakery.due.date.required}")
    private LocalDate dueDate;

    /** Time when the order is due. */
    @NotNull(message = "{bakery.due.time.required}")
    private LocalTime dueTime;

    /** Location where the order will be picked up. */
    @NotNull(message = "{bakery.pickup.location.required}")
    @ManyToOne
    private PickupLocation pickupLocation;

    /** Customer who placed the order. */
    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    private Customer customer;

    /** List of items in the order. */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderColumn
    @JoinColumn
    @BatchSize(size = 1000)
    @NotEmpty
    @Valid
    private List<OrderItem> items;

    /** Current state of the order. */
    @NotNull(message = "{bakery.status.required}")
    private OrderState state;

    /** History of changes for the order. */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderColumn
    @JoinColumn
    private List<HistoryItem> history;

    /**
     * Constructs a new Order with the given user as creator.
     * Initializes state, customer, history, and items.
     *
     * @param createdBy the user who created the order
     */
    public Order(User createdBy) {
        this.state = OrderState.NEW;
        setCustomer(new Customer());
        addHistoryItem(createdBy, "Order placed");
        this.items = new ArrayList<>();
    }

    /**
     * Protected no-arg constructor required by JPA.
     */
    Order() {
        // Empty constructor is needed by Spring Data / JPA
    }

    /**
     * Adds a history item to the order.
     *
     * @param createdBy the user who made the change
     * @param comment   the comment describing the change
     */
    public void addHistoryItem(User createdBy, String comment) {
        HistoryItem item = new HistoryItem(createdBy, comment);
        item.setNewState(state);
        if (history == null) {
            history = new LinkedList<>();
        }
        history.add(item);
    }

    /** @return the due date of the order */
    @Override
    public LocalDate getDueDate() {
        return dueDate;
    }

    /** @param dueDate sets the due date of the order */
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    /** @return the due time of the order */
    @Override
    public LocalTime getDueTime() {
        return dueTime;
    }

    /** @param dueTime sets the due time of the order */
    public void setDueTime(LocalTime dueTime) {
        this.dueTime = dueTime;
    }

    /** @return the pickup location */
    @Override
    public PickupLocation getPickupLocation() {
        return pickupLocation;
    }

    /** @param pickupLocation sets the pickup location */
    public void setPickupLocation(PickupLocation pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    /** @return the customer who placed the order */
    @Override
    public Customer getCustomer() {
        return customer;
    }

    /** @param customer sets the customer for the order */
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    /** @return the list of order items */
    @Override
    public List<OrderItem> getItems() {
        return items;
    }

    /** @param items sets the list of order items */
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    /** @return the history of the order */
    public List<HistoryItem> getHistory() {
        return history;
    }

    /** @param history sets the history of the order */
    public void setHistory(List<HistoryItem> history) {
        this.history = history;
    }

    /** @return the current state of the order */
    @Override
    public OrderState getState() {
        return state;
    }

    /**
     * Changes the state of the order and adds a history item if the state changes.
     *
     * @param user  the user making the change
     * @param state the new state
     */
    public void changeState(User user, OrderState state) {
        boolean createHistory = this.state != state && this.state != null && state != null;
        this.state = state;
        if (createHistory) {
            addHistoryItem(user, "Order " + state);
        }
    }

    /** @return string representation of the order */
    @Override
    public String toString() {
        return "Order{" + "dueDate=" + dueDate + ", dueTime=" + dueTime + ", pickupLocation=" + pickupLocation
                + ", customer=" + customer + ", items=" + items + ", state=" + state + '}';
    }

    /** @return the total price of all items in the order */
    @Override
    public Integer getTotalPrice() {
        return items.stream().map(i -> i.getTotalPrice()).reduce(0, Integer::sum);
    }
}
