package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.core.client.GWT;
import de.marketmaker.itools.gwtutil.client.util.Firebug;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: umaurer
 * Created: 16.03.15
 */
public class IconImageMapping {
    public static final IconImageMapping I = GWT.create(IconImageMapping.class);

    private final Map<String, ImageSpec> map = new HashMap<>();
    private final String urlPrefix;

    @SuppressWarnings("unused")
    public IconImageMapping() {
        this("");
    }

    protected IconImageMapping(String urlPrefix) {
        this.urlPrefix = urlPrefix;
        initializeMapping();
    }

    protected void initializeMapping() {
        // initialization happens in subclass in other module
    }

    protected void add(String name, ImageSpec imageSpec) {
        if (this.map.containsKey(name)) {
            Firebug.warn("IconImageMapping <add> icon name already assigned: " + name);
        }
        this.map.put(name, imageSpec);
    }

    protected ImageSpec add(String name, String url, int x, int y, int width, int height) {
        final ImageSpec imageSpec = new ImageSpec(name, this.urlPrefix + url, x, y, width, height);
        add(name, imageSpec);
        return imageSpec;
    }

    protected ImageSpec add(String name, String url, int width, int height) {
        return add(name, url, 0, 0, width, height);
    }

    protected void addSynonyms(String name, String... synonyms) {
        final ImageSpec imageSpec = this.map.get(name);
        if (imageSpec == null) {
            throw new IllegalArgumentException("synonym source not found: " + name);
        }
        for (String synonym : synonyms) {
            add(synonym, imageSpec);
        }
    }

    public boolean hasImageSpec(String name) {
        return this.map.containsKey(name);
    }

    public ImageSpec getImageSpec(String name) {
        final ImageSpec imageSpec = this.map.get(name);
        if (imageSpec == null) {
            throw new IllegalArgumentException("IconImageMapping - unknown icon name: " + name);

        }
        return imageSpec;
    }

    public class Sprite {
        private final String url;
        int width = -1;
        int height = -1;
        int xBase = 0;
        int yBase = 0;
        int colCount = -1;

        public Sprite(String url) {
            this.url = url;
        }

        public Sprite size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Sprite size(int widthAndHeight) {
            this.width = widthAndHeight;
            this.height = widthAndHeight;
            return this;
        }

        public Sprite xBase(int xBase) {
            this.xBase = xBase;
            return this;
        }

        public Sprite yBase(int yBase) {
            this.yBase = yBase;
            return this;
        }

        public int yBase() {
            return this.yBase;
        }

        public Sprite incYBase() {
            this.yBase += this.height;
            return this;
        }

        public Sprite addYBase(int yBaseDiff) {
            this.yBase += yBaseDiff;
            return this;
        }

        public Sprite colCount(int colCount) {
            this.colCount = colCount;
            return this;
        }

        public Sprite icons(String... names) {
            this.yBase += addIcons(names);
            return this;
        }

        public Sprite iconsKeepY(String... names) {
            addIcons(names);
            return this;
        }

        private int addIcons(String[] names) {
            if (this.width == -1 || this.height == -1) {
                throw new IllegalStateException("size is not initialized");
            }
            int row = 0;
            int column = -1;
            for (String name : names) {
                column++;
                if (this.colCount > 0 && column == this.colCount) {
                    row++;
                    column = 0;
                }
                if (name != null) {
                    add(name, this.url, column * this.width + this.xBase, row * this.height + this.yBase, this.width, this.height);
                }
            }
            return (row + 1) * this.height;
        }
    }

    public Sprite sprite(String url) {
        return new Sprite(url);
    }
}
