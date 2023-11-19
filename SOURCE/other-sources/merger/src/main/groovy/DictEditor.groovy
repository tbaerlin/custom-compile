/*
* DictEditor.java
*
* Created on 08.05.12 10:15
*
* Copyright (c) market maker Software AG. All Rights Reserved.
*/
package de.marketmaker.istar.merger.web.easytrade.block.definition;


import de.marketmaker.istar.common.util.LocalConfigProvider
import groovy.swing.SwingBuilder

import java.awt.BorderLayout
import java.awt.Font
import java.awt.Component
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JSplitPane
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode
import javax.swing.JComboBox
import javax.swing.BorderFactory

class DictEditor {
  def swing = new SwingBuilder()

  def file = new File(LocalConfigProvider.getIstarSrcDir(), "/merger/src/conf/dm-doc/dict.html")

  def bakFile = new File(file.getParentFile(), "dict.html.bak")

  FilteredTreeModel treeModel = new FilteredTreeModel(new DefaultMutableTreeNode("root"))

  JTree typesTree

  JTextArea editor

  JTextField filterText

  JLabel htmlArea

  JLabel status

  JComboBox clazzBox

  groovy.util.Node root;

  boolean inTreeSelectionChange

  TreeNodeWithXmlNode current

  def save() {
    if (bakFile.exists()) {
      bakFile.delete()
    }
    status.text = "Saving..."
    file.renameTo(bakFile)
    file.withPrintWriter { pw ->
      pw.println('<?xml version="1.0" encoding="UTF-8"?>')
      def printer = new XmlNodePrinter(pw)
      printer.print(root)
    }
    status.text = "Saved"
  }

  def treeSelectionChanged(event) {
    updateText() // tree selection might be fired _before_ focusLost of textArea!
    inTreeSelectionChange = true
    TreeNode treeNode = typesTree.lastSelectedPathComponent
    if (treeNode instanceof TreeNodeWithXmlNode) {
      current = treeNode as TreeNodeWithXmlNode
      editor.enable(true)
      changeText(current.xml.text())
      clazzBox.setSelectedItem(current.xml.@class)
      clazzBox.enable(true)
    }
    else {
      editor.setText("")
      editor.enable(false)
      htmlArea.setText("")
      clazzBox.enable(false)
      current = null
    }
    inTreeSelectionChange = false
  }

  def changeText(s) {
    editor.text = s
    refreshHtml()
  }

  def refreshHtml() {
    htmlArea.text = "<html>" + editor.text + "</html>"
  }

  def filter() {
    this.treeModel.setFilter(filterText.text.trim())
    this.treeModel.reload()
  }

  def updateClass() {
    if (inTreeSelectionChange) return
    TreeNode treeNode = typesTree.lastSelectedPathComponent
    if (treeNode instanceof TreeNodeWithXmlNode) {
      def node = treeNode as TreeNodeWithXmlNode
      node.xml.@class = clazzBox.getSelectedItem()
      this.treeModel.nodeChanged(node)
    }
  }

  def undo() {
    if (current != null && current.originalText != null) {
      changeText(current.originalText)
    }
  }

  def updateText() {
    if (current != null) {
      current.updateText(editor.getText())
    }
  }

  def types = []

  def init() {
    root = new XmlParser().parse(file)

    createTreeModel(root)

    swing.frame(title: 'DictEditor', defaultCloseOperation: JFrame.DISPOSE_ON_CLOSE,
            size: [1000, 800], show: true, locationRelativeTo: null) {
      lookAndFeel("system")
      menuBar() {
        menu(text: "File", mnemonic: 'F') {
          menuItem(text: "Save", mnemonic: 'S', actionPerformed: {save() })
          menuItem(text: "Exit", mnemonic: 'X', actionPerformed: {dispose() })
        }
        menu(text: "Edit", mnemonic: 'E') {
          menuItem(text: "Undo", mnemonic: 'U', actionPerformed: {undo() })
        }
      }
      vbox {
        splitPane {
          panel(constraints: "left", layout: new BorderLayout()) {
            hbox(constraints: BorderLayout.NORTH) {
              label(text: " Filter ", mnemonic: 'l', labelFor: filterText)
              filterText = textField(text: "", actionPerformed: {filter()})
            }
            scrollPane(constraints: BorderLayout.CENTER, preferredSize: [160, -1]) {
              typesTree = tree(rootVisible: false, largeModel: true, model: treeModel,
                      cellRenderer: new ColoredTreeCellRenderer(),
                      valueChanged: { event -> treeSelectionChanged(event) })
            }
          }
          splitPane(orientation: JSplitPane.VERTICAL_SPLIT, dividerLocation: 280) {
            scrollPane(constraints: "top") {
              vbox {
                editor = textArea(enabled: false, focusLost: {updateText()})
                hbox {
                  label(text: " Class ")
                  clazzBox = comboBox(items: ['def', 'def todo', 'def ref'],
                          actionPerformed: { updateClass() })
                  hglue()
                  button(text: "Refresh HTML", actionPerformed: {refreshHtml()})
                }
              }
            }
            scrollPane(constraints: "bottom") {
              htmlArea = label(border: BorderFactory.createEmptyBorder(5, 5, 5, 5))
            }
          }
        }
        status = label(text: "Hi!")
      }
    }
  }

