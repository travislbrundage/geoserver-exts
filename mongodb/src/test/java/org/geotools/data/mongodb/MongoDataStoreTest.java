package org.geotools.data.mongodb;

import java.util.Arrays;
import java.util.List;

import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureReader;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureWriter;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.GeometryBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public abstract class MongoDataStoreTest extends MongoTestSupport {

    protected MongoDataStoreTest(MongoTestSetup testSetup) {
        super(testSetup);
    }

    public void testGetTypeNames() throws Exception {
        String[] typeNames = dataStore.getTypeNames();
        assertEquals(1, typeNames.length);
        assertEquals("ft1", typeNames[0]);
    }

    public void testGetSchema() throws Exception {
        SimpleFeatureType schema = dataStore.getSchema("ft1");
        assertNotNull(schema);

        assertNotNull(schema.getDescriptor("geometry"));
        assertTrue(Geometry.class.isAssignableFrom(schema.getDescriptor("geometry").getType().getBinding()));
    }

    public void testGetFeatureReader() throws Exception {
        SimpleFeatureReader reader = (SimpleFeatureReader) 
            dataStore.getFeatureReader(new Query("ft1"), Transaction.AUTO_COMMIT);
        try {
            for (int i = 0; i < 3; i++) {
                assertTrue(reader.hasNext());
                SimpleFeature f = reader.next();

                assertFeature(f);
            } 
            assertFalse(reader.hasNext());
        }
        finally {
            reader.close();
        }
    }

    public void testGetFeatureSource() throws Exception {
        SimpleFeatureSource source = dataStore.getFeatureSource("ft1");
        assertEquals(3, source.getCount(Query.ALL));

        ReferencedEnvelope env = source.getBounds();
        assertEquals(0d, env.getMinX(), 0.1);
        assertEquals(0d, env.getMinY(), 0.1);
        assertEquals(2d, env.getMaxX(), 0.1);
        assertEquals(2d, env.getMaxY(), 0.1);
    }

    public void testGetAppendFeatureWriter() throws Exception {
        FeatureWriter w = dataStore.getFeatureWriterAppend("ft1", Transaction.AUTO_COMMIT);
        SimpleFeature f = (SimpleFeature) w.next();

        GeometryBuilder gb = new GeometryBuilder();
        f.setDefaultGeometry(gb.point(3, 3));
        f.setAttribute("intProperty", 3);
        f.setAttribute("doubleProperty", 3.3);
        f.setAttribute("stringProperty", "three");
        w.write();
        w.close();
    }

    public void testCreateSchema() throws Exception {
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName("ft2");

        List<String> typeNames = Arrays.asList(dataStore.getTypeNames());
        assertFalse(typeNames.contains("ft2"));

        dataStore.createSchema(tb.buildFeatureType());
        assertEquals(typeNames.size()+1, dataStore.getTypeNames().length);
        typeNames = Arrays.asList(dataStore.getTypeNames());
        assertTrue(typeNames.contains("ft2"));

        SimpleFeatureSource source = dataStore.getFeatureSource("ft2");
        assertEquals(0, source.getCount(new Query("ft2")));
        
        FeatureWriter w = dataStore.getFeatureWriterAppend("ft2", Transaction.AUTO_COMMIT);
        SimpleFeature f = (SimpleFeature) w.next();
        f.setDefaultGeometry(new GeometryBuilder().point(1,1));
        f.setAttribute("intProperty", 1);
        w.write();

        source = dataStore.getFeatureSource("ft2");
        assertEquals(1, source.getCount(new Query("ft2")));
        
    }
}
