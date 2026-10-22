import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class SchedulingSimulator extends JFrame {

    private final DefaultTableModel inputModel =
            new DefaultTableModel(
                    new Object[]{"Process ID", "Arrival Time", "Burst Time"}, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return true;
                }
            };

    private final DefaultTableModel resultModel =
            new DefaultTableModel(
                    new Object[]{"PID", "AT", "BT", "CT", "TAT", "WT", "RT"}, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };

    private final JTable inputTable = new JTable(inputModel);
    private final JTable resultTable = new JTable(resultModel);

    private final JComboBox<String> algorithmBox =
            new JComboBox<>(new String[]{
                    "SJF (Non-preemptive)",
                    "SRTF (Preemptive)"
            });

    private final JTextArea ganttArea = new JTextArea();
    private final JTextArea summaryArea = new JTextArea();

    private JSplitPane mainSplit;
    private JSplitPane bottomSplit;

    public SchedulingSimulator() {
        super("OS SLA - SJF and SRTF Process Scheduling");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 950);
        setMinimumSize(new Dimension(950, 750));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));

        // APPLICATION TITLE
        JLabel title = new JLabel(
                "CPU PROCESS SCHEDULING SIMULATOR — SJF & SRTF",
                SwingConstants.CENTER);

        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        add(title, BorderLayout.NORTH);

        addSampleRows();

        // INPUT PANEL
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(
                BorderFactory.createTitledBorder("1. Enter Process Details"));

        inputTable.setRowHeight(25);
        inputPanel.add(new JScrollPane(inputTable), BorderLayout.CENTER);

        JPanel inputButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addButton = new JButton("Add Process");
        JButton removeButton = new JButton("Remove Selected");
        JButton sampleButton = new JButton("Load Sample");

        inputButtons.add(addButton);
        inputButtons.add(removeButton);
        inputButtons.add(sampleButton);

        inputPanel.add(inputButtons, BorderLayout.SOUTH);

        addButton.addActionListener(e ->
                inputModel.addRow(new Object[]{
                        "P" + (inputModel.getRowCount() + 1), 0, 1
                }));

        removeButton.addActionListener(e -> {
            int row = inputTable.getSelectedRow();

            if (row >= 0) {
                inputModel.removeRow(row);
            } else {
                showError("Select a process row to remove.");
            }
        });

        sampleButton.addActionListener(e -> {
            inputModel.setRowCount(0);
            addSampleRows();
        });

        // ALGORITHM CONTROLS
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.setBorder(
                BorderFactory.createTitledBorder("2. Select and Run Algorithm"));

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

        // RESULTS TABLE
        resultTable.setRowHeight(24);
        resultTable.setFillsViewportHeight(true);

        JScrollPane resultScroll = new JScrollPane(resultTable);
        resultScroll.setPreferredSize(new Dimension(1000, 160));

        // GANTT CHART PANEL
        ganttArea.setEditable(false);
        ganttArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        ganttArea.setLineWrap(true);
        ganttArea.setWrapStyleWord(true);

        JPanel ganttPanel = new JPanel(new BorderLayout());
        ganttPanel.setBorder(
                BorderFactory.createTitledBorder("Gantt Chart (time units)"));
        ganttPanel.add(new JScrollPane(ganttArea), BorderLayout.CENTER);
        ganttPanel.setMinimumSize(new Dimension(300, 100));

        // PERFORMANCE SUMMARY PANEL
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        summaryArea.setMargin(new Insets(8, 8, 8, 8));

        JScrollPane summaryScroll = new JScrollPane(summaryArea);
        summaryScroll.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        summaryScroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBorder(
                BorderFactory.createTitledBorder("Performance Summary"));
        summaryPanel.add(summaryScroll, BorderLayout.CENTER);
        summaryPanel.setMinimumSize(new Dimension(300, 180));

        // GANTT AND SUMMARY SPLIT
        bottomSplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                ganttPanel,
                summaryPanel);

        bottomSplit.setContinuousLayout(true);
        bottomSplit.setOneTouchExpandable(true);
        bottomSplit.setDividerSize(10);
        bottomSplit.setResizeWeight(0.35);

        // OUTPUT PANEL
        JPanel outputPanel = new JPanel(new BorderLayout(5, 5));
        outputPanel.setBorder(
                BorderFactory.createTitledBorder("3. Scheduling Output"));

        outputPanel.add(resultScroll, BorderLayout.NORTH);
        outputPanel.add(bottomSplit, BorderLayout.CENTER);

        // LOWER PANEL
        JPanel lowerPanel = new JPanel(new BorderLayout(5, 5));
        lowerPanel.add(controls, BorderLayout.NORTH);
        lowerPanel.add(outputPanel, BorderLayout.CENTER);

        // MAIN SPLIT
        mainSplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                inputPanel,
                lowerPanel);

        mainSplit.setContinuousLayout(true);
        mainSplit.setOneTouchExpandable(true);
        mainSplit.setDividerSize(10);
        mainSplit.setResizeWeight(0.25);

        add(mainSplit, BorderLayout.CENTER);

        JLabel note = new JLabel(
                "AT = Arrival Time | BT = Burst Time | CT = Completion Time | " +
                "TAT = Turnaround Time | WT = Waiting Time | RT = Response Time");

        note.setBorder(BorderFactory.createEmptyBorder(5, 10, 8, 10));
        note.setFont(new Font("SansSerif", Font.PLAIN, 12));
        add(note, BorderLayout.SOUTH);

        // INITIAL DIVIDER POSITIONS
        SwingUtilities.invokeLater(() -> {
            mainSplit.setDividerLocation(0.25);
            bottomSplit.setDividerLocation(0.35);
        });
    }

    private void addSampleRows() {
        inputModel.addRow(new Object[]{"P1", 0, 7});
        inputModel.addRow(new Object[]{"P2", 2, 4});
        inputModel.addRow(new Object[]{"P3", 4, 1});
        inputModel.addRow(new Object[]{"P4", 5, 4});
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this, message, "Input Error", JOptionPane.ERROR_MESSAGE);
    }

    // VALIDATE USER INPUT
    private List<ProcessInfo> readProcesses() {
        if (inputTable.isEditing()) {
            inputTable.getCellEditor().stopCellEditing();
        }

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
                    showError("Please fill in every field in row " + (r + 1));
                    return null;
                }

                String pid = pidObj.toString().trim();

                if (!ids.add(pid)) {
                    showError("Process IDs must be unique. Duplicate: " + pid);
                    return null;
                }

                int arrival = Integer.parseInt(atObj.toString().trim());
                int burst = Integer.parseInt(btObj.toString().trim());

                if (arrival < 0) {
                    showError("Arrival time cannot be negative.");
                    return null;
                }

                if (burst <= 0) {
                    showError("Burst time must be greater than zero.");
                    return null;
                }

                processes.add(
                        new ProcessInfo(pid, arrival, burst, r));
            }
        } catch (NumberFormatException ex) {
            showError("Arrival Time and Burst Time must be whole numbers.");
            return null;
        }

        return processes;
    }

    // RUN SELECTED ALGORITHM
    private void runSelected() {
        List<ProcessInfo> processes = readProcesses();
        if (processes == null) return;

        String choice = (String) algorithmBox.getSelectedItem();

        ScheduleResult result = choice.startsWith("SJF")
                ? runSJF(processes)
                : runSRTF(processes);

        displayResult(result);
    }

    // COMPARE SJF AND SRTF
    private void compareBoth() {
        List<ProcessInfo> processes = readProcesses();
        if (processes == null) return;

        ScheduleResult sjf = runSJF(processes);
        ScheduleResult srtf = runSRTF(processes);

        resultModel.setRowCount(0);

        for (ProcessInfo p : sjf.processes) {
            addResultRow(p, "SJF");
        }

        for (ProcessInfo p : srtf.processes) {
            addResultRow(p, "SRTF");
        }

        ganttArea.setText(
                "SJF (Non-preemptive)\n" +
                sjf.ganttText +
                "\n\nSRTF (Preemptive)\n" +
                srtf.ganttText);

        summaryArea.setText(String.format(Locale.US,
                "PERFORMANCE COMPARISON\n\n" +
                "%-28s %10s %10s\n" +
                "%-28s %10.2f %10.2f\n" +
                "%-28s %10.2f %10.2f\n" +
                "%-28s %10.2f %10.2f\n\n" +
                "Comparison uses the same process input.\n" +
                "Lower averages are preferable for these metrics.",
                "Metric", "SJF", "SRTF",
                "Average Waiting Time", sjf.avgWT, srtf.avgWT,
                "Average Turnaround Time", sjf.avgTAT, srtf.avgTAT,
                "Average Response Time", sjf.avgRT, srtf.avgRT));

        summaryArea.setCaretPosition(0);

        // Give the summary more space when displaying the comparison.
        SwingUtilities.invokeLater(() ->
                bottomSplit.setDividerLocation(0.25));
    }

    private void clearResults() {
        resultModel.setRowCount(0);
        ganttArea.setText("");
        summaryArea.setText("");
    }

    // DISPLAY ONE ALGORITHM'S RESULTS
    private void displayResult(ScheduleResult result) {
        resultModel.setRowCount(0);

        for (ProcessInfo p : result.processes) {
            addResultRow(p, null);
        }

        ganttArea.setText(
                result.algorithm + "\n\n" + result.ganttText);

        summaryArea.setText(String.format(Locale.US,
                "Algorithm: %s\n\n" +
                "Average Waiting Time: %.2f\n" +
                "Average Turnaround Time: %.2f\n" +
                "Average Response Time: %.2f\n" +
                "Total elapsed time: %d time units\n",
                result.algorithm,
                result.avgWT,
                result.avgTAT,
                result.avgRT,
                result.totalTime));

        summaryArea.setCaretPosition(0);

        // Reserve extra room for the complete performance summary.
        SwingUtilities.invokeLater(() ->
                bottomSplit.setDividerLocation(0.30));
    }

    private void addResultRow(ProcessInfo p, String prefix) {
        int tat = p.completion - p.arrival;
        int wt = tat - p.burst;
        int rt = p.firstStart - p.arrival;

        resultModel.addRow(new Object[]{
                prefix == null ? p.pid : prefix + " - " + p.pid,
                p.arrival,
                p.burst,
                p.completion,
                tat,
                wt,
                rt
        });
    }

    // SJF: NON-PREEMPTIVE SHORTEST JOB FIRST
    private static ScheduleResult runSJF(List<ProcessInfo> source) {
        List<ProcessInfo> ps = copyProcesses(source);
        int done = 0;
        int time = 0;
        List<Segment> segments = new ArrayList<>();

        while (done < ps.size()) {
            ProcessInfo best = null;

            for (ProcessInfo p : ps) {
                if (!p.completed && p.arrival <= time) {
                    if (best == null ||
                            p.burst < best.burst ||
                            (p.burst == best.burst &&
                            (p.arrival < best.arrival ||
                            (p.arrival == best.arrival &&
                            p.order < best.order)))) {
                        best = p;
                    }
                }
            }

            if (best == null) {
                int nextArrival = Integer.MAX_VALUE;

                for (ProcessInfo p : ps) {
                    if (!p.completed) {
                        nextArrival = Math.min(nextArrival, p.arrival);
                    }
                }

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

        return makeResult(
                "SJF (Non-preemptive)", ps, segments, time);
    }

    // SRTF: PREEMPTIVE SHORTEST REMAINING TIME FIRST
    private static ScheduleResult runSRTF(List<ProcessInfo> source) {
        List<ProcessInfo> ps = copyProcesses(source);
        int done = 0;
        int time = 0;
        List<Segment> segments = new ArrayList<>();

        while (done < ps.size()) {
            ProcessInfo best = null;

            for (ProcessInfo p : ps) {
                if (!p.completed &&
                        p.arrival <= time &&
                        p.remaining > 0) {

                    if (best == null ||
                            p.remaining < best.remaining ||
                            (p.remaining == best.remaining &&
                            (p.arrival < best.arrival ||
                            (p.arrival == best.arrival &&
                            p.order < best.order)))) {
                        best = p;
                    }
                }
            }

            if (best == null) {
                int nextArrival = Integer.MAX_VALUE;

                for (ProcessInfo p : ps) {
                    if (!p.completed) {
                        nextArrival = Math.min(nextArrival, p.arrival);
                    }
                }

                addSegment(segments, "IDLE", time, nextArrival);
                time = nextArrival;
                continue;
            }

            if (best.firstStart == -1) {
                best.firstStart = time;
            }

            addSegment(segments, best.pid, time, time + 1);

            best.remaining--;
            time++;

            if (best.remaining == 0) {
                best.completed = true;
                best.completion = time;
                done++;
            }
        }

        return makeResult(
                "SRTF (Preemptive)", ps, segments, time);
    }

    // MERGE ADJACENT SEGMENTS FOR THE SAME PROCESS
    private static void addSegment(
            List<Segment> segments,
            String pid,
            int start,
            int end) {

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

    // CALCULATE CT, TAT, WT, RT AND AVERAGES
    private static ScheduleResult makeResult(
            String algorithm,
            List<ProcessInfo> ps,
            List<Segment> segments,
            int totalTime) {

        double totalWT = 0;
        double totalTAT = 0;
        double totalRT = 0;

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
            if (gantt.length() > 0) {
                gantt.append(" | ");
            }

            gantt.append(s.pid)
                    .append(" [")
                    .append(s.start)
                    .append("-")
                    .append(s.end)
                    .append("]");
        }

        int n = ps.size();

        return new ScheduleResult(
                algorithm,
                ps,
                segments,
                gantt.toString(),
                totalWT / n,
                totalTAT / n,
                totalRT / n,
                totalTime);
    }

    private static List<ProcessInfo> copyProcesses(
            List<ProcessInfo> source) {

        List<ProcessInfo> copy = new ArrayList<>();

        for (ProcessInfo p : source) {
            copy.add(new ProcessInfo(
                    p.pid, p.arrival, p.burst, p.order));
        }

        return copy;
    }

    private static class ProcessInfo {
        String pid;
        int arrival;
        int burst;
        int remaining;
        int order;
        int completion = 0;
        int firstStart = -1;
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
        int start;
        int end;

        Segment(String pid, int start, int end) {
            this.pid = pid;
            this.start = start;
            this.end = end;
        }
    }

    private static class ScheduleResult {
        String algorithm;
        String ganttText;
        List<ProcessInfo> processes;
        List<Segment> segments;
        double avgWT;
        double avgTAT;
        double avgRT;
        int totalTime;

        ScheduleResult(
                String algorithm,
                List<ProcessInfo> processes,
                List<Segment> segments,
                String ganttText,
                double avgWT,
                double avgTAT,
                double avgRT,
                int totalTime) {

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
        SwingUtilities.invokeLater(() -> {
            SchedulingSimulator app = new SchedulingSimulator();
            app.setVisible(true);
        });
    }
}