  private void createTreeModel(Node root) {
    def defGroups = root.body.div.findAll { it.@class == 'def-group'}
    def typeDescs = defGroups.find() { it.@id == 'typeDesc' }
    def attrDescs = defGroups.find() { it.@id == 'attrDesc' }
    def fieldDescs = defGroups.find() { it.@id == 'fieldDesc' }
    def fields = root.body.div.find() { it.@id == 'fields' }

    def typeDescsNode = new DefaultMutableTreeNode("Type Descriptions", true)
    treeModel.root.add(typeDescsNode)

    typeDescs.children()/*.take(50)*/.each {
      typeDesc ->
      def node = new TreeNodeWithXmlNode(typeDesc)
      typeDescsNode.add(node)

      def typeName = node.getUserObject()

      def attrDesc = attrDescs.children().findAll() { it.@id =~ "${typeName}\\.\\.\\w+"}
      attrDesc.each {
        def attrNode = new TreeNodeWithXmlNode(it)
        node.add(attrNode)
      }

      def fieldDesc = fieldDescs.children().findAll() { it.@id =~ "${typeName}\\.\\w+"}
      fieldDesc.each {
        node.add(new TreeNodeWithXmlNode(it))
      }
    }

    def fieldsNode = new DefaultMutableTreeNode("Fields", true)
    treeModel.root.add(fieldsNode)

    fields.children().each {
      fieldsNode.add(new TreeNodeWithXmlNode(it))
    }
  }

  static main(args) {
    new DictEditor().init()
  }
}

// ========= H E L P E R   C L A S S E S =================

class TreeNodeWithXmlNode extends DefaultMutableTreeNode {
  groovy.util.Node xml

  String originalText

  def TreeNodeWithXmlNode(groovy.util.Node xml) {
    super(xml.@id)
    this.xml = xml
  }

  void updateText(String text) {
    if (this.originalText == null) {
      originalText = xml.text()
    }
    xml.setValue(text)
  }
}

class FilteredTreeModel extends DefaultTreeModel {
  private String filter = ""

  private Closure nodeFilter = {DefaultMutableTreeNode node ->
    return filter.length() == 0 ||
            (node.getUserObject() as String).contains(filter) ||
            childList(node).any(nodeFilter)
  }

  private Closure<List> findChildren = { String filter, TreeNode parent ->
    // use filter as arg to ensure memoize works as intended
    parent.children().toList().findAll(nodeFilter) as List
  }.memoize()

  public FilteredTreeModel(TreeNode root) {
    super(root, false);
  }

  void setFilter(String filter) {
    this.filter = filter;
  }

  @Override
  public void nodeChanged(TreeNode node) {
    if (listenerList != null && node != null) {
      TreeNode parent = node.getParent();
      if (parent != null) {
//        int anIndex = parent.getIndex(node); <- this is why we have to override this method
        int anIndex = getIndexOfChild(parent, node);
        if (anIndex != -1) {
          nodesChanged(parent, [anIndex] as int[]);
        }
      }
      else if (node == getRoot()) {
        nodesChanged(node, null);
      }
    }
  }

  private List childList(parent) {
    findChildren(this.filter, parent)
  }

  @Override
  int getIndexOfChild(Object parent, Object child) {
    childList(parent).indexOf(child)
  }

  @Override
  int getChildCount(Object parent) {
    childList(parent).size()
  }

  @Override
  Object getChild(Object parent, int index) {
    childList(parent)[index]
  }
}

class ColoredTreeCellRenderer extends DefaultTreeCellRenderer {
  def colors = ["def todo": java.awt.Color.RED,
    "def todo obsolete": java.awt.Color.GRAY,
    "def ref": java.awt.Color.BLUE,
  ]
  @Override
  Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)
    if (value instanceof TreeNodeWithXmlNode) {
      def color = colors.get(value.xml.@class);
      if (color != null) {
        setForeground(color)
      }
    }
    return this
  }
}
