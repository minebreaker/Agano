package agano.runner.state;

import agano.ipmsg.Message;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;

public final class State {

    private List<User> users;
    private Optional<User> selected;

    private State(@Nonnull List<User> users, Optional<User> selected) {
        this.users = ImmutableList.copyOf(checkNotNull(users));
        this.selected = checkNotNull(selected);
    }

    @Nonnull
    static State initialState() {
        return new State(Collections.emptyList(), Optional.empty());
    }

    @Nonnull
    private State copy() {
        return new State(users, selected);
    }

    /**
     * Note that returned list is immutable.
     *
     * @return immutable list of users
     */
    @Nonnull
    public List<User> getUsers() {
        return users;
    }

    @Nonnull
    public Optional<User> getSelectedUser() {
        return selected;
    }

    @Nonnull
    public State addUser(@Nonnull User user) {
        checkNotNull(user);

        State newState = copy();
        newState.users = ImmutableList.<User>builder().addAll(users).add(user).build();
        return newState;
    }

    @Nonnull
    public State removeUser(@Nonnull User user) {
        checkNotNull(user);

        State newState = copy();
        newState.users = users.stream().filter(e -> !e.equals(user)).collect(toList());
        return newState;
    }

    @Nonnull
    public State selectUser(@Nonnull User user) {
        checkNotNull(user);

        State newState = copy();
        if (!newState.getUsers().contains(user)) throw new IllegalArgumentException("Unregistered user.");
        newState.selected = Optional.of(user);
        return newState;
    }

    @Nonnull
    public State swapUser(@Nonnull UnaryOperator<List<User>> updater) {
        checkNotNull(updater);

        State newState = copy();
        newState.users = updater.apply(users);
        return newState;
    }

    @Nonnull
    public State addTalkToUser(User toWhom, Message message) {
        State newState = copy();
        ImmutableList.Builder<User> builder = ImmutableList.builder();
        for (User each : users) {
            builder.add(each.equals(toWhom) ? each.addTalk(message) : each);
        }
        newState.users = builder.build();

        return newState;
    }

}