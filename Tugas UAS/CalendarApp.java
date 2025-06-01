import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.List;

public class CalendarApp {
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
        frame = new JFrame("Calendar App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());
        
        JPanel topPanel = new JPanel(new BorderLayout());
        
        JPanel selectionPanel = new JPanel(new FlowLayout());
        
        Integer[] years = new Integer[11];
        int currentYear = Year.now().getValue();
        for (int i = 0; i < 11; i++) {
            years[i] = currentYear - 5 + i;
        }
        yearComboBox = new JComboBox<>(years);
        yearComboBox.setSelectedItem(currentYear);
        yearComboBox.addActionListener(e -> updateCalendarFromSelection());
        
        String[] months = new String[12];
        for (int i = 0; i < 12; i++) {
            months[i] = Month.of(i + 1).getDisplayName(TextStyle.FULL, Locale.getDefault());
        }
        monthComboBox = new JComboBox<>(months);
        monthComboBox.setSelectedIndex(currentDate.getMonthValue() - 1);
        monthComboBox.addActionListener(e -> updateCalendarFromSelection());
        
        selectionPanel.add(new JLabel("Month:"));
        selectionPanel.add(monthComboBox);
        selectionPanel.add(new JLabel("Year:"));
        selectionPanel.add(yearComboBox);
        
        JPanel navButtonPanel = new JPanel(new FlowLayout());
        
        prevButton = new JButton("<");
        prevButton.addActionListener(e -> {
            currentDate = currentDate.minusMonths(1);
            updateCalendar();
        });
        
        nextButton = new JButton(">");
        nextButton.addActionListener(e -> {
            currentDate = currentDate.plusMonths(1);
            updateCalendar();
        });
        
        todayButton = new JButton("Today");
        todayButton.addActionListener(e -> {
            currentDate = LocalDate.now();
            selectedDate = currentDate;
            updateCalendar();
        });
        
        navButtonPanel.add(prevButton);
        navButtonPanel.add(todayButton);
        navButtonPanel.add(nextButton);
        
        monthYearLabel = new JLabel("", SwingConstants.CENTER);
        monthYearLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        topPanel.add(selectionPanel, BorderLayout.WEST);
        topPanel.add(monthYearLabel, BorderLayout.CENTER);
        topPanel.add(navButtonPanel, BorderLayout.EAST);
        
        calendarPanel = new JPanel(new GridLayout(0, 7));
        
        JPanel eventPanel = new JPanel(new BorderLayout());
        eventListArea = new JTextArea();
        eventListArea.setEditable(false);
        eventScrollPane = new JScrollPane(eventListArea);
        
        addEventButton = new JButton("Add Event");
        addEventButton.addActionListener(e -> addEvent());
        
        JButton deleteEventButton = new JButton("Delete Selected Event");
        deleteEventButton.addActionListener(e -> deleteSelectedEvent());
        
        JPanel eventButtonPanel = new JPanel(new FlowLayout());
        eventButtonPanel.add(addEventButton);
        eventButtonPanel.add(deleteEventButton);
        
        eventPanel.add(new JLabel("Events:"), BorderLayout.NORTH);
        eventPanel.add(eventScrollPane, BorderLayout.CENTER);
        eventPanel.add(eventButtonPanel, BorderLayout.SOUTH);
        
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(calendarPanel, BorderLayout.CENTER);
        frame.add(eventPanel, BorderLayout.EAST);
        
        frame.setVisible(true);
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
        
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String dayName : dayNames) {
            JLabel label = new JLabel(dayName, SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 14));
            calendarPanel.add(label);
        }
        
        YearMonth yearMonth = YearMonth.from(currentDate);
        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstOfMonth = currentDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // Sunday = 0
        
        for (int i = 0; i < dayOfWeek; i++) {
            calendarPanel.add(new JLabel(""));
        }
        
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = LocalDate.of(currentDate.getYear(), currentDate.getMonth(), day);
            JButton dayButton = new JButton(Integer.toString(day));
            
            if (date.equals(LocalDate.now())) {
                dayButton.setBackground(Color.YELLOW);
            }
            
            if (date.equals(selectedDate)) {
                dayButton.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
                updateEventList();
            }
            
            if (eventsMap.containsKey(date) && !eventsMap.get(date).isEmpty()) {
                dayButton.setForeground(Color.BLUE);
            }
            
            dayButton.addActionListener(e -> {
                selectedDate = date;
                updateCalendar();
            });
            
            calendarPanel.add(dayButton);
        }
        
        calendarPanel.revalidate();
        calendarPanel.repaint();
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
        if (selectedDate == null || !eventsMap.containsKey(selectedDate) || eventsMap.get(selectedDate).isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No events to delete.");
            return;
        }
        
        int selectedIndex = eventListArea.getSelectionStart();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(frame, "Please select an event to delete.");
            return;
        }
        
        String text = eventListArea.getText();
        int lineNumber = -1;
        try {
            lineNumber = eventListArea.getLineOfOffset(selectedIndex);

            int start = eventListArea.getLineStartOffset(lineNumber);
            int end = eventListArea.getLineEndOffset(lineNumber);
            String selectedLine = text.substring(start, end).trim();
            
            List<String> events = eventsMap.get(selectedDate);
            events.remove(selectedLine);
            
            if (events.isEmpty()) {
                eventsMap.remove(selectedDate);
            }
            
            updateEventList();
            updateCalendar();
        } catch (javax.swing.text.BadLocationException e) {
            e.printStackTrace();
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
                sb.append(event).append("\n");
            }
        } else {
            sb.append("No events for this date.");
        }
        
        eventListArea.setText(sb.toString());
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CalendarApp());
    }
}