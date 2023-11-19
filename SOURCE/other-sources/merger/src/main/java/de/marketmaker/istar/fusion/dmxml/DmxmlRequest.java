/*
 * DmxmlRequest.java
 *
 * Created on 06.12.13 10:57
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.fusion.dmxml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.marketmaker.istar.merger.web.GsonUtil;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;
import de.marketmaker.istar.merger.web.easytrade.RequestParserMethod;

/**
 * Represents a dmxml request that can be submitted to a
 * {@link de.marketmaker.istar.fusion.dmxml.DmxmlFacade} for evaluation.
 * <p>Usage:
 * <pre>
 *  DmxmlRequest.Builder b = DmxmlRequest.createBuilder();
 *  DmxmlRequest.Builder.Block&lt;MSCPriceData> b1 = b.addBlock("MSC_PriceData")
 *                                   .withParameter("symbol", "DE0007100000");
 *  DmxmlRequest r = b.build();
 *
 *  if (facade.evaluate(r)) {
 *      if (r.getResult() != null) {
 *          MSCPriceData pd = b1.getResult();
 *          ...
 *      }
 *  }
 * </pre>
 *
 * @author oflege
 */
public class DmxmlRequest {

    public static class Builder {
        private List<Block> blocks = new ArrayList<>();

        private String path;

        public class Block<V extends BlockType> {
            private final String id;

            private final String name;

            private final Map<String, String[]> parameters = new HashMap<>();

            private String dependsOnId;

            private V result;

            private ErrorType error;

            void setResult(V result) {
                this.result = result;
            }

            void setError(ErrorType result) {
                this.error = result;
            }

            private Block(String id, String name) {
                this(id, name, null);
            }

            private Block(String id, String name, Class<V> clazz) {
                this.id = id;
                this.name = name;
            }

            public V getResult() {
                return result;
            }

            public ErrorType getError() {
                return error;
            }

            public Block<V> withParameter(String key, String... value) {
                return with(key, value);
            }

            public Block<V> with(String key, String... value) {
                this.parameters.put(key, value);
                return this;
            }

            public Block<V> withDependsOn(Block b) {
                this.dependsOnId = b.id;
                return this;
            }
        }

        private final MoleculeRequest r = new MoleculeRequest();

        public Builder() {
        }

        public Builder withAuth(String authentication, String authenticationType) {
            r.setAuthentication(authentication);
            r.setAuthenticationType(authenticationType);
            return this;
        }

        public Builder withKey(String key) {
            r.setKey(key);
            return this;
        }

        public Builder withLocale(String locale) {
            r.setLocales(RequestParserMethod.parseLocales(locale));
            return this;
        }

        public Builder withPath(String path) {
            this.path = path;
            return this;
        }

        public <V extends BlockType> Block<V> addBlock(String id, String name) {
            Block<V> result = new Block<>(id, name);
            this.blocks.add(result);
            return result;
        }

        public <V extends BlockType> Block<V> addBlock(String name) {
            return addBlock("_" + this.blocks.size(), name);
        }

        public <V extends BlockType> Block<V> addBlock(String id, String name, Class<V> clazz) {
            Block<V> result = new Block<>(id, name, clazz);
            this.blocks.add(result);
            return result;
        }

        public <V extends BlockType> Block<V> addBlock(String name, Class<V> clazz) {
            return addBlock("_" + this.blocks.size(), name, clazz);
        }

        public DmxmlRequest build() {
            for (Block block : blocks) {
                r.addAtom(block.id, block.name, block.parameters, block.dependsOnId);
            }
            return new DmxmlRequest(this.path, this.r, this.blocks);
        }
    }

    private final String path;

    private final MoleculeRequest request;

    private final List<Builder.Block> blocks;

    private Exception exception;

    private ResponseType response;

    private byte[] dmxmlResponse;

    public static Builder createBuilder() {
        return new Builder();
    }

    private DmxmlRequest(String path, MoleculeRequest request, List<Builder.Block> blocks) {
        this.path = path;
        this.request = request;
        this.blocks = blocks;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        if (this.response != null) {
            return this.response.getHeader().getGenerated();
        }
        return GsonUtil.toJson(this.request);
    }

    void setResult(Exception exception) {
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }

    void setDmxmlResponse(byte[] dmxmlResponse) {
        this.dmxmlResponse = dmxmlResponse;
    }

    public byte[] getDmxmlResponse() {
        return dmxmlResponse;
    }

    public ResponseType getResponse() {
        return this.response;
    }

    void setResult(ResponseType result) {
        for (BlockOrError boe : result.getData().getBlockOrError()) {
            if (boe instanceof BlockType) {
                assignResult((BlockType) boe);
            }
            else {
                assignError((ErrorType) boe);
            }
        }
        this.response = result;
    }

    MoleculeRequest getMoleculeRequest() {
        return this.request;
    }

    private void assignError(ErrorType et) {
        findBlock(et.getCorrelationId()).setError(et);
    }

    private void assignResult(BlockType boe) {
        //noinspection unchecked
        findBlock(boe.getCorrelationId()).setResult(boe);
    }

    private Builder.Block findBlock(String id) {
        for (Builder.Block block : this.blocks) {
            if (id.equals(block.id)) {
                return block;
            }
        }
        throw new IllegalStateException("unknown id in result: '" + id + "'");
    }
}
