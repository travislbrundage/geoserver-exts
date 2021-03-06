package org.geotools.ysld.encode;


import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer2;
import org.geotools.styling.UserLayer;
import org.geotools.ysld.YamlMap;
import org.geotools.ysld.YamlSeq;
import org.geotools.ysld.Ysld;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.style.GraphicalSymbol;
import org.opengis.style.RasterSymbolizer;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class YsldEncodeTest {

    @Test
    public void testFunction() throws Exception {
         StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
        FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory();

        StyledLayerDescriptor sld = styleFactory.createStyledLayerDescriptor();

        UserLayer layer = styleFactory.createUserLayer();
        sld.layers().add(layer);

        Style style = styleFactory.createStyle();
        layer.userStyles().add(style);

        Rule rule = styleFactory.createRule();

        style.featureTypeStyles().add(styleFactory.createFeatureTypeStyle());
        style.featureTypeStyles().get(0).rules().add(rule);

        Function func =
            filterFactory.function("strEndsWith", filterFactory.property("foo"), filterFactory.literal("bar"));
        rule.setFilter(filterFactory.equal(func, filterFactory.literal(true), false));

        StringWriter out = new StringWriter();
        Ysld.encode(sld, out);

        YamlMap obj = new YamlMap(new Yaml().load(out.toString()));
        String filter = obj.seq("feature-styles").map(0).seq("rules").map(0).str("filter");
        assertEquals("strEndsWith(foo,'bar') = 'true'", filter);
    }

    @Test
    public void testRenderingTransformation() throws IOException {

        StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
        FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory();

        StyledLayerDescriptor sld = styleFactory.createStyledLayerDescriptor();

        UserLayer layer = styleFactory.createUserLayer();
        sld.layers().add(layer);

        Style style = styleFactory.createStyle();
        layer.userStyles().add(style);

        FeatureTypeStyle featureStyle = styleFactory.createFeatureTypeStyle();
        style.featureTypeStyles().add(featureStyle);

        Function p1 = filterFactory.function("parameter", filterFactory.literal("data"));
        Function p2 = filterFactory.function("parameter", filterFactory.literal("levels"), filterFactory.literal(1000),
            filterFactory.literal(1100), filterFactory.literal(1200));

        Function rt = filterFactory.function("ras:Contour", p1, p2);
        featureStyle.setTransformation(rt);

        StringWriter out = new StringWriter();
        Ysld.encode(sld, out);

        YamlMap obj = new YamlMap(new Yaml().load(out.toString()));
        YamlMap tx = obj.seq("feature-styles").map(0).map("transform");
        assertEquals("ras:Contour", tx.get("name"));
        YamlSeq levels = tx.map("params").seq("levels");
        assertEquals("1000", levels.str(0));
        assertEquals("1100", levels.str(1));
        assertEquals("1200", levels.str(2));
    }

    @Test
    public void testRenderingTransformationHeatmap() throws IOException {
        StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
        FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory();

        StyledLayerDescriptor sld = styleFactory.createStyledLayerDescriptor();

        UserLayer layer = styleFactory.createUserLayer();
        sld.layers().add(layer);

        Style style = styleFactory.createStyle();
        layer.userStyles().add(style);

        FeatureTypeStyle featureStyle = styleFactory.createFeatureTypeStyle();
        style.featureTypeStyles().add(featureStyle);

        Function p1 = filterFactory.function("parameter", filterFactory.literal("data"));
        Function p2 = filterFactory.function("parameter",
            filterFactory.literal("weightAttr"), filterFactory.literal("pop2000"));
        Function p3 = filterFactory.function("parameter",
                filterFactory.literal("radius"), filterFactory.literal("100"));
        Function p4 = filterFactory.function("parameter",
                filterFactory.literal("pixelsPerCell"), filterFactory.literal(10));

        Function rt = filterFactory.function("vec:Heatmap", p1, p2, p3, p4);
        featureStyle.setTransformation(rt);

        Rule rule = styleFactory.createRule();
        rule.setName("Za'Ha'Dum");
        featureStyle.rules().add(rule);
        
        StringWriter out = new StringWriter();
        Ysld.encode(sld, out);

        YamlMap obj = new YamlMap(new Yaml().load(out.toString()));
        YamlMap tx = obj.seq("feature-styles").map(0).map("transform");
        assertEquals("vec:Heatmap", tx.get("name"));

        YamlMap params = tx.map("params");
        assertEquals("pop2000", params.get("weightAttr"));
        assertEquals("100", params.str("radius"));
        assertEquals("10", params.str("pixelsPerCell"));
        
        YamlMap ruleMap = obj.seq("feature-styles").map(0).seq("rules").map(0);
        assertThat(ruleMap.str("name"), equalTo("Za'Ha'Dum"));
    }
    
    @Test
    public void testLabelShield() throws IOException {
        StyleFactory sf = CommonFactoryFinder.getStyleFactory();
        FilterFactory ff = CommonFactoryFinder.getFilterFactory();

        StyledLayerDescriptor sld = sf.createStyledLayerDescriptor();

        UserLayer layer = sf.createUserLayer();
        sld.layers().add(layer);
        Style style = sf.createStyle();
        layer.userStyles().add(style);
        FeatureTypeStyle featureStyle = sf.createFeatureTypeStyle();
        style.featureTypeStyles().add(featureStyle);
        Rule rule = sf.createRule();
        featureStyle.rules().add(rule);
        Stroke stroke = sf.stroke(ff.literal("#555555"), null, null, null, null, null, null);
        rule.symbolizers().add(
                sf.lineSymbolizer("line", null, null, null, stroke, null));
        Mark mark = sf.mark(ff.literal("circle"), sf.fill(null, ff.literal("#995555"), null), null);
        List<GraphicalSymbol> symbols = new ArrayList<GraphicalSymbol>();
        symbols.add(mark);
        TextSymbolizer2 text = (TextSymbolizer2) sf.textSymbolizer(null, ff.property("geom"), null, null, ff.property("name"),null,null,null,null);
        text.setGraphic( sf.graphic( symbols,null,null,null,null,null) );
        rule.symbolizers().add(text);
        
        StringWriter out = new StringWriter();
        Ysld.encode(sld, out);

        YamlMap yaml = new YamlMap(new Yaml().load(out.toString()));
        
        assertEquals( "${name}", yaml.lookup("feature-styles/0/rules/0/symbolizers/1/text/label") );
        assertEquals( "circle",  yaml.lookup("feature-styles/0/rules/0/symbolizers/1/text/symbols/0/mark/shape") );
    }
    
    @Test
    public void testEmptyColorMap() throws Exception {
        StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();

        StyledLayerDescriptor sld = sld(styleFactory.createRasterSymbolizer());

        StringWriter out = new StringWriter();
        Ysld.encode(sld, out);

        YamlMap obj = new YamlMap(new Yaml().load(out.toString()));
        YamlMap symbMap = obj.seq("feature-styles").map(0).seq("rules").map(0).seq("symbolizers").map(0).map("raster");

        assertFalse(symbMap.has("color-map"));
    }
    @Test
    public void testEmptyContrastEnhancement() throws Exception {
        StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();

        StyledLayerDescriptor sld = sld(styleFactory.createRasterSymbolizer());

        StringWriter out = new StringWriter();
        Ysld.encode(sld, out);

        YamlMap obj = new YamlMap(new Yaml().load(out.toString()));
        YamlMap symbMap = obj.seq("feature-styles").map(0).seq("rules").map(0).seq("symbolizers").map(0).map("raster");
        
        assertFalse(symbMap.has("contrast-enhancement"));
    }

    @Test
    public void testExpressionNil() throws Exception {
        PointSymbolizer p = CommonFactoryFinder.getStyleFactory().createPointSymbolizer();
        p.getGraphic().setSize(Expression.NIL);

        StringWriter out = new StringWriter();
        Ysld.encode(sld(p), out);

        System.out.println(out.toString());
    }
    
    @Test
    public void testNameExpressionLiteral() throws Exception {
        PointSymbolizer p = CommonFactoryFinder.getStyleFactory().createPointSymbolizer();
        Expression nameExpression = CommonFactoryFinder.getFilterFactory2().literal("test");
        Mark mark = CommonFactoryFinder.getStyleFactory().createMark();
        mark.setWellKnownName(nameExpression);
        p.getGraphic().graphicalSymbols().add(mark);

        StringWriter out = new StringWriter();
        Ysld.encode(sld(p), out);
        
        YamlMap obj = new YamlMap(new Yaml().load(out.toString()));
        String result = obj.seq("feature-styles").map(0).seq("rules").map(0).seq("symbolizers").map(0).map("point").seq("symbols").map(0).map("mark").str("shape");
        
        assertThat(result, equalTo("test"));
    }
    @Test
    public void testNameExpressionAttribute() throws Exception {
        PointSymbolizer p = CommonFactoryFinder.getStyleFactory().createPointSymbolizer();
        Expression nameExpression = CommonFactoryFinder.getFilterFactory2().property("test");
        Mark mark = CommonFactoryFinder.getStyleFactory().createMark();
        mark.setWellKnownName(nameExpression);
        p.getGraphic().graphicalSymbols().add(mark);

        StringWriter out = new StringWriter();
        Ysld.encode(sld(p), out);
        
        YamlMap obj = new YamlMap(new Yaml().load(out.toString()));
        String result = obj.seq("feature-styles").map(0).seq("rules").map(0).seq("symbolizers").map(0).map("point").seq("symbols").map(0).map("mark").str("shape");
        
        assertThat(result, equalTo("${test}"));
    }
    
    @Test
    public void testNonNameExpressionLiteral() throws Exception {
        PointSymbolizer p = CommonFactoryFinder.getStyleFactory().createPointSymbolizer();
        Expression expression = CommonFactoryFinder.getFilterFactory2().literal("test");
        p.setGeometry(expression);

        StringWriter out = new StringWriter();
        Ysld.encode(sld(p), out);
        
        YamlMap obj = new YamlMap(new Yaml().load(out.toString()));
        String result = obj.seq("feature-styles").map(0).seq("rules").map(0).seq("symbolizers").map(0).map("point").str("geometry");
        
        assertThat(result, equalTo("test"));
    }
    @Test
    public void testNonNameExpressionAttribute() throws Exception {
        PointSymbolizer p = CommonFactoryFinder.getStyleFactory().createPointSymbolizer();
        Expression expression = CommonFactoryFinder.getFilterFactory2().property("test");
        p.setGeometry(expression);

        StringWriter out = new StringWriter();
        Ysld.encode(sld(p), out);
        
        YamlMap obj = new YamlMap(new Yaml().load(out.toString()));
        String result = obj.seq("feature-styles").map(0).seq("rules").map(0).seq("symbolizers").map(0).map("point").str("geometry");
        
        assertThat(result, equalTo("${test}"));
    }
    @Test
    public void testEmbededExpression() throws Exception {
        PointSymbolizer p = CommonFactoryFinder.getStyleFactory().createPointSymbolizer();
        Expression expression = CommonFactoryFinder.getFilterFactory2().function("Concatenate", 
                CommonFactoryFinder.getFilterFactory2().literal("literal0"),
                CommonFactoryFinder.getFilterFactory2().property("attribute1"),
                CommonFactoryFinder.getFilterFactory2().literal("literal2")
                );
        p.setGeometry(expression);

        StringWriter out = new StringWriter();
        Ysld.encode(sld(p), out);
        
        YamlMap obj = new YamlMap(new Yaml().load(out.toString()));
        String result = obj.seq("feature-styles").map(0).seq("rules").map(0).seq("symbolizers").map(0).map("point").str("geometry");
        
        assertThat(result, equalTo("literal0${attribute1}literal2"));
    }

    StyledLayerDescriptor sld(Symbolizer sym) {
        StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();

        StyledLayerDescriptor sld = styleFactory.createStyledLayerDescriptor();

        UserLayer layer = styleFactory.createUserLayer();
        sld.layers().add(layer);

        Style style = styleFactory.createStyle();
        layer.userStyles().add(style);

        Rule rule = styleFactory.createRule();

        style.featureTypeStyles().add(styleFactory.createFeatureTypeStyle());
        style.featureTypeStyles().get(0).rules().add(rule);

        RasterSymbolizer symb = styleFactory.createRasterSymbolizer();

        rule.symbolizers().add((Symbolizer)sym);
        return sld;
    }

}
