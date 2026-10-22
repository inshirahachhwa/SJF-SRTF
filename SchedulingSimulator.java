import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Operating Systems SLA: SJF and SRTF Process Scheduling Simulator
 * Java Swing, single-file version for easy setup in VS Code.
 *
 * Time values are non-negative integers; CPU burst times must be positive integers.
 */
public class SchedulingSimulator extends JFrame {
    private final DefaultTableModel inputModel = new DefaultTableModel(
            new Object[]{"Process ID", "Arrival Time", "Burst Time"},
            0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return true; }
    };

    private final DefaultTableModel resultModel = new DefaultTableModel(
            new Object[]{"PID", "AT", "BT", "CT", "TAT", "WT", "RT"},
            0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    private final JTable inputTable = new JTable(inputModel);
    private final JTable resultTable = new JTable(resultModel);
    private final JComboBox<String> algorithmBox =
            new JComboBox<>(new String[]{"SJF (Non-preemptive)", "SRTF (Preemptive)"});
    private final JTextArea ganttArea = new JTextArea(4, 70);
    private final JTextArea summaryArea = new JTextArea(7, 70);

    public SchedulingSimulator() {
        super("OS SLA - SJF and SRTF Process Scheduling");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("CPU PROCESS SCHEDULING SIMULATOR — SJF & SRTF", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 19));
        title.setBorder(BorderFactory.createEmptyBorder(12, 8, 8, 8));
        add(title, BorderLayout.NORTH);

        addSampleRows();

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("1. Enter Process Details"));
        inputTable.setRowHeight(24);
        inputPanel.add(new JScrollPane(inputTable), BorderLayout.CENTER);

        JPanel inputButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add Process");
        JButton removeButton = new JButton("Remove Selected");
        JButton sampleButton = new JButton("Load Sample");
        inputButtons.add(addButton);
        inputButtons.add(removeButton);
        inputButtons.add(sampleButton);
        inputPanel.add(inputButtons, BorderLayout.SOUTH);

        addButton.addActionListener(e -> inputModel.addRow(new Object[]{"P" + (inputModel.getRowCount() + 1), 0, 1}));
        removeButton.addActionListener(e -> {
            int row = inputTable.getSelectedRow();
            if (row >= 0) inputModel.removeRow(row);
            else showError("Select a process row to remove.");
        });
        sampleButton.addActionListener(e -> {
            inputModel.setRowCount(0);
            addSampleRows();
        });

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.setBorder(BorderFactory.createTitledBorder("2. Select and Run Algorithm"));
        controls.add(new JLabel("Algorithm:"));
        controls.add(algorithmBox);
        JButton runButton = new JButton("Run Selected");
        JButton compareButton = new JButton("Compare Both");
        JButton clearButton = new JButton("Clear Results");
        controls.add(runButton);
        controls.add(compareButton);
        controls.add(clearButton);

        runButton.addActionListener(e -> runSelected());
        compareButton.addActionListener(e -> compareBoth());
        clearButton.addActionListener(e -> clearResults());

        JPanel outputPanel = new JPanel(new BorderLayout(6, 6));
        outputPanel.setBorder(BorderFactory.createTitledBorder("3. Scheduling Output"));
        resultTable.setRowHeight(23);
        ganttArea.setEditable(false);
        ganttArea.setLineWrap(true);
        ganttArea.setWrapStyleWord(true);
        summaryArea.setEditable(false);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);

        JPanel ganttPanel = new JPanel(new BorderLayout());
        ganttPanel.setBorder(BorderFactory.createTitledBorder("Gantt Chart (time units)"));
        ganttPanel.add(new JScrollPane(ganttArea), BorderLayout.CENTER);

        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Performance Summary"));
        summaryPanel.add(new JScrollPane(summaryArea), BorderLayout.CENTER);

        JSplitPane bottomSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, ganttPanel, summaryPanel);
        bottomSplit.setResizeWeight(0.45);

        outputPanel.add(new JScrollPane(resultTable), BorderLayout.NORTH);
        outputPanel.add(bottomSplit, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.add(inputPanel, BorderLayout.NORTH);
        center.add(controls, BorderLayout.CENTER);
        center.add(outputPanel, BorderLayout.SOUTH);

        // A vertical split allows the input and output areas to remain usable when resized.
        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputPanel,
                new JPanel(new BorderLayout(6, 6)));
        JPanel lower = (JPanel) mainSplit.getBottomComponent();
        lower.add(controls, BorderLayout.NORTH);
        lower.add(outputPanel, BorderLayout.CENTER);
        mainSplit.setResizeWeight(0.35);
        add(mainSplit, BorderLayout.CENTER);

        JLabel note = new JLabel("AT = Arrival Time | BT = Burst Time | CT = Completion Time | TAT = Turnaround | WT = Waiting | RT = Response");
        note.setBorder(BorderFactory.createEmptyBorder(4, 10, 8, 10));
        note.setFont(new Font("SansSerif", Font.PLAIN, 11));
        add(note, BorderLayout.SOUTH);
    }

    private void addSampleRows() {
        inputModel.addRow(new Object[]{"P1", 0, 7});
        inputModel.addRow(new Object[]{"P2", 2, 4});
        inputModel.addRow(new Object[]{"P3", 4, 1});
        inputModel.addRow(new Object[]{"P4", 5, 4});
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Input Error", JOptionPane.ERROR_MESSAGE);
    }

    private List<ProcessInfo> readProcesses() {
        if (inputTable.isEditing()) inputTable.getCellEditor().stopCellEditing();
        if (inputModel.getRowCount() == 0) {
            showError("Enter at least one process.");
            return null;
        }

        List<ProcessInfo> processes = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        try {
            for (int r = 0; r < inputModel.getRowCount(); r++) {
                Object pidObj = inputModel.getValueAt(r, 0);
                Object atObj = inputModel.getValueAt(r, 1);
                Object btObj = inputModel.getValueAt(r, 2);

                if (pidObj == null || atObj == null || btObj == null ||
                        pidObj.toString().trim().isEmpty() ||
                        atObj.toString().trim().isEmpty() ||
                        btObj.toString().trim().isEmpty()) {
                    showError("Row " + (r + 1) + " has an empty value.");
                    return null;
                }

                String pid = pidObj.toString().trim();
                if (!ids.add(pid)) {
                    showError("Process IDs must be unique. Duplicate ID: " + pid);
                    return null;
                }

                int arrival = Integer.parseInt(atObj.toString().trim());
                int burst = Integer.parseInt(btObj.toString().trim());
                if (arrival < 0) {
                    showError("Arrival time cannot be negative (row " + (r + 1) + ").");
                    return null;
                }
                if (burst <= 0) {
                    showError("Burst time must be a positive integer (row " + (r + 1) + ").");
                    return null;
                }
                processes.add(new ProcessInfo(pid, arrival, burst, r));
            }
        } catch (NumberFormatException ex) {
            showError("Arrival Time and Burst Time must be whole numbers.");
            return null;
        }
        return processes;
    }

    private void runSelected() {
        List<ProcessInfo> processes = readProcesses();
        if (processes == null) return;
        String choice = (String) algorithmBox.getSelectedItem();
        ScheduleResult result = choice.startsWith("SJF")
                ? runSJF(processes) : runSRTF(processes);
        displayResult(result, true);
    }

    private void compareBoth() {
        List<ProcessInfo> processes = readProcesses();
        if (processes == null) return;
        ScheduleResult sjf = runSJF(processes);
        ScheduleResult srtf = runSRTF(processes);
        displayResult(sjf, true);
        String comparison = String.format(Locale.US,
                "SJF vs SRTF COMPARISON%n" +
                "----------------------------------------%n" +
                "Metric                     SJF          SRTF%n" +
                "Average Waiting Time       %8.2f     %8.2f%n" +
                "Average Turnaround Time    %8.2f     %8.2f%n" +
                "Average Response Time      %8.2f     %8.2f%n" +
                "----------------------------------------%n" +
                "Note: Lower averages are generally preferable. Interpret them with the workload and pre-emption overhead in mind.",
                sjf.avgWT, srtf.avgWT, sjf.avgTAT, srtf.avgTAT, sjf.avgRT, srtf.avgRT);
        summaryArea.setText(comparison);
        ganttArea.setText("SJF:  " + sjf.ganttText + "\n\nSRTF: " + srtf.ganttText);
        resultModel.setRowCount(0);
        for (ProcessInfo p : sjf.processes) addResultRow(p, "SJF");
        for (ProcessInfo p : srtf.processes) addResultRow(p, "SRTF");
        // In comparison mode the first column is annotated so the two result sets are distinguishable.
    }

    private void clearResults() {
        resultModel.setRowCount(0);
        ganttArea.setText("");
        summaryArea.setText("");
    }

    private void displayResult(ScheduleResult result, boolean showRows) {
        resultModel.setRowCount(0);
        if (showRows) {
            for (ProcessInfo p : result.processes) addResultRow(p, null);
        }
        ganttArea.setText(result.ganttText);
        summaryArea.setText(String.format(Locale.US,
                "Algorithm: %s%nAverage Waiting Time: %.2f%nAverage Turnaround Time: %.2f%n" +
                "Average Response Time: %.2f%nTotal elapsed time: %d time units%n",
                result.algorithm, result.avgWT, result.avgTAT, result.avgRT, result.totalTime));
    }

    private void addResultRow(ProcessInfo p, String prefix) {
        resultModel.addRow(new Object[]{
                prefix == null ? p.pid : prefix + " - " + p.pid,
                p.arrival, p.burst, p.completion,
                p.completion - p.arrival,
                p.completion - p.arrival - p.burst,
                p.firstStart - p.arrival
        });
    }

    private static ScheduleResult runSJF(List<ProcessInfo> source) {
        List<ProcessInfo> ps = copyProcesses(source);
        int n = ps.size(), done = 0, time = 0;
        List<Segment> segments = new ArrayList<>();
        while (done < n) {
            ProcessInfo best = null;
            for (ProcessInfo p : ps) {
                if (!p.completed && p.arrival <= time) {
                    if (best == null || p.burst < best.burst ||
                            (p.burst == best.burst && (p.arrival < best.arrival ||
                                    (p.arrival == best.arrival && p.order < best.order)))) {
                        best = p;
                    }
                }
            }
            if (best == null) {
                int nextArrival = Integer.MAX_VALUE;
                for (ProcessInfo p : ps) if (!p.completed) nextArrival = Math.min(nextArrival, p.arrival);
                addSegment(segments, "IDLE", time, nextArrival);
                time = nextArrival;
                continue;
            }
            best.firstStart = time;
            int start = time;
            time += best.burst;
            best.completion = time;
            best.completed = true;
            done++;
            addSegment(segments, best.pid, start, time);
        }
        return makeResult("SJF (Non-preemptive)", ps, segments, time);
    }

    private static ScheduleResult runSRTF(List<ProcessInfo> source) {
        List<ProcessInfo> ps = copyProcesses(source);
        int n = ps.size(), done = 0, time = 0;
        List<Segment> segments = new ArrayList<>();
        while (done < n) {
            ProcessInfo best = null;
            for (ProcessInfo p : ps) {
                if (!p.completed && p.arrival <= time && p.remaining > 0) {
                    if (best == null || p.remaining < best.remaining ||
                            (p.remaining == best.remaining && (p.arrival < best.arrival ||
                                    (p.arrival == best.arrival && p.order < best.order)))) {
                        best = p;
                    }
                }
            }
            if (best == null) {
                int nextArrival = Integer.MAX_VALUE;
                for (ProcessInfo p : ps) if (!p.completed) nextArrival = Math.min(nextArrival, p.arrival);
                addSegment(segments, "IDLE", time, nextArrival);
                time = nextArrival;
                continue;
            }
            if (best.firstStart == -1) best.firstStart = time;
            addSegment(segments, best.pid, time, time + 1);
            best.remaining--;
            time++;
            if (best.remaining == 0) {
                best.completed = true;
                best.completion = time;
                done++;
            }
        }
        return makeResult("SRTF (Preemptive)", ps, segments, time);
    }

    private static void addSegment(List<Segment> segments, String pid, int start, int end) {
        if (end <= start) return;
        if (!segments.isEmpty()) {
            Segment last = segments.get(segments.size() - 1);
            if (last.pid.equals(pid) && last.end == start) {
                last.end = end;
                return;
            }
        }
        segments.add(new Segment(pid, start, end));
    }

    private static ScheduleResult makeResult(String algorithm, List<ProcessInfo> ps,
                                             List<Segment> segments, int totalTime) {
        double totalWT = 0, totalTAT = 0, totalRT = 0;
        ps.sort(Comparator.comparingInt(p -> p.order));
        for (ProcessInfo p : ps) {
            int tat = p.completion - p.arrival;
            int wt = tat - p.burst;
            int rt = p.firstStart - p.arrival;
            totalWT += wt;
            totalTAT += tat;
            totalRT += rt;
        }
        StringBuilder gantt = new StringBuilder();
        for (Segment s : segments) {
            if (gantt.length() > 0) gantt.append(" | ");
            gantt.append(s.pid).append(" [").append(s.start).append("–").append(s.end).append("]");
        }
        return new ScheduleResult(algorithm, ps, segments, gantt.toString(),
                totalWT / ps.size(), totalTAT / ps.size(), totalRT / ps.size(), totalTime);
    }

    private static List<ProcessInfo> copyProcesses(List<ProcessInfo> source) {
        List<ProcessInfo> copy = new ArrayList<>();
        for (ProcessInfo p : source) copy.add(new ProcessInfo(p.pid, p.arrival, p.burst, p.order));
        return copy;
    }

    private static class ProcessInfo {
        String pid;
        int arrival, burst, remaining, order;
        int completion = 0, firstStart = -1;
        boolean completed = false;

        ProcessInfo(String pid, int arrival, int burst, int order) {
            this.pid = pid;
            this.arrival = arrival;
            this.burst = burst;
            this.remaining = burst;
            this.order = order;
        }
    }

    private static class Segment {
        String pid;
        int start, end;
        Segment(String pid, int start, int end) { this.pid = pid; this.start = start; this.end = end; }
    }

    private static class ScheduleResult {
        String algorithm, ganttText;
        List<ProcessInfo> processes;
        List<Segment> segments;
        double avgWT, avgTAT, avgRT;
        int totalTime;
        ScheduleResult(String algorithm, List<ProcessInfo> processes, List<Segment> segments,
                       String ganttText, double avgWT, double avgTAT, double avgRT, int totalTime) {
            this.algorithm = algorithm;
            this.processes = processes;
            this.segments = segments;
            this.ganttText = ganttText;
            this.avgWT = avgWT;
            this.avgTAT = avgTAT;
            this.avgRT = avgRT;
            this.totalTime = totalTime;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SchedulingSimulator().setVisible(true));
    }
}
