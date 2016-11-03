package agano.runner.swing;

import agano.runner.parameter.SelectionParameter;
import agano.runner.state.User;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public final class UserListImpl implements UserList {

    private final JPanel base;
    private final JScrollPane scrollPane;
    private final JList<User> list;
    private final DefaultListModel<User> model;

    @Inject
    public UserListImpl(EventBus eventBus) {

        scrollPane = new JScrollPane();

        model = new DefaultListModel<>();

        list = new JList<>(model);
        list.setBorder(BorderFactory.createEmptyBorder());
        list.addListSelectionListener(e -> {
            User selected = model.get(e.getFirstIndex());
            eventBus.post(new SelectionParameter(e, selected));
        });
        list.setCellRenderer(new UserListCellRenderer());

        scrollPane.getViewport().setView(list);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        base = new JPanel();

        LayoutManager layout = new BorderLayout();
        base.setLayout(layout);

        base.add(scrollPane, BorderLayout.CENTER);
//        this.base.setMinimumSize(new Dimension(100, 100));
    }

    @Override
    public synchronized void update(@Nonnull List<User> element) {
        for (int i = 0; i < element.size(); i++) {
            if (model.get(i) != element.get(i)) {
                model.set(i, element.get(i));
            }
        }
    }

    @Override
    public JComponent component() {
        return base;
    }

    private static final class UserListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(
                JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setText(((User) value).getName());

            return label;

        }

    }

}