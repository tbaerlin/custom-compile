package de.marketmaker.itools.gwtutil.client.vml.shape.path;

/**
 * User: umaurer
 * Date: 15.11.13
 * Time: 12:13
 */
public abstract class PathStep {

    public abstract String getVmlString();

    @Override
    public String toString() {
        return getVmlString();
    }
}