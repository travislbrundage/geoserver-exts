package org.geotools.ysld.encode;

import java.util.Map;

import org.geotools.styling.Symbolizer;
import org.geotools.ysld.Ysld;

public abstract class SymbolizerEncoder<S extends Symbolizer> extends YsldEncodeHandler<S> {

    SymbolizerEncoder(S sym) {
        super(sym);
    }

    @Override
    protected void encode(S sym) {
        put("geometry", sym.getGeometry());
        put("uom", sym.getUnitOfMeasure());
        if (!sym.getOptions().isEmpty()) {
            for (Map.Entry<String,String> kv : sym.getOptions().entrySet()) {
                String option = Ysld.OPTION_PREFIX + kv.getKey();
                String text = kv.getValue();
                put(option, toObjOrNull(text));
            }
        }
    }
}
