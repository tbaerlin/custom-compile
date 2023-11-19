package de.marketmaker.istar.ratios.frontend;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import de.marketmaker.istar.common.request.AbstractIstarResponse;

public class MatrixMetadataRatioSearchResponse extends AbstractIstarResponse implements
        RatioSearchResponse {

    private final Node root = new Node();

    public static class GroupValueKey implements Serializable {
        static final long serialVersionUID = 1L;

        private final String group;

        private final String value;

        private String translation;

        public GroupValueKey(String groupName, String groupValue) {
            this.group = groupName;
            this.value = groupValue;
        }

        public void withTranslation(String translation) {
            this.translation = translation;
        }

        public String getGroup() {
            return group;
        }

        public String getValue() {
            return value;
        }

        public String getTranslation() {
            if (this.translation == null) {
                return this.group;
            }
            return this.translation;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GroupValueKey that = (GroupValueKey) o;
            if (group != null ? !group.equals(that.group) : that.group != null) return false;
            if (value != null ? !value.equals(that.value) : that.value != null) return false;
            return true;
        }

        @Override
        public int hashCode() {
            int result = group != null ? group.hashCode() : 0;
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }

        public String toString() {
            return group + " [" + value + "]";
        }
    }

    public static class Node implements Serializable {
        static final long serialVersionUID = 1L;

        private Map<String, Integer> counts;

        private Map<GroupValueKey, Node> children;

        public Node() {
        }

        public String toString() {
            if (this.counts != null) {
                return "Node{counts=" + this.counts + "}";
            }
            else {
                return "Node{children=" + this.children + "}";
            }
        }

        /**
         * side effect alert:
         * this method creates a child if non exists so far for the specified key!
         *
         * @param key
         * @return a child for the key
         */
        public Node getChild(GroupValueKey key) {
            assert this.counts == null;
            if (children == null) {
                children = new HashMap<>();
            }
            Node result = children.get(key);
            if (result == null) {
                children.put(key, result = new Node());
            }
            return result;
        }

        public void addCount(String value) {
            assert this.children == null;
            if (this.counts == null) {
                this.counts = new HashMap<>();
            }
            this.counts.put(value, getCount(value) + 1);
        }

        public Map<GroupValueKey, Node> getChildren() {
            return children;
        }

        public Map<String, Integer> getCounts() {
            return counts;
        }

        public int getCount(String value) {
            assert this.counts != null;
            final Integer count = this.counts.get(value);
            return (count != null) ? count : 0;
        }

        public int getChildCount() {
            return isLeaf() ? counts.size() : (children != null ? children.size() : 0);
        }

        public boolean isLeaf() {
            return this.counts != null;
        }

        // recursive merge operation
        public void merge(Node that) {
            if (isLeaf() && that.isLeaf()) {
                merge(that.counts);
            }
            else if ((that.children != null) && (that.children.size() > 0)) {
                mergeChildren(that);
            }
        }

        private void mergeChildren(Node that) {
            if (children == null) {
                children = new HashMap<>();
            }
            for (Map.Entry<GroupValueKey, Node> child : that.children.entrySet()) {
                if (this.children.containsKey(child.getKey())) {
                    this.children.get(child.getKey()).merge(child.getValue());
                }
                else {
                    this.children.put(child.getKey(), child.getValue());
                }
            }
        }

        void merge(Map<String, Integer> counts) {
            for (Map.Entry<String, Integer> elem : counts.entrySet()) {
                this.counts.put(elem.getKey(), getCount(elem.getKey()) + elem.getValue());
            }
        }

        // ---  the current state as tree, this is for for debugging only
        void appendAsTree(String level, GroupValueKey key, StringBuilder builder) {
            builder.append(level);
            builder.append(key + ": " + getChildCount() + "\n");
            if (this.children != null) {
                for (Map.Entry<GroupValueKey, Node> entry : children.entrySet()) {
                    entry.getValue().appendAsTree(level + "--", entry.getKey(), builder);
                }
            }
            // display the counts if available
            if (counts != null) {
                for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                    builder.append(level + "--::");
                    builder.append(entry.getKey());
                    builder.append(":");
                    builder.append(entry.getValue() + "\n");
                }
            }
        }

        // ---  the current state as matrix, this is for for debugging only
        void appendAsMatrix(String head, StringBuilder builder) {
            if (children == null) {
                builder.append(head + "\n");
            }
            else {
                for (Map.Entry<GroupValueKey, Node> entry : children.entrySet()) {
                    entry.getValue().appendAsMatrix(
                            head + entry.getKey() + ":" + entry.getValue().getChildCount() + "|", builder);
                }
            }
        }
    }

    public void merge(MatrixMetadataRatioSearchResponse response) {
        doMerge(response.root);
    }

    // this is also called by the visitor to add new values
    void doMerge(Node tree) {
        root.merge(tree);
    }

    public Node getResult() {
        return root;
    }

    protected void appendToString(StringBuilder sb) {
        sb.append(", ").append(this.root);
    }

}
