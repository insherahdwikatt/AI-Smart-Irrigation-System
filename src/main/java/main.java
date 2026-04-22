import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class main
{
    public static List<double[]> loadData(String filename) throws Exception
    {
        List<double[]> data = new ArrayList<>();

        java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(filename));
        String line;
        br.readLine();

        while ((line = br.readLine()) != null)
        {
            String[] parts = line.split(",");

            double soil = Double.parseDouble(parts[0]);
            double last = Double.parseDouble(parts[1]);
            double type = Double.parseDouble(parts[2]);
            double needs = Double.parseDouble(parts[3]);

            data.add(new double[]{soil, last, type, needs});
        }

        br.close();
        return data;
    }

    public static void main(String[] args) throws Exception
    {
        SwingUtilities.invokeLater(() -> {
            gui g = new gui();
            g.setVisible(true);
        });
    }
}
class gui extends JFrame
{

    private static final Color PAGE_BG = new Color(246, 249, 252);
    private static final Color PANEL_BG = Color.WHITE;
    private static final Color PANEL_BORDER = new Color(214, 223, 233);
    private static final Color ACCENT = new Color(43, 120, 98);
    private static final Color SOFT_SECONDARY = new Color(230, 237, 247);
    private static final Color ACTION_BLUE = new Color(74, 106, 158);
    private static final Color TEXT_DARK = new Color(32, 38, 46);
    private static final Color TEXT_MUTED = new Color(95, 104, 118);
    private static final Color CARD_BORDER = new Color(221, 229, 239);
    private static final Color INPUT_BORDER = new Color(207, 216, 229);
    private static final Color WATER_ROW = new Color(253, 240, 241);
    private static final Color NORMAL_ROW = new Color(236, 245, 252);
    private static final Color ERROR_RED = new Color(176, 44, 44);

    private perceptron model;
    private final List<plants> allPlants;
    private List<perceptron.EpochStats> trainingHistory;
    private double modelAccuracy;
    private boolean trainingCompleted;

    private DataManager dataManager;

    private JTextField soilField;
    private JTextField lastField;
    private JTextField typeField;
    private JTextField xField;
    private JTextField yField;
    private JTextField selectedCountField;
    private JTextField iterField;
    private JTextField tempField;

    private JTextField testSoilField;
    private JTextField testLastField;
    private JTextField testTypeField;
    private JLabel testPredictionLabel;
    private JButton testButton;

    private DefaultTableModel plantTableModel;
    private JTable plantTable;

    private JButton loadDatasetButton;
    private JButton trainPerceptronButton;
    private JButton addButton;
    private JButton optimizeButton;
    private JButton clearButton;
    private JCheckBox normalizeCheckBox;

    private JLabel datasetStatusLabel;

    private JLabel statusLabel;
    private JLabel accuracyLabel;
    private JLabel costLabel;
    private JLabel distanceLabel;
    private JLabel perceptronEpochsLabel;
    private JLabel perceptronTestAccuracyLabel;
    private JLabel perceptronLastErrorsLabel;

    private JTextArea routeArea;
    private JTextArea perceptronInfoArea;
    private JTextArea saLogArea;

    private JSplitPane centerSplit;
    private RouteDrawingPanel drawingPanel;
    private MiniLearningCurvePanel learningCurvePanel;

    private List<plants> currentRoute;

    public gui()
    {
        this.model = null;
        this.allPlants = new ArrayList<>();
        this.currentRoute = new ArrayList<>();
        this.trainingHistory = new ArrayList<>();
        this.modelAccuracy = 0;
        this.trainingCompleted = false;
        this.dataManager = new DataManager();

        setTitle("Smart Plant Watering");
        setSize(1460, 920);
        setMinimumSize(new Dimension(1260, 820));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initComponents();
    }

    private void initComponents()
    {
        getContentPane().setBackground(PAGE_BG);
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(14, 14));
        root.setBackground(PAGE_BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        root.add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel leftPanel = new JPanel(new BorderLayout(0, 12));
        leftPanel.setBackground(PAGE_BG);
        leftPanel.setPreferredSize(new Dimension(430, 760));
        leftPanel.add(createInputPanel(), BorderLayout.CENTER);
        leftPanel.add(createControlPanel(), BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(PAGE_BG);

        centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createPlantTablePanel(), createBottomTabbedPanel());
        centerSplit.setResizeWeight(0.46);
        centerSplit.setDividerLocation(320);
        centerSplit.setContinuousLayout(true);
        centerSplit.setBorder(null);
        centerSplit.setOpaque(false);
        centerSplit.setDividerSize(10);

        centerPanel.add(centerSplit, BorderLayout.CENTER);

        JPanel mapPanel = createDrawingPanel();
        mapPanel.setPreferredSize(new Dimension(410, 760));

        root.add(leftPanel, BorderLayout.WEST);
        root.add(centerPanel, BorderLayout.CENTER);
        root.add(mapPanel, BorderLayout.EAST);

        add(root, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> centerSplit.setDividerLocation(0.40));
        updateTrainingStatusUI();
    }

