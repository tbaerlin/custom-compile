package de.infront;

import java.awt.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;

public class DependencyConverter extends Frame implements ActionListener {
    private TextField gradleDependencyField;
    private TextArea mavenDependencyTextArea;

    public DependencyConverter() {
        // Create the frame
        setTitle("Dependency Converter");
        setSize(400, 300);
        setLayout(new FlowLayout());

        // Create the Gradle dependency text field
        Label gradleLabel = new Label("Gradle Dependency:");
        gradleDependencyField = new TextField(30);
        add(gradleLabel);
        add(gradleDependencyField);

        // Create the Maven dependency text area
        Label mavenLabel = new Label("Maven Dependency:");
        mavenDependencyTextArea = new TextArea(10, 30);
        add(mavenLabel);
        add(mavenDependencyTextArea);

        // Create the Convert button
        Button convertButton = new Button("Convert");
        convertButton.addActionListener(this);
        add(convertButton);

        // Handle window close event
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Convert")) {
            // Get the text from the Gradle dependency field
            String gradleDependencyText = gradleDependencyField.getText();

            // Convert the Gradle dependency text to Maven POM XML format
            String mavenDependencyText = convertToMavenDependency(gradleDependencyText);

            // Display the Maven dependency text in the text area
            mavenDependencyTextArea.setText(mavenDependencyText);

            // Copy the Maven dependency text to the clipboard
            copyToClipboard(mavenDependencyText);
        }
    }

    private String convertToMavenDependency(String gradleDependencyText) {
        // Split the Gradle dependency into its components
        String[] parts = gradleDependencyText.split(":");

        if (parts.length != 3) {
            return "Invalid Gradle Dependency Format";
        }

        // Extract the components
        String groupId = parts[0].trim();
        String artifactId = parts[1].trim();
        String version = parts[2].trim();

        // Create the Maven POM XML format
        StringBuilder mavenDependency = new StringBuilder();
        mavenDependency.append("<dependency>\n");
        mavenDependency.append("    <groupId>").append(groupId).append("</groupId>\n");
        mavenDependency.append("    <artifactId>").append(artifactId).append("</artifactId>\n");
        mavenDependency.append("    <version>").append(version).append("</version>\n");
        mavenDependency.append("</dependency>");

        return mavenDependency.toString();
    }

    private void copyToClipboard(String text) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection(text);
        clipboard.setContents(selection, null);
    }

    public static void main(String[] args) {
        DependencyConverter converter = new DependencyConverter();
        converter.setVisible(true);
    }
}








