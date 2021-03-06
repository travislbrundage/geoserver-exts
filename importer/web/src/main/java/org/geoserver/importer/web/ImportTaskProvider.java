package org.geoserver.importer.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.geoserver.web.wicket.GeoServerDataProvider;
import org.geoserver.importer.ImportContext;
import org.geoserver.importer.ImportTask;

public class ImportTaskProvider extends GeoServerDataProvider<ImportTask> {

    public static Property<ImportTask> NAME = new BeanProperty("name", "layer.name");
    //public static Property<ImportItem> FORMAT = new BeanProperty("format", "format");
    public static Property<ImportTask> STATUS = new BeanProperty("status", "state");
    public static Property<ImportTask> ACTION = new BeanProperty("action", "state");

    IModel<ImportContext> context;

    public ImportTaskProvider(ImportContext context) {
        this(new ImportContextModel(context));
    }

    public ImportTaskProvider(IModel<ImportContext> context) {
        this.context = context;
    }

    @Override
    protected List<Property<ImportTask>> getProperties() {
        return Arrays.asList(NAME, STATUS, ACTION);
    }

    @Override
    protected List<ImportTask> getItems() {
        List<ImportTask> tasks = new ArrayList(context.getObject().getTasks());
//        Collections.sort(items, new Comparator<ImportItem>() {
//            public int compare(ImportItem o1, ImportItem o2) {
//                return o1.getState().compareTo(o2.getState());
//            }
//        });
        return tasks;
    }

    @Override
    protected IModel newModel(Object object) {
        return new ImportTaskModel((ImportTask)object);
    }
}