    private JTabbedPane createBottomTabbedPanel()
    {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 12));
        tabs.addTab("Results", createResultsPanel());
        tabs.addTab("Perceptron", createPerceptronPanel());
        tabs.addTab("SA Logs", createSALogPanel());
        return tabs;
    }

    private JPanel createHeaderPanel()
    {
        JPanel header = createSectionPanel("Project Overview");
        TitledBorder compactTitle = BorderFactory.createTitledBorder(new LineBorder(PANEL_BORDER, 1, true), "Project Overview");
        compactTitle.setTitleFont(new Font("SansSerif", Font.BOLD, 13));
        compactTitle.setTitleColor(ACCENT.darker());
        header.setBorder(new CompoundBorder(compactTitle, new EmptyBorder(3, 10, 3, 10)));
        header.setLayout(new BorderLayout(10, 0));

        JLabel title = new JLabel("Smart Plant Watering \uD83C\uDF3F");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(new Color(33, 122, 74));


        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(1));
        header.add(textPanel, BorderLayout.WEST);

        JPanel rightToolbarWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightToolbarWrap.setOpaque(false);
        rightToolbarWrap.add(createTrainingToolbarPanel());
        header.add(rightToolbarWrap, BorderLayout.EAST);
        return header;
    }

    private JPanel createInputPanel()
    {
        JPanel panel = createSectionPanel("Plant Input Form");
        panel.setLayout(new BorderLayout(0, 12));

        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        JPanel plantFieldsPanel = new JPanel(new GridBagLayout());
        plantFieldsPanel.setOpaque(false);

        GridBagConstraints plantGbc = new GridBagConstraints();
        plantGbc.insets = new Insets(8, 4, 8, 4);
        plantGbc.fill = GridBagConstraints.HORIZONTAL;
        plantGbc.anchor = GridBagConstraints.WEST;

        soilField = createFormField();
        lastField = createFormField();
        typeField = createFormField();
        xField = createFormField();
        yField = createFormField();
        selectedCountField = createFormField();
        iterField = createFormField();
        tempField = createFormField();

        Dimension saFieldSize = new Dimension(320, 32);
        selectedCountField.setPreferredSize(saFieldSize);
        selectedCountField.setMinimumSize(saFieldSize);
        selectedCountField.setMaximumSize(saFieldSize);
        selectedCountField.setColumns(18);
        iterField.setPreferredSize(saFieldSize);
        iterField.setMinimumSize(saFieldSize);
        iterField.setMaximumSize(saFieldSize);
        iterField.setColumns(18);
        tempField.setPreferredSize(saFieldSize);
        tempField.setMinimumSize(saFieldSize);
        tempField.setMaximumSize(saFieldSize);
        tempField.setColumns(18);

        selectedCountField.setText("1");
        iterField.setText("100");
        tempField.setText("1000");

        int row = 0;
        addFormRow(plantFieldsPanel, plantGbc, row++, "Soil Moisture", soilField, "0-100");
        addFormRow(plantFieldsPanel, plantGbc, row++, "Last Watered", lastField, "0-48");
        addFormRow(plantFieldsPanel, plantGbc, row++, "Plant Type (0 / 1 / 2)", typeField, "0=Cactus, 1=Flower, 2=Herb");
        addFormRow(plantFieldsPanel, plantGbc, row++, "X Coordinate", xField, "Garden coordinate X");
        addFormRow(plantFieldsPanel, plantGbc, row++, "Y Coordinate", yField, "Garden coordinate Y");

        JPanel saPanel = new JPanel(new GridBagLayout());
        saPanel.setOpaque(false);
        TitledBorder saBorder = BorderFactory.createTitledBorder(new LineBorder(PANEL_BORDER, 1, true), "Simulated Annealing Inputs");
        saBorder.setTitleFont(new Font("SansSerif", Font.BOLD, 12));
        saBorder.setTitleColor(ACCENT.darker());
        saPanel.setBorder(new CompoundBorder(saBorder, new EmptyBorder(10, 8, 8, 8)));

        GridBagConstraints saGbc = new GridBagConstraints();
        saGbc.insets = new Insets(7, 4, 7, 4);
        saGbc.fill = GridBagConstraints.HORIZONTAL;
        saGbc.anchor = GridBagConstraints.WEST;

        int saRow = 0;
        addFormRow(saPanel, saGbc, saRow++, "Number of Plants to Water", selectedCountField, "1-added plants");
        addFormRow(saPanel, saGbc, saRow++, "SA Iterations", iterField, "int > 0");
        addFormRow(saPanel, saGbc, saRow++, "SA Initial Temperature", tempField, "number > 0");

        container.add(plantFieldsPanel);
        container.add(Box.createVerticalStrut(14));
        container.add(saPanel);

        panel.add(container, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createControlPanel()
    {
        JPanel panel = createSectionPanel("Control Buttons");
        panel.setLayout(new GridLayout(3, 1, 12, 12));

        addButton = buildButton("Predict and Add Plant", ACCENT, Color.WHITE);
        optimizeButton = buildButton("Run Simulated Annealing", ACTION_BLUE, Color.WHITE);
        clearButton = buildButton("Clear Input Fields", SOFT_SECONDARY, new Color(52, 58, 66));

        addButton.addActionListener(e -> addPlant());
        optimizeButton.addActionListener(e -> runSA());
        clearButton.addActionListener(e -> clearInputs());

        panel.add(addButton);
        panel.add(optimizeButton);
        panel.add(clearButton);

        return panel;
    }

    private JPanel createTrainingToolbarPanel()
    {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bar.setBackground(PANEL_BG);
        bar.setBorder(new CompoundBorder(new LineBorder(PANEL_BORDER, 1, true), new EmptyBorder(5, 6, 5, 6)));

        loadDatasetButton = buildCompactButton("Load Dataset", ACTION_BLUE, 120);
        trainPerceptronButton = buildCompactButton("Train Perceptron", ACCENT, 130);

        normalizeCheckBox = new JCheckBox("Normalize data");
        normalizeCheckBox.setOpaque(false);
        normalizeCheckBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        normalizeCheckBox.setForeground(TEXT_DARK);

        datasetStatusLabel = createStatusLine("Dataset: Not loaded");
        datasetStatusLabel.setPreferredSize(new Dimension(200, 22));

        loadDatasetButton.addActionListener(e -> chooseDatasetFile());
        trainPerceptronButton.addActionListener(e -> trainModel());

        bar.add(loadDatasetButton);
        bar.add(datasetStatusLabel);
        bar.add(trainPerceptronButton);
        bar.add(normalizeCheckBox);
        return bar;
    }

    private JPanel createPlantTablePanel()
    {
        JPanel panel = createSectionPanel("Added Plants and Features");
        panel.setLayout(new BorderLayout(0, 8));

        String[] columns = {"#", "Soil Moisture", "Last Watered", "Plant Type", "X", "Y", "Needs Water"};

        plantTableModel = new DefaultTableModel(columns, 0)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };

        plantTable = new JTable(plantTableModel);
        plantTable.setRowHeight(32);
        plantTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        plantTable.setIntercellSpacing(new Dimension(0, 0));
        plantTable.setShowVerticalLines(false);
        plantTable.setShowHorizontalLines(true);
        plantTable.setGridColor(new Color(230, 235, 242));
        plantTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        plantTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        plantTable.getTableHeader().setBackground(SOFT_SECONDARY);
        plantTable.getTableHeader().setForeground(TEXT_DARK);
        plantTable.getTableHeader().setPreferredSize(new Dimension(100, 34));
        plantTable.getTableHeader().setReorderingAllowed(false);
        plantTable.setFillsViewportHeight(true);
        plantTable.setAutoCreateRowSorter(true);

        DefaultTableCellRenderer rowHighlighter = new DefaultTableCellRenderer()
        {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column
            ) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                int modelRow = table.convertRowIndexToModel(row);
                Object waterCell = table.getModel().getValueAt(modelRow, 6);
                boolean needsWater = "Yes".equals(String.valueOf(waterCell));

                if (isSelected) {
                    c.setBackground(new Color(215, 228, 246));
                } else {
                    c.setBackground(needsWater ? WATER_ROW : NORMAL_ROW);
                }

                if (column == 0 || column == 1 || column == 2 || column == 4 || column == 5 || column == 6) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                if (column == 6) {
                    c.setForeground(needsWater ? new Color(177, 58, 58) : ACCENT);
                } else {
                    c.setForeground(TEXT_DARK);
                }

                return c;
            }
        };

        for (int i = 0; i < plantTable.getColumnCount(); i++) {
            plantTable.getColumnModel().getColumn(i).setCellRenderer(rowHighlighter);
        }

        plantTable.getColumnModel().getColumn(0).setMaxWidth(40);
        plantTable.getColumnModel().getColumn(1).setPreferredWidth(110);
        plantTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        plantTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        plantTable.getColumnModel().getColumn(6).setPreferredWidth(96);

        JScrollPane scrollPane = new JScrollPane(plantTable);
        scrollPane.setBorder(new LineBorder(new Color(221, 228, 238)));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPerceptronPanel()
    {
        JPanel panel = createSectionPanel("Perceptron Learning Process");
        panel.setLayout(new BorderLayout());
        final int perceptronContentWidth = 620;

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(2, 10, 4, 10));

        JPanel summary = new JPanel(new GridLayout(1, 3, 10, 10));
        summary.setOpaque(false);
        perceptronEpochsLabel = createBadgeValueLabel("0");
        perceptronTestAccuracyLabel = createBadgeValueLabel("-");
        perceptronLastErrorsLabel = createBadgeValueLabel("0");
        summary.add(createInfoBadge("Epochs", perceptronEpochsLabel));
        summary.add(createInfoBadge("Test Accuracy", perceptronTestAccuracyLabel));
        summary.add(createInfoBadge("Last Epoch Error", perceptronLastErrorsLabel));
        summary.setPreferredSize(new Dimension(perceptronContentWidth, 74));
        summary.setMaximumSize(new Dimension(perceptronContentWidth, 84));
        summary.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(summary);
        content.add(Box.createVerticalStrut(12));

        perceptronInfoArea = new JTextArea(8, 22);
        perceptronInfoArea.setEditable(false);
        perceptronInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        perceptronInfoArea.setMargin(new Insets(8, 8, 8, 8));
        perceptronInfoArea.setText(buildPerceptronHistoryText());

        JScrollPane perceptronScroll = new JScrollPane(perceptronInfoArea);
        perceptronScroll.setBorder(new TitledBorder(new LineBorder(new Color(220, 226, 236)), "Error Per Epoch"));
        perceptronScroll.getVerticalScrollBar().setUnitIncrement(14);
        perceptronScroll.setPreferredSize(new Dimension(perceptronContentWidth, 180));
        perceptronScroll.setMinimumSize(new Dimension(500, 160));
        perceptronScroll.setMaximumSize(new Dimension(perceptronContentWidth, 210));
        perceptronScroll.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(perceptronScroll);
        content.add(Box.createVerticalStrut(12));
        content.setBorder(new EmptyBorder(20, 12, 20, 30));

        learningCurvePanel = new MiniLearningCurvePanel();
        learningCurvePanel.setPreferredSize(new Dimension(500, 300));
        learningCurvePanel.setMinimumSize(new Dimension(500, 260));
        learningCurvePanel.setMaximumSize(new Dimension(500, 360));
        learningCurvePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        learningCurvePanel.setBorder(new TitledBorder(new LineBorder(new Color(220, 226, 236)), "Perceptron Learning Curve"));
        learningCurvePanel.setHistory(trainingHistory);
        content.add(learningCurvePanel);
        content.add(Box.createVerticalStrut(12));
        content.setBorder(new EmptyBorder(20, 0, 20, 35));

        JPanel testPanel = createPerceptronTestPanel();
        testPanel.setPreferredSize(new Dimension(perceptronContentWidth, 128));
        testPanel.setMaximumSize(new Dimension(perceptronContentWidth, 180));
        testPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(testPanel);

        JScrollPane tabScroll = new JScrollPane(content);
        tabScroll.setBorder(null);
        tabScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        tabScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        tabScroll.getVerticalScrollBar().setUnitIncrement(14);

        panel.add(tabScroll, BorderLayout.CENTER);
        refreshPerceptronTabUI();
        return panel;
    }

    private JPanel createPerceptronTestPanel()
    {
        JPanel panel = new JPanel(new BorderLayout(8, 6));
        panel.setOpaque(false);
        panel.setBorder(new TitledBorder(new LineBorder(new Color(220, 226, 236)), "Perceptron Test Input"));

        JPanel fields = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        fields.setOpaque(false);

        testSoilField = createFormField();
        testLastField = createFormField();
        testTypeField = createFormField();

        Dimension testFieldSize = new Dimension(85, 32);
        testSoilField.setPreferredSize(testFieldSize);
        testLastField.setPreferredSize(testFieldSize);
        testTypeField.setPreferredSize(testFieldSize);

        JPanel soilInput = createLabeledInput("Soil Moisture", testSoilField);
        JPanel lastInput = createLabeledInput("Last Watered", testLastField);
        JPanel typeInput = createLabeledInput("Plant Type (0/1/2)", testTypeField);

        Dimension inputBoxSize = new Dimension(150, 56);
        soilInput.setPreferredSize(inputBoxSize);
        lastInput.setPreferredSize(inputBoxSize);
        typeInput.setPreferredSize(inputBoxSize);

        fields.add(soilInput);
        fields.add(lastInput);
        fields.add(typeInput);

        testButton = buildButton("Test Perceptron", ACTION_BLUE, Color.WHITE);
        testButton.addActionListener(e -> testPerceptronPrediction());

        testPredictionLabel = new JLabel("   Prediction: -");
        testPredictionLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        testPredictionLabel.setForeground(TEXT_MUTED);

        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setOpaque(false);
        resultPanel.add(testButton, BorderLayout.WEST);
        resultPanel.add(testPredictionLabel, BorderLayout.CENTER);

        panel.add(fields, BorderLayout.NORTH);
        panel.add(resultPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createSALogPanel()
    {
        JPanel panel = createSectionPanel("Simulated Annealing Optimization Steps");
        panel.setLayout(new BorderLayout(0, 8));

        saLogArea = new JTextArea(16, 40);
        saLogArea.setEditable(false);
        saLogArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        saLogArea.setMargin(new Insets(10, 10, 10, 10));
        saLogArea.setText("Run Simulated Annealing to view optimization steps.");

        JScrollPane scroll = new JScrollPane(saLogArea);
        scroll.setBorder(new TitledBorder(new LineBorder(new Color(220, 226, 236)), "SA Iteration Log"));
        scroll.getVerticalScrollBar().setUnitIncrement(14);

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createResultsPanel()
    {
        JPanel panel = createSectionPanel("Final Results");
        panel.setLayout(new BorderLayout(0, 14));

        JPanel metricsGrid = new JPanel(new GridLayout(1, 3, 10, 10));
        metricsGrid.setOpaque(false);

        accuracyLabel = createMetricValue("-", ACCENT);
        costLabel = createMetricValue("-", ACTION_BLUE);
        distanceLabel = createMetricValue("-", ACCENT);

        // Keep status label for existing updateStatus() calls, but do not render it as a results box.
        statusLabel = createMetricValue("Ready", TEXT_MUTED);

        metricsGrid.add(createMetricCard("Training Accuracy", accuracyLabel));
        metricsGrid.add(createMetricCard("Final Cost", costLabel));
        metricsGrid.add(createMetricCard("Total Distance", distanceLabel));

        panel.add(metricsGrid, BorderLayout.NORTH);

        routeArea = new JTextArea(18, 36);
        routeArea.setEditable(false);
        routeArea.setLineWrap(false);
        routeArea.setWrapStyleWord(false);
        routeArea.setMargin(new Insets(10, 10, 10, 10));
        routeArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        routeArea.setText("Best route will appear here after running Simulated Annealing.");

        JScrollPane routeScrollPane = new JScrollPane(routeArea);
        routeScrollPane.setPreferredSize(new Dimension(200, 300));
        routeScrollPane.setMinimumSize(new Dimension(200, 250));
        routeScrollPane.setBorder(new TitledBorder(new LineBorder(new Color(220, 226, 236)), "Best Route Details"));
        routeScrollPane.getVerticalScrollBar().setUnitIncrement(14);

        panel.add(routeScrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDrawingPanel()
    {
        JPanel panel = createSectionPanel("Route Map");
        panel.setLayout(new BorderLayout(0, 10));

        drawingPanel = new RouteDrawingPanel();
        drawingPanel.setPreferredSize(new Dimension(350, 620));
        drawingPanel.setBorder(new LineBorder(new Color(220, 226, 236)));
        panel.add(drawingPanel, BorderLayout.CENTER);

        JLabel legend = new JLabel("Red circle=needs water | Blue square=no water");
        legend.setFont(new Font("SansSerif", Font.PLAIN, 11));
        legend.setForeground(TEXT_MUTED);
        panel.add(legend, BorderLayout.SOUTH);
        return panel;
    }

    private JTextField createFormField()
    {
        JTextField field = new JTextField();
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(140, 32));
        field.setBorder(new CompoundBorder(new LineBorder(INPUT_BORDER, 1, true), new EmptyBorder(5, 8, 5, 8)));
        return field;
    }

    private JPanel createLabeledInput(String labelText, JTextField field)
    {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(TEXT_DARK);
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private JLabel createStatusLine(String text)
    {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        label.setForeground(TEXT_MUTED);
        return label;
    }

    private JPanel createInfoBadge(String title, String value)
    {
        return createInfoBadge(title, createBadgeValueLabel(value));
    }

    private JPanel createInfoBadge(String title, JLabel valueLabel)
    {
        JPanel badge = new JPanel(new BorderLayout(0, 4));
        badge.setOpaque(false);
        badge.setBorder(new CompoundBorder(new LineBorder(CARD_BORDER, 1, true), new EmptyBorder(7, 9, 7, 9)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        titleLabel.setForeground(TEXT_MUTED);

        badge.add(titleLabel, BorderLayout.NORTH);
        badge.add(valueLabel, BorderLayout.CENTER);
        return badge;
    }

    private JLabel createBadgeValueLabel(String value)
    {
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        valueLabel.setForeground(TEXT_DARK);
        return valueLabel;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JTextField field, String helperText)
    {
        JLabel label = new JLabel(labelText + ":");
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(TEXT_DARK);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        JLabel hint = new JLabel(helperText);
        hint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        hint.setForeground(TEXT_MUTED);
        panel.add(hint, gbc);
    }

    private JPanel createSectionPanel(String title)
    {
        JPanel panel = new JPanel();
        panel.setBackground(PANEL_BG);

        TitledBorder titled = BorderFactory.createTitledBorder(new LineBorder(PANEL_BORDER, 1, true), title);
        titled.setTitleFont(new Font("SansSerif", Font.BOLD, 13));
        titled.setTitleColor(ACCENT.darker());

        panel.setBorder(new CompoundBorder(titled, new EmptyBorder(14, 14, 14, 14)));
        return panel;
    }

    private JButton buildButton(String text, Color bg, Color fg)
    {
        JButton button = new JButton(text);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(230, 40));
        button.setBorder(new CompoundBorder(new LineBorder(new Color(180, 193, 209), 1, true), new EmptyBorder(8, 14, 8, 14)));
        return button;
    }

    private JButton buildCompactButton(String text, Color bg, int width)
    {
        JButton button = buildButton(text, bg, Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(width, 30));
        return button;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel)
    {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setOpaque(true);
        card.setBackground(new Color(250, 252, 255));
        card.setBorder(new CompoundBorder(
                new LineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(9, 11, 9, 11)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        titleLabel.setForeground(TEXT_MUTED);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JLabel createMetricValue(String text, Color color)
    {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        label.setForeground(color);
        return label;
    }

    private void addPlant()
    {
        try {
            if (!trainingCompleted || model == null) {
                throw new IllegalArgumentException("Train the perceptron before adding plants.");
            }

            double soil = parseRequiredDouble(soilField, "Soil moisture");
            double last = parseRequiredDouble(lastField, "Last watered");
            int type = parsePlantType(typeField);
            double x = parseRequiredDouble(xField, "X coordinate");
            double y = parseRequiredDouble(yField, "Y coordinate");

            plants p = new plants(soil, last, type, x, y);
            p.needsWater = model.predict(applyNormalizationToPlantFeatures(soil, last, type));

            allPlants.add(p);
            currentRoute.clear();

            plantTableModel.addRow(new Object[]{
                    allPlants.size(),
                    formatDouble(soil),
                    formatDouble(last),
                    plantTypeToName(type),
                    formatDouble(x),
                    formatDouble(y),
                    p.needsWater == 1 ? "Yes" : "No"
            });

            costLabel.setText("-");
            distanceLabel.setText("-");
            routeArea.setText("Run Simulated Annealing to generate the best watering route.");
            saLogArea.setText("Run Simulated Annealing to view optimization steps.");
            updateStatus("Plant added successfully (" + (p.needsWater == 1 ? "Needs water" : "No water needed") + ")", ACCENT);
            refreshSynchronizedViews();
            clearInputs();

        } catch (IllegalArgumentException ex) {
            updateStatus("Input error", ERROR_RED);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void runSA()
    {
        try {
            if (!trainingCompleted || model == null) {
                updateStatus("Train the perceptron before running SA", ERROR_RED);
                return;
            }

            if (allPlants.isEmpty()) {
                updateStatus("Add at least one plant before running SA", ERROR_RED);
                return;
            }

            int iterMax = parsePositiveInt(iterField, "SA iterations");
            double T = parsePositiveDouble(tempField, "Initial temperature");
            int selectedCount = parseSelectedCount();

            List<plants> bestRoute = SA.SIM_ANNEAL(allPlants, selectedCount, iterMax, T);
            currentRoute = new ArrayList<>(bestRoute);

            double finalCost = SA.cost(bestRoute, allPlants);
            double finalDistance = SA.totalDistance(bestRoute);
            List<SA.SALogEntry> logEntries = SA.getLastRunLog();

            costLabel.setText(formatDouble(finalCost));
            distanceLabel.setText(formatDouble(finalDistance));
            costLabel.setForeground(ACTION_BLUE.darker());
            distanceLabel.setForeground(ACCENT);
            routeArea.setText(buildRouteDetails(bestRoute, finalCost, finalDistance));
            routeArea.setCaretPosition(0);
            saLogArea.setText(buildSALogText(logEntries));
            saLogArea.setCaretPosition(0);

            updateStatus("Optimization completed successfully", ACCENT);
            refreshSynchronizedViews();

        } catch (IllegalArgumentException ex) {
            updateStatus("SA input error", ERROR_RED);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void testPerceptronPrediction()
    {
        try {
            if (!trainingCompleted || model == null) {
                throw new IllegalArgumentException("Train the perceptron before testing predictions.");
            }

            double soil = parseRequiredDouble(testSoilField, "Test soil moisture");
            double last = parseRequiredDouble(testLastField, "Test last watered");
            int type = parsePlantType(testTypeField);

            int prediction = model.predict(applyNormalizationToPlantFeatures(soil, last, type));
            boolean needsWater = prediction == 1;

            testPredictionLabel.setText(needsWater ? "  Prediction: Needs Water" : "  Prediction: No Water Needed");
            testPredictionLabel.setForeground(needsWater ? new Color(178, 54, 54) : ACCENT);
        } catch (IllegalArgumentException ex) {
            testPredictionLabel.setText("Invalid input for test prediction");
            testPredictionLabel.setForeground(ERROR_RED);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private String buildPerceptronHistoryText()
    {
        if (trainingHistory.isEmpty()) {
            return "Train the perceptron to view errors per epoch.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Learning Log (Error Per Epoch)\n");
        sb.append("----------------------------------------------------\n");
        sb.append(String.format("%-8s %-14s %-16s%n", "Epoch", "Train Error", "Validation Error"));
        sb.append("----------------------------------------------------\n");

        for (perceptron.EpochStats stat : trainingHistory) {
            sb.append(String.format("%-8d %-14d %-16d%n", stat.epoch, stat.trainingErrors, stat.validationErrors));
        }

        return sb.toString();
    }

    private String buildSALogText(List<SA.SALogEntry> entries)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Simulated Annealing Step Log\n");
        sb.append("---------------------------------------------------------------------------------------------------------\n");
        sb.append(String.format("%-7s %-10s %-12s %-12s %-10s %-10s %-10s%n", "Iter", "Temp", "Current", "Next", "Accepted", "Improved", "BestCost"));
        sb.append("---------------------------------------------------------------------------------------------------------\n");

        for (SA.SALogEntry entry : entries) {
            sb.append(String.format(
                    "%-7d %-10s %-12s %-12s %-10s %-10s %-10s%n",
                    entry.iteration,
                    formatDouble(entry.temperature),
                    formatDouble(entry.currentCost),
                    formatDouble(entry.nextCost),
                    entry.accepted ? "Yes" : "No",
                    entry.improvedBest ? "Yes" : "No",
                    formatDouble(entry.bestCost)
            ));
        }

        sb.append("\nTotal logged steps: ").append(entries.size());
        return sb.toString();
    }

    private String buildRouteDetails(List<plants> route, double finalCost, double finalDistance)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Optimized Watering Route\n");
        sb.append("-------------------------------------------------------------\n");
        sb.append("Final Cost     : ").append(formatDouble(finalCost)).append("\n");
        sb.append("Total Distance : ").append(formatDouble(finalDistance)).append("\n");
        sb.append("Stops          : ").append(route.size()).append("\n\n");

        for (int i = 0; i < route.size(); i++) {
            plants p = route.get(i);
            sb.append(String.format(
                    "%2d) (%7s, %7s)  %-7s  Needs Water: %s%n",
                    i + 1,
                    formatDouble(p.x),
                    formatDouble(p.y),
                    plantTypeToName(p.plantType),
                    p.needsWater == 1 ? "Yes" : "No"
            ));
        }

        return sb.toString();
    }

    private void refreshSynchronizedViews()
    {
        plantTable.repaint();
        drawingPanel.setData(allPlants, currentRoute);
    }

    private void updateStatus(String text, Color color)
    {
        statusLabel.setForeground(color);
        statusLabel.setText(text);
    }

    private double getFinalTrainingAccuracy()
    {
        if (trainingHistory.isEmpty()) {
            return 0;
        }
        return trainingHistory.get(trainingHistory.size() - 1).accuracy;
    }

    private int getLastEpochErrors()
    {
        if (trainingHistory.isEmpty()) {
            return 0;
        }
        return trainingHistory.get(trainingHistory.size() - 1).errors;
    }

    private double parseRequiredDouble(JTextField field, String label)
    {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must be a valid number.");
        }
    }

    private int parsePlantType(JTextField field)
    {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Plant type is required (0, 1, or 2).");
        }

        try {
            int value = Integer.parseInt(text);
            if (value < 0 || value > 2) {
                throw new IllegalArgumentException("Plant type must be 0 (cactus), 1 (flower), or 2 (herb).");
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Plant type must be an integer (0, 1, or 2).");
        }
    }

    private int parsePositiveInt(JTextField field, String label)
    {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        try {
            int value = Integer.parseInt(text);
            if (value <= 0) {
                throw new IllegalArgumentException(label + " must be greater than 0.");
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must be a valid integer.");
        }
    }

    private int parseSelectedCount()
    {
        int selectedCount = parsePositiveInt(selectedCountField, "Number of plants to water");
        if (selectedCount > allPlants.size()) {
            throw new IllegalArgumentException(
                    "Number of plants to water cannot be greater than added plants (" + allPlants.size() + ")."
            );
        }
        return selectedCount;
    }

    private double parsePositiveDouble(JTextField field, String label)
    {
        double value = parseRequiredDouble(field, label);
        if (value <= 0) {
            throw new IllegalArgumentException(label + " must be greater than 0.");
        }
        return value;
    }

    private String plantTypeToName(int type)
    {
        if (type == 0) {
            return "Cactus";
        }
        if (type == 1) {
            return "Flower";
        }
        if (type == 2) {
            return "Herb";
        }
        return "Type " + type;
    }

    private String formatDouble(double value)
    {
        return String.format("%.2f", value);
    }

    private String formatPercent(double value)
    {
        return String.format("%.2f%%", value * 100);
    }

    private void clearInputs()
    {
        soilField.setText("");
        lastField.setText("");
        typeField.setText("");
        xField.setText("");
        yField.setText("");
    }

    private void chooseDatasetFile()
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose CSV Dataset");

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            java.io.File selectedFile = chooser.getSelectedFile();
            dataManager.loadDataset(selectedFile);

            trainingCompleted = false;
            model = null;
            trainingHistory = new ArrayList<>();
            modelAccuracy = 0;

            allPlants.clear();
            currentRoute.clear();
            plantTableModel.setRowCount(0);
            costLabel.setText("-");
            distanceLabel.setText("-");
            routeArea.setText("Best route will appear here after running Simulated Annealing.");
            saLogArea.setText("Run Simulated Annealing to view optimization steps.");
            testPredictionLabel.setText("   Prediction: -");
            testPredictionLabel.setForeground(TEXT_MUTED);
            refreshPerceptronTabUI();

            updateStatus("Dataset loaded successfully", ACCENT);
            updateTrainingStatusUI();
            refreshSynchronizedViews();

        } catch (Exception ex) {
            updateStatus("Dataset load error", ERROR_RED);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dataset Error", JOptionPane.WARNING_MESSAGE);
        }
    }


    private void trainModel()
    {
        try {
            if (!dataManager.isDatasetLoaded()) {
                throw new IllegalArgumentException("Load a dataset file before training.");
            }

            boolean normalizeData = normalizeCheckBox.isSelected();
            dataManager.setNormalizationEnabled(normalizeData);

            List<double[]> trainForModel = dataManager.getTrainDataForModel(normalizeData);
            List<double[]> valForModel = dataManager.getValDataForModel(normalizeData);
            List<double[]> testForModel = dataManager.getTestDataForModel(normalizeData);

            model = new perceptron(3, 0.01);
            model.train(trainForModel, valForModel, 20);

            double valAcc = model.accuracy(valForModel);
            double testAcc = model.accuracy(testForModel);

            trainingHistory = model.getTrainingHistory();
            modelAccuracy = testAcc;
            trainingCompleted = true;

            refreshPerceptronTabUI();

            recomputeAllPlantPredictions();
            updateStatus(
                    "Training completed. Test Accuracy: " + formatPercent(testAcc) + " | Validation: " + formatPercent(valAcc),
                    ACCENT
            );
            updateTrainingStatusUI();

        } catch (IllegalArgumentException ex) {
            updateStatus("Training error", ERROR_RED);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Training Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private double[] applyNormalizationToPlantFeatures(double soil, double last, int type)
    {
        return dataManager.applyNormalizationToFeatures(soil, last, type);
    }

    private void recomputeAllPlantPredictions()
    {
        if (model == null) {
            return;
        }

        for (int i = 0; i < allPlants.size(); i++) {
            plants p = allPlants.get(i);
            int prediction = model.predict(applyNormalizationToPlantFeatures(p.soilMoisture, p.lastWatered, p.plantType));
            p.needsWater = prediction;
            if (i < plantTableModel.getRowCount()) {
                plantTableModel.setValueAt(prediction == 1 ? "Yes" : "No", i, 6);
            }
        }

        currentRoute.clear();
        costLabel.setText("-");
        distanceLabel.setText("-");
        routeArea.setText("Run Simulated Annealing to generate the best watering route.");
        saLogArea.setText("Run Simulated Annealing to view optimization steps.");
        refreshSynchronizedViews();
    }

    private void updateTrainingStatusUI()
    {
        boolean datasetLoaded = dataManager.isDatasetLoaded();
        boolean enabled = trainingCompleted && model != null;

        normalizeCheckBox.setEnabled(datasetLoaded);
        trainPerceptronButton.setEnabled(datasetLoaded);

        soilField.setEnabled(enabled);
        lastField.setEnabled(enabled);
        typeField.setEnabled(enabled);
        xField.setEnabled(enabled);
        yField.setEnabled(enabled);
        selectedCountField.setEnabled(enabled);
        iterField.setEnabled(enabled);
        tempField.setEnabled(enabled);
        addButton.setEnabled(enabled);
        optimizeButton.setEnabled(enabled);
        clearButton.setEnabled(enabled);

        testSoilField.setEnabled(enabled);
        testLastField.setEnabled(enabled);
        testTypeField.setEnabled(enabled);
        testButton.setEnabled(enabled);

        datasetStatusLabel.setText(datasetLoaded
                ? "File: " + dataManager.getSelectedDatasetFile().getName() + " | Samples: " + dataManager.getDatasetSize()
                : "Dataset: Not loaded");
        accuracyLabel.setText(trainingCompleted ? formatPercent(modelAccuracy) : "-");
    }

    private void refreshPerceptronTabUI()
    {
        if (perceptronEpochsLabel != null) {
            perceptronEpochsLabel.setText(String.valueOf(trainingHistory.size()));
        }
        if (perceptronTestAccuracyLabel != null) {
            perceptronTestAccuracyLabel.setText(trainingCompleted ? formatPercent(modelAccuracy) : "-");
        }
        if (perceptronLastErrorsLabel != null) {
            perceptronLastErrorsLabel.setText(String.valueOf(getLastEpochErrors()));
        }
        if (perceptronInfoArea != null) {
            perceptronInfoArea.setText(buildPerceptronHistoryText());
            perceptronInfoArea.setCaretPosition(0);
        }
        if (learningCurvePanel != null) {
            learningCurvePanel.setHistory(trainingHistory);
        }
    }

    private static class MiniLearningCurvePanel extends JPanel
    {
        private List<perceptron.EpochStats> history = new ArrayList<>();

        public void setHistory(List<perceptron.EpochStats> history)
        {
            this.history = new ArrayList<>(history);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int left = 50;
            int right = 22;
            int top = 28;
            int bottom = 56;

            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, w, h);

            int plotW = Math.max(1, w - left - right);
            int plotH = Math.max(1, h - top - bottom);
            int xAxisY = h - bottom;

            g2.setColor(new Color(233, 237, 244));
            g2.fillRect(left, top, plotW, plotH);

            g2.setColor(new Color(190, 198, 210));
            g2.drawLine(left, xAxisY, w - right, xAxisY);
            g2.drawLine(left, top, left, xAxisY);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.setColor(new Color(96, 104, 117));
            g2.drawString("Epochs", left + (plotW / 2) - 16, h - 34);

            Graphics2D yAxisGraphics = (Graphics2D) g2.create();
            yAxisGraphics.rotate(-Math.PI / 2);
            yAxisGraphics.drawString("Error", -top - (plotH / 2) - 12, 16);
            yAxisGraphics.dispose();

            if (history.size() < 2)
            {
                g2.dispose();
                return;
            }

            int maxErrors = 1;
            for (perceptron.EpochStats stat : history)
            {
                maxErrors = Math.max(maxErrors, stat.trainingErrors);
                maxErrors = Math.max(maxErrors, stat.validationErrors);
            }

            drawCurve(g2, left, top, plotW, plotH, maxErrors, new Color(63, 123, 201), true);
            drawCurve(g2, left, top, plotW, plotH, maxErrors, new Color(226, 133, 44), false);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            FontMetrics fm = g2.getFontMetrics();
            int swatch = 10;
            int itemGap = 14;
            int pairGap = 18;

            String trainLabel = "Training";
            String valLabel = "Validation";
            int legendWidth = swatch + itemGap + fm.stringWidth(trainLabel)
                    + pairGap
                    + swatch + itemGap + fm.stringWidth(valLabel);
            int legendStartX = Math.max(left, left + (plotW - legendWidth) / 2);
            int legendY = h - 19;

            g2.setColor(new Color(63, 123, 201));
            g2.fillRect(legendStartX, legendY - 9, swatch, swatch);
            g2.setColor(new Color(96, 104, 117));
            int trainTextX = legendStartX + swatch + itemGap;
            g2.drawString(trainLabel, trainTextX, legendY);

            int secondX = trainTextX + fm.stringWidth(trainLabel) + pairGap;
            g2.setColor(new Color(226, 133, 44));
            g2.fillRect(secondX, legendY - 9, swatch, swatch);
            g2.setColor(new Color(96, 104, 117));
            g2.drawString(valLabel, secondX + swatch + itemGap, legendY);

            g2.dispose();
        }

        private void drawCurve(
                Graphics2D g2,
                int left,
                int top,
                int plotW,
                int plotH,
                int maxErrors,
                Color color,
                boolean training
        ) {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int prevX = -1;
            int prevY = -1;
            int n = history.size();

            for (int i = 0; i < n; i++) {
                perceptron.EpochStats stat = history.get(i);
                int errors = training ? stat.trainingErrors : stat.validationErrors;
                int x = left + (int) Math.round((i / Math.max(1.0, n - 1.0)) * plotW);
                int y = top + (int) Math.round((errors / (double) maxErrors) * plotH);

                if (prevX != -1) {
                    g2.drawLine(prevX, prevY, x, y);
                }

                g2.fillOval(x - 2, y - 2, 4, 4);
                prevX = x;
                prevY = y;
            }
        }
    }


    private static class RouteDrawingPanel extends JPanel
    {

        private static final Color MAP_BG = new Color(248, 250, 253);
        private static final Color GRID_COLOR = new Color(231, 236, 243);
        private static final Color ROUTE_LINE = new Color(49, 94, 171);
        private static final Color ROUTE_HIGHLIGHT = new Color(255, 211, 71);
        private static final Color NEEDS_WATER_FILL = new Color(219, 63, 63);
        private static final Color NEEDS_WATER_BORDER = new Color(126, 32, 32);
        private static final Color NO_WATER_FILL = new Color(49, 136, 217);
        private static final Color NO_WATER_BORDER = new Color(26, 85, 145);

        private List<plants> plantsData = new ArrayList<>();
        private List<plants> routeData = new ArrayList<>();

        public RouteDrawingPanel()
        {
            setBackground(Color.WHITE);
        }

        public void setData(List<plants> allPlants, List<plants> route)
        {
            plantsData = new ArrayList<>(allPlants);
            routeData = new ArrayList<>(route);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int pad = 38;
            int shapePad = 16;
            int leftBound = pad + shapePad;
            int rightBound = width - pad - shapePad;
            int topBound = pad + shapePad;
            int bottomBound = height - pad - shapePad;

            drawMapBackground(g2, width, height, pad);

            if (plantsData.isEmpty()) {
                g2.setColor(new Color(120, 120, 120));
                g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
                g2.dispose();
                return;
            }

            double minX = Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;
            double maxX = -Double.MAX_VALUE;
            double maxY = -Double.MAX_VALUE;

            for (plants p : plantsData) {
                minX = Math.min(minX, p.x);
                minY = Math.min(minY, p.y);
                maxX = Math.max(maxX, p.x);
                maxY = Math.max(maxY, p.y);
            }

            if (routeData.size() >= 2) {
                g2.setColor(ROUTE_LINE);
                g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                for (int i = 0; i < routeData.size() - 1; i++) {
                    plants a = routeData.get(i);
                    plants b = routeData.get(i + 1);

                    int ax = mapX(a.x, minX, maxX, leftBound, rightBound);
                    int ay = mapY(a.y, minY, maxY, topBound, bottomBound);
                    int bx = mapX(b.x, minX, maxX, leftBound, rightBound);
                    int by = mapY(b.y, minY, maxY, topBound, bottomBound);

                    g2.drawLine(ax, ay, bx, by);
                }
            }

            for (plants p : plantsData) {
                int px = mapX(p.x, minX, maxX, leftBound, rightBound);
                int py = mapY(p.y, minY, maxY, topBound, bottomBound);
                boolean inOptimizedRoute = containsPlant(routeData, p);

                float alpha = (!routeData.isEmpty() && !inOptimizedRoute) ? 0.33f : 0.96f;
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

                if (p.needsWater == 1) {
                    g2.setColor(NEEDS_WATER_FILL);
                    g2.fillOval(px - 8, py - 8, 16, 16);
                    g2.setColor(NEEDS_WATER_BORDER);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawOval(px - 8, py - 8, 16, 16);
                } else {
                    g2.setColor(NO_WATER_FILL);
                    g2.fillRect(px - 7, py - 7, 14, 14);
                    g2.setColor(NO_WATER_BORDER);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRect(px - 7, py - 7, 14, 14);
                }
            }

            g2.setComposite(AlphaComposite.SrcOver);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));

            for (int i = 0; i < routeData.size(); i++) {
                plants p = routeData.get(i);
                int px = mapX(p.x, minX, maxX, leftBound, rightBound);
                int py = mapY(p.y, minY, maxY, topBound, bottomBound);

                g2.setColor(ROUTE_HIGHLIGHT);
                g2.setStroke(new BasicStroke(2.4f));
                g2.drawOval(px - 12, py - 12, 24, 24);

                int badgeX = px + 9;
                int badgeY = py - 18;
                g2.setColor(new Color(48, 54, 63));
                g2.fillRoundRect(badgeX - 2, badgeY - 12, 22, 15, 9, 9);
                g2.setColor(Color.WHITE);
                g2.drawString(String.valueOf(i + 1), badgeX + 5, badgeY - 1);
            }

            g2.dispose();
        }

        private void drawMapBackground(Graphics2D g2, int width, int height, int pad)
        {
            g2.setColor(MAP_BG);
            g2.fillRect(0, 0, width, height);

            g2.setColor(GRID_COLOR);
            for (int i = 0; i < 6; i++) {
                int y = pad + (i * (height - 2 * pad) / 5);
                g2.drawLine(pad, y, width - pad, y);
            }
            for (int i = 0; i < 6; i++) {
                int x = pad + (i * (width - 2 * pad) / 5);
                g2.drawLine(x, pad, x, height - pad);
            }
        }

        private boolean containsPlant(List<plants> route, plants target)
        {
            for (plants p : route) {
                if (p == target) {
                    return true;
                }

                if (Math.abs(p.x - target.x) < 1e-9
                        && Math.abs(p.y - target.y) < 1e-9
                        && p.plantType == target.plantType
                        && p.needsWater == target.needsWater) {
                    return true;
                }
            }
            return false;
        }

        private int mapX(double value, double min, double max, int leftBound, int rightBound)
        {
            int width = Math.max(1, rightBound - leftBound);
            double range = max - min;

            if (Math.abs(range) < 1e-9) {
                return leftBound + width / 2;
            }

            double ratio = (value - min) / range;
            int mapped = leftBound + (int) Math.round(ratio * width);
            return clamp(mapped, leftBound, rightBound);
        }

        private int mapY(double value, double min, double max, int topBound, int bottomBound)
        {
            int height = Math.max(1, bottomBound - topBound);
            double range = max - min;

            if (Math.abs(range) < 1e-9) {
                return topBound + height / 2;
            }

            double ratio = (value - min) / range;
            int mapped = bottomBound - (int) Math.round(ratio * height);
            return clamp(mapped, topBound, bottomBound);
        }

        private int clamp(int value, int min, int max)
        {
            return Math.max(min, Math.min(max, value));
        }
    }
}
