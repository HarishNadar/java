import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class IncomeTaxCalculator {
    private JFrame frame;
    private JTextField incomeField;
    private JTextField taxField;
    private JButton calculateButton;
    private JTextField nameField;
    private JTextField occupationField;
    private JTextField cityField;

    public IncomeTaxCalculator() {
        createGUI();
    }

    private void createGUI() {
        frame = new JFrame("Income Tax Calculator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        JLabel nameLabel = new JLabel("Enter your name:");
        nameField = new JTextField(10);

        JLabel occupationLabel = new JLabel("Enter your occupation:");
        occupationField = new JTextField(10);

        JLabel cityLabel = new JLabel("Enter your city:");
        cityField = new JTextField(10);

        JLabel incomeLabel = new JLabel("Enter your income (in lakhs):");
        incomeField = new JTextField(10);

        JLabel taxLabel = new JLabel("Your income tax:");
        taxField = new JTextField(10);
        taxField.setEditable(false);

        calculateButton = new JButton("Calculate");
        calculateButton.addActionListener(new CalculateButtonListener());

        frame.add(nameLabel);
        frame.add(nameField);
        frame.add(occupationLabel);
        frame.add(occupationField);
        frame.add(cityLabel);
        frame.add(cityField);
        frame.add(incomeLabel);
        frame.add(incomeField);
        frame.add(taxLabel);
        frame.add(taxField);
        frame.add(calculateButton);

        frame.pack();
        frame.setVisible(true);
    }

    private class CalculateButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String name = nameField.getText();
            String occupation = occupationField.getText();
            String city = cityField.getText();
            double income = Double.parseDouble(incomeField.getText()) * 100000;
            double tax = calculateTax(income);
            taxField.setText(String.format("%.2f", tax));

            // Save data to database
            try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/income_tax", "username", "password")) {
                PreparedStatement pstmt = con.prepareStatement("INSERT INTO income_tax_data (name, occupation, city, income, tax) VALUES (?, ?, ?, ?, ?)");
                pstmt.setString(1, name);
                pstmt.setString(2, occupation);
                pstmt.setString(3, city);
                pstmt.setDouble(4, income);
                pstmt.setDouble(5, tax);
                pstmt.executeUpdate();
            } catch (SQLException ex) {
                System.err.println("Error saving data to database: " + ex.getMessage());
            }

            // Save data to .cxt file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("income_tax_data.cxt", true))) {
                writer.write("Name: " + name + ", Occupation: " + occupation + ", City: " + city + ", Income: " + income + ", Tax: " + tax + "\n");
            } catch (IOException ex) {
                System.err.println("Error saving data to .cxt file: " + ex.getMessage());
            }
        }
    }

    private double calculateTax(double income) {
        if (income <= 250000) {
            return 0;
        } else if (income <= 500000) {
            return (income - 250000) * 0.05;
        } else if (income <= 750000) {
            return (income - 500000) * 0.10 + 12500;
        } else if (income <= 1000000) {
            return (income - 750000) * 0.15 + 37500;
        } else if (income <= 1250000) {
            return (income - 1000000) * 0.20 + 75000;
        } else if (income <= 1500000) {
            return (income - 1250000) * 0.25 + 125000;
        } else {
            return (income - 1500000) * 0.30 + 187500;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new IncomeTaxCalculator();
            }
        });
    }
}