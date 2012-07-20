package net.refractions.udig.catalog.document;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.eclipse.swt.program.Program;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.Name;
import org.opengis.filter.identity.FeatureId;

import net.refractions.udig.catalog.IAbstractDocumentSource;
import net.refractions.udig.catalog.IDocument;
import net.refractions.udig.catalog.IDocument.TYPE;
import net.refractions.udig.catalog.IDocumentFolder;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IHotlink;
import net.refractions.udig.catalog.IHotlink.HotlinkDescriptor;
import net.refractions.udig.tool.info.InfoPlugin;

public class BasicHotlink implements IHotlink {
    /**
     * {@link IGeoResource#getPersistentProperties()} key used to record hotlink descriptor list.
     * <p>
     * The value is stored as a definition consisting of:
     * <code>attributeName:file,attributeName:link</code>
     */
    final static String HOTLINK = "hotlink";

    class FileLink implements IDocument {
        private File file;
        private String attributeName;
        public FileLink(String attributeName, Object value) {
            String path = (String) value;
            File resourceFile = resource.getID().toFile();
            if (resourceFile != null) {
                // store a relative file path
                file = new File(resourceFile.getParent(), path);
            } else {
                file = new File(path);
            }
            this.attributeName = attributeName;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getDescription() {
            return attributeName+" file:"+file;
        }

        @Override
        public UUID getID() {
            return null;
        }

        @Override
        public URI getURI() {
            return null;
        }

        @Override
        public String getLabel() {
            return file.getName();
        }

        @Override
        public String getAttributeName() {
            return attributeName;
        }

        @Override
        public TYPE getType() {
            return TYPE.FILE;
        }

        @Override
        public IAbstractDocumentSource getSource() {
            return null;
        }

        @Override
        public IDocumentFolder getFolder() {
            return null;
        }

        @Override
        public boolean open() {
            boolean success = Program.launch( file.toString() );
            return success;
        }

        @Override
        public boolean isEmpty() {
           return file == null;
        }

    }

    class WebLink implements IDocument {
        private URL url;
        private String attributeName;
        public WebLink(String attributeName, Object value) {
            String path = (String) value;
            try {
                url = new URL( (String) value );
            } catch (MalformedURLException e) {
                url = null;
            }
            this.attributeName = attributeName;
        }
    
        @Override
        public String getName() {
            return null;
        }
    
        @Override
        public String getDescription() {
            return attributeName+" file:"+url;
        }
    
        @Override
        public UUID getID() {
            return null;
        }
    
        @Override
        public URI getURI() {
            return null;
        }
    
        @Override
        public String getLabel() {
            return url == null ? "(empty)" : url.getFile();
        }
    
        @Override
        public String getAttributeName() {
            return attributeName;
        }
    
        @Override
        public TYPE getType() {
            return TYPE.WEB;
        }
    
        @Override
        public IAbstractDocumentSource getSource() {
            return null;
        }
    
        @Override
        public IDocumentFolder getFolder() {
            return null;
        }
    
        @Override
        public boolean open() {
            boolean success = Program.launch( url.toExternalForm() );
            return success;
        }
    
        @Override
        public boolean isEmpty() {
           return url == null;
        }
    
    }

    private IGeoResource resource;

    public BasicHotlink(IGeoResource resource) {
        this.resource = resource;
    }

    @Override
    public List<HotlinkDescriptor> getHotlinkDescriptors() {
        String definition = (String) resource.getPersistentProperties().get(HOTLINK);
        List<HotlinkDescriptor> list = new ArrayList<IHotlink.HotlinkDescriptor>();
        if (definition != null && !definition.isEmpty()) {
            String split[] = definition.split(",");
            for (String defn : split) {
                try {
                    HotlinkDescriptor descriptor = new HotlinkDescriptor(defn);
                    list.add(descriptor);
                } catch (Throwable t) {
                    InfoPlugin.getDefault().log("Unable describe hotlink:" + defn, t);
                }
            }
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public IDocumentFolder getDocumentsInFolder(SimpleFeature feature, String folderName) {
        return null;
    }

    @Override
    public IDocumentFolder getDocumentsInFolder(SimpleFeature feature) {
        return null;
    }

    @Override
    public List<IDocument> getDocuments(SimpleFeature feature) {
        List<IDocument> list = new ArrayList<IDocument>();
        for (HotlinkDescriptor descriptor : getHotlinkDescriptors()) {
            IDocument document = getDocument(feature, descriptor.getAttributeName());
            list.add(document);
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public IDocument getDocument(SimpleFeature feature, String attributeName) {
        for (HotlinkDescriptor descriptor : getHotlinkDescriptors()) {
            if (descriptor.getAttributeName().equals(attributeName)) {
                return createDocument(feature, descriptor);
            }
        }
        return null; // not available
    }

    private IDocument createDocument(SimpleFeature feature, HotlinkDescriptor descriptor) {
        Object value = feature.getAttribute(descriptor.getAttributeName());
        if (value == null) {
            return null; // document not available
        }
        switch (descriptor.getType()) {
        case FILE:
            return new FileLink(descriptor.getAttributeName(), value);
        case WEB:
            return new WebLink(descriptor.getAttributeName(), value);
        }
        return null;
    }

    @Override
    public IDocument setFile(SimpleFeature feature, String attributeName, File file) {
        
        return null;
    }

    @Override
    public IDocument setLink(SimpleFeature feature, String attributeName, URL link) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IDocument clear(SimpleFeature feature, String attributeName) {
        // TODO Auto-generated method stub
        return null;
    }

}
