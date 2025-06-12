import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.List;

public class CalendarApp {
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180);
    private static final Color SECONDARY_COLOR = new Color(245, 245, 245);
    private static final Color ACCENT_COLOR = new Color(255, 87, 34);

    private JFrame frame;
    private JPanel calendarPanel;
    private JComboBox<Integer> yearComboBox;
    private JComboBox<String> monthComboBox;
    private JLabel monthYearLabel;
    private JButton todayButton;
    private JButton prevButton;
    private JButton nextButton;
    private JButton addEventButton;
    private JTextArea eventListArea;
    private JScrollPane eventScrollPane;
    
    private LocalDate currentDate;
    private LocalDate selectedDate;
    private Map<LocalDate, List<String>> eventsMap;
    
    public CalendarApp() {
        currentDate = LocalDate.now();
        selectedDate = currentDate;
        eventsMap = new HashMap<>();
        
        initializeUI();
        updateCalendar();
    }
    
    private void initializeUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        frame = new JFrame("Calendar App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(SECONDARY_COLOR);

        // Top panel with gradient
        JPanel topPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                        0, 0, PRIMARY_COLOR, 
                        getWidth(), 0, PRIMARY_COLOR.darker());
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        topPanel.setPreferredSize(new Dimension(100, 80));

        // Month and year selection
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        selectionPanel.setOpaque(false);
        
        Integer[] years = new Integer[11];
        int currentYear = Year.now().getValue();
        for (int i = 0; i < 11; i++) {
            years[i] = currentYear - 5 + i;
        }
        yearComboBox = new JComboBox<>(years);
        yearComboBox.setSelectedItem(currentYear);
        yearComboBox.setBackground(Color.WHITE);
        yearComboBox.addActionListener(e -> updateCalendarFromSelection());
        
        String[] months = new String[12];
        for (int i = 0; i < 12; i++) {
            months[i] = Month.of(i + 1).getDisplayName(TextStyle.FULL, Locale.getDefault());
        }
        monthComboBox = new JComboBox<>(months);
        monthComboBox.setSelectedIndex(currentDate.getMonthValue() - 1);
        monthComboBox.setBackground(Color.WHITE);
        monthComboBox.addActionListener(e -> updateCalendarFromSelection());
        
        selectionPanel.add(new JLabel("Month:"));
        selectionPanel.add(monthComboBox);
        selectionPanel.add(Box.createHorizontalStrut(20));
        selectionPanel.add(new JLabel("Year:"));
        selectionPanel.add(yearComboBox);

        // Navigation buttons - UPDATED FOR DAY NAVIGATION
        JPanel navButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        navButtonPanel.setOpaque(false);
        
        prevButton = createIconButton("<", 16);
        prevButton.addActionListener(e -> {
            selectedDate = selectedDate.minusDays(1); // Changed to minusDays
            currentDate = selectedDate; // Keep calendar view in sync
            updateCalendar();
        });
        
        nextButton = createIconButton(">", 16);
        nextButton.addActionListener(e -> {
            selectedDate = selectedDate.plusDays(1); // Changed to plusDays
            currentDate = selectedDate; // Keep calendar view in sync
            updateCalendar();
        });
        
        todayButton = new JButton("TODAY");
        todayButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        todayButton.setForeground(Color.BLACK);
        todayButton.setBackground(ACCENT_COLOR);
        todayButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        todayButton.addActionListener(e -> {
            currentDate = LocalDate.now();
            selectedDate = currentDate;
            updateCalendar();
        });

        navButtonPanel.add(prevButton);
        navButtonPanel.add(todayButton);
        navButtonPanel.add(nextButton);
        
        // Month year label
        monthYearLabel = new JLabel("", SwingConstants.CENTER);
        monthYearLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        monthYearLabel.setForeground(Color.WHITE);

        topPanel.add(selectionPanel, BorderLayout.WEST);
        topPanel.add(monthYearLabel, BorderLayout.CENTER);
        topPanel.add(navButtonPanel, BorderLayout.EAST);
        
        // Calendar panel
        calendarPanel = new JPanel(new GridLayout(0, 7, 5, 5));
        calendarPanel.setBackground(Color.WHITE);
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Event panel
        JPanel eventPanel = new JPanel(new BorderLayout());
        eventPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        eventPanel.setBackground(Color.WHITE);
        
        eventListArea = new JTextArea();
        eventListArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        eventListArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        eventListArea.setEditable(false);
        eventScrollPane = new JScrollPane(eventListArea);
        eventScrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        
        addEventButton = new JButton("Add Event");
        addEventButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addEventButton.setBackground(PRIMARY_COLOR);
        addEventButton.setForeground(Color.BLACK);
        addEventButton.addActionListener(e -> addEvent());
        
        JButton deleteEventButton = new JButton("Delete Event");
        deleteEventButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deleteEventButton.setBackground(ACCENT_COLOR);
        deleteEventButton.setForeground(Color.BLACK);
        deleteEventButton.addActionListener(e -> deleteSelectedEvent());
        
        JPanel eventButtonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        eventButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        eventButtonPanel.add(addEventButton);
        eventButtonPanel.add(deleteEventButton);
        
        eventPanel.add(new JLabel("EVENTS:", SwingConstants.LEFT), BorderLayout.NORTH);
        eventPanel.add(eventScrollPane, BorderLayout.CENTER);
        eventPanel.add(eventButtonPanel, BorderLayout.SOUTH);
        
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(calendarPanel, BorderLayout.CENTER);
        frame.add(eventPanel, BorderLayout.EAST);
        
        frame.setVisible(true);
    }

    private JButton createIconButton(String icon, int fontSize) {
        JButton btn = new JButton(icon);
        btn.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    private void updateCalendarFromSelection() {
        int year = (int) yearComboBox.getSelectedItem();
        int month = monthComboBox.getSelectedIndex() + 1;
        currentDate = LocalDate.of(year, month, 1);
        updateCalendar();
    }
    
    private void updateCalendar() {
        yearComboBox.setSelectedItem(currentDate.getYear());
        monthComboBox.setSelectedIndex(currentDate.getMonthValue() - 1);
        
        monthYearLabel.setText(currentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) 
                              + " " + currentDate.getYear());
        
        calendarPanel.removeAll();
        
        String[] dayNames = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        for (String dayName : dayNames) {
            JLabel label = new JLabel(dayName, SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 14));
            label.setForeground(PRIMARY_COLOR);
            calendarPanel.add(label);
        }
        
        YearMonth yearMonth = YearMonth.from(currentDate);
        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstOfMonth = currentDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;
        
        for (int i = 0; i < dayOfWeek; i++) {
            calendarPanel.add(new JLabel(""));
        }
        
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = LocalDate.of(currentDate.getYear(), currentDate.getMonth(), day);
            JButton dayButton = new JButton(Integer.toString(day));
            dayButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            dayButton.setFocusPainted(false);
            dayButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            dayButton.setBackground(Color.WHITE);
            dayButton.setBorder(BorderFactory.createLineBorder(SECONDARY_COLOR, 1));
            
            if (date.equals(LocalDate.now())) {
                dayButton.setBackground(new Color(255, 245, 157));
            }
            
            if (date.equals(selectedDate)) {
                dayButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT_COLOR, 2),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)
                ));
            }
            
            if (eventsMap.containsKey(date) && !eventsMap.get(date).isEmpty()) {
                dayButton.setForeground(PRIMARY_COLOR);
                dayButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            }
            
            dayButton.addActionListener(e -> {
                selectedDate = date;
                updateCalendar();
            });
            
            calendarPanel.add(dayButton);
        }
        
        calendarPanel.revalidate();
        calendarPanel.repaint();
        updateEventList();
    }
    
    private void addEvent() {
        if (selectedDate == null) {
            JOptionPane.showMessageDialog(frame, "Please select a date first.");
            return;
        }
        
        String eventText = JOptionPane.showInputDialog(frame, "Enter event for " + selectedDate + ":");
        if (eventText != null && !eventText.trim().isEmpty()) {
            eventsMap.computeIfAbsent(selectedDate, k -> new ArrayList<>()).add(eventText);
            updateEventList();
            updateCalendar();
        }
    }
    
    private void deleteSelectedEvent() {
        if (selectedDate == null || !eventsMap.containsKey(selectedDate)) {
            JOptionPane.showMessageDialog(frame, "No events to delete.");
            return;
        }
        
        List<String> events = eventsMap.get(selectedDate);
        if (events.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No events to delete.");
            return;
        }
        
        String selectedText = eventListArea.getSelectedText();
        if (selectedText == null || selectedText.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please select an event to delete.");
            return;
        }
        
        try {
            int selectionStart = eventListArea.getSelectionStart();
            int lineStart = eventListArea.getLineStartOffset(eventListArea.getLineOfOffset(selectionStart));
            int lineEnd = eventListArea.getLineEndOffset(eventListArea.getLineOfOffset(selectionStart));
            String fullLine = eventListArea.getText().substring(lineStart, lineEnd).trim();
            
            String eventToRemove = fullLine.replaceFirst("^-\\s*", "").trim();
            
            if (events.remove(eventToRemove)) {
                if (events.isEmpty()) {
                    eventsMap.remove(selectedDate);
                }
                updateEventList();
                updateCalendar();
            } else {
                JOptionPane.showMessageDialog(frame, "Event not found in the list.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error deleting event: " + e.getMessage());
        }
    }
    
    private void updateEventList() {
        if (selectedDate == null) {
            eventListArea.setText("");
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Events for ").append(selectedDate).append(":\n\n");
        
        if (eventsMap.containsKey(selectedDate)) {
            List<String> events = eventsMap.get(selectedDate);
            for (String event : events) {
                sb.append("- ").append(event).append("\n");
            }
        } else {
            sb.append("No events for this date.");
        }
        
        eventListArea.setText(sb.toString());
        eventListArea.setCaretPosition(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new CalendarApp();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}