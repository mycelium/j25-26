package org.example;

import org.example.generator.*;
import org.example.grid.*;
import org.example.grid.Point;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.List;
import java.util.Random;

public class Visualizer {

    private class JGridPanel extends JPanel{
        final Grid gr;
        final int         freeSpace = 30;
        private final int ptsSize   = 10;

        public JGridPanel(Grid aGr) {
            gr = aGr;
        }

        @Override
        public void paintComponent(Graphics g){
            super.paintComponent(g);
            printPoints(g, gr.getGeneratedPoints(), ptsSize);
            printPoints(g, gr.getAPoints(), ptsSize + 5);

            g.setColor(Color.BLACK);
            int rectWidth = gr.getXLength() + ptsSize;
            int rectHeight = gr.getYLength() + ptsSize;
            g.drawRect(freeSpace, freeSpace, rectWidth, rectHeight);

            for(int i = 0; i < gr.getXLength(); i += 30){
                g.drawLine(freeSpace + i + ptsSize,freeSpace + rectHeight + 5,
                           freeSpace + i + + ptsSize,freeSpace + rectHeight - 5);
                g.drawString(String.valueOf(i),freeSpace + i + ptsSize,freeSpace + rectHeight + 15);
            }
            for(int i = 0; i < gr.getYLength(); i += 30){
                g.drawLine(freeSpace - 5,freeSpace + rectHeight - i - ptsSize ,
                        freeSpace + 5, freeSpace + rectHeight - i - ptsSize);
                g.drawString(String.valueOf(i),freeSpace - 30, freeSpace + rectHeight - i - ptsSize);
            }
        }

        private void printPoints(Graphics g, List<? extends Point> pts, int size) {
            if (pts == null) return;
            for(Point pt : pts){
                g.setColor(pt.getColor());
                g.fillOval(freeSpace + pt.getX(), gr.getYLength() + freeSpace - pt.getY(), size, size);
            }
        }
    }

    private JGridPanel  grp;
    private JFrame      frame;
    private JScrollPane jsp;
    private JTextPane   textPane;
    private JButton     btnAddPoint;
    private JButton     btnChangeDist;
    private int         leftPartWidth  = 210;
    private int         jspHeight      = 300;
    private int         textPaneHeight = 80;
    private int         buttonsHeight  = 50;
    private int         genId          = 0;
    List<Generator>     generators;

    public Visualizer(int xLength, int yLength, List<Generator> aGenerators) {
        if(aGenerators.isEmpty() || xLength < 1 || yLength < 1)
            throw new IllegalArgumentException("Illegal argument");

        generators = aGenerators;

        frame = new JFrame("KNN");
        frame.setLayout(null);

        grp = new JGridPanel(new Grid(xLength, yLength, generators.get(genId)));
        int panelWidth  = grp.gr.getXLength() + grp.ptsSize + 2 * grp.freeSpace;
        int panelHeight = grp.gr.getYLength() + grp.ptsSize + 2 * grp.freeSpace;
        grp.setBounds(0, 0, panelWidth, panelHeight);

        jsp = new JScrollPane();
        updateScrollArea();
        jsp.setBounds(panelWidth, 0, leftPartWidth, jspHeight);

        textPane = new JTextPane();
        updateTextPanel();
        textPane.setBounds(panelWidth, jspHeight, leftPartWidth, textPaneHeight);

        btnAddPoint = new JButton("Добавить точку");
        btnAddPoint.setBounds(panelWidth, jspHeight + textPaneHeight , leftPartWidth, buttonsHeight);
        btnAddPoint.addActionListener(e ->
                                 {
                                     Random r = new Random();
                                     grp.gr.addPoint((int)(r.nextDouble() * grp.gr.getXLength()),
                                                     (int)(r.nextDouble() * grp.gr.getYLength()));
                                     updateScrollArea();
                                     grp.repaint();
                                     updateTextPanel();
                                 });

        btnChangeDist = new JButton("Сменить распределение");
        btnChangeDist.setBounds(panelWidth, jspHeight + textPaneHeight + buttonsHeight,
                              leftPartWidth, buttonsHeight);
        btnChangeDist.addActionListener(e ->
                                        {
                                            genId = genId + 1 < generators.size() ? genId + 1 : 0;
                                            grp.gr.reGenerate(generators.get(genId));
                                            grp.repaint();
                                            updateTextPanel();
                                            updateScrollArea();
                                        });
        frame.add(grp);
        frame.add(jsp);
        frame.add(textPane);
        frame.add(btnAddPoint);
        frame.add(btnChangeDist);

        int totalWidth  = panelWidth + leftPartWidth + 20;
        int totalHeight = Math.max( panelHeight,
                                    jspHeight + textPaneHeight + 2 * buttonsHeight + 40);
        frame.setSize(totalWidth, totalHeight);
        frame.setResizable(false);
    }

    private void updateTextPanel(){
        textPane.setText(String.format("""
                         Нынешнее распределение: %s
                         Количество точек: %d
                         """, generators.get(genId).getName(),
                              grp.gr.getGeneratedPoints().size() +
                                      grp.gr.getAPoints().size()));
    }

    private void updateScrollArea() {

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        for(AddedPoint aPoint : grp.gr.getAPoints()) {
            JTextPane jtp = new JTextPane();
            jtp.setEditable(false);
            StyledDocument doc = jtp.getStyledDocument();

            Style bigTextSt = createStyle(jtp, null, "BTS",Color.LIGHT_GRAY, 16);
            StyleConstants.setFontFamily(bigTextSt, "Arial Black");
            StyleConstants.setLineSpacing(bigTextSt, 0.1f);
            doc.setParagraphAttributes(0, doc.getLength(), bigTextSt, false);

            Style smallTextSt = createStyle(jtp, bigTextSt, "STS",Color.WHITE, 14);
            Style bigColorSt =  createStyle(jtp, bigTextSt, "BCS",aPoint.getColor(), 16);

            if (insertPointData(doc, aPoint, bigColorSt, bigTextSt) == -1) continue;

            for (Point nghbrPoint : aPoint.getSurrounding()) {
                Style smallColorSt =  createStyle(jtp, bigTextSt, "SCS",nghbrPoint.getColor(), 14);
                insertPointData(doc, nghbrPoint, smallColorSt, smallTextSt);
            }

            mainPanel.add(jtp);
        }
        jsp.setViewportView(mainPanel);
    }

    private int insertPointData(StyledDocument doc, Point p, Style clrStyle, Style textStyle) {
        try {
            doc.insertString(doc.getLength(), "   ", clrStyle);
            doc.insertString(doc.getLength(),
                             String.format(" Point at (%d,%d) \n",p.getX(),p.getY()),
                             textStyle);
        } catch (BadLocationException e) {
            System.err.println("Failed to print point " + e.getMessage());
            return -1;
        }
        return 0;
    }

    private Style createStyle(JTextPane jtp,   Style parentSt, String name,
                              Color              BGClr, int   fontSize ) {
        Style st = jtp.addStyle(name, parentSt);
        StyleConstants.setBackground(st, BGClr);
        StyleConstants.setFontSize(st, fontSize);
        return st;
    }

    public void visualize() {
        SwingUtilities.invokeLater(() -> {
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
