package org.gephi.io.exporter.preview;

import org.gephi.desktop.welcome.WelcomeTopComponent;
import org.gephi.io.exporter.spi.ByteExporter;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.junit.Test;
import org.openide.util.Lookup;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: edubecks
 * Date: 5/2/13
 * Time: 6:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class PNGExporterTest {

    @Test
    public void testExecute() throws Exception {

        //Init a project - and therefore a workspace

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        //Append container to graph structure
        String sample = "/org/gephi/desktop/welcome/samples/Les Miserables.gexf";

        String fileString = "/org/gephi/desktop/welcome/samples/graph.gexf";
        File file = new File(fileString);


        final InputStream stream = WelcomeTopComponent.class.getResourceAsStream(fileString);
        System.out.println("@Var: stream: "+stream);

        //            final InputStream stream = new FileInputStream("/Users/eduBecKs/Desktop/partitions.gephi");
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        FileImporter fileImporter = importController.getFileImporter(".gexf");
        //            FileImporter fileImporter = importController.getFileImporter(new File("/Users/eduBecKs/Desktop/partitions.gephi"));
        System.out.println("@Var: fileImporter: " + fileImporter);

        Container container = importController.importFile(stream, fileImporter);

        importController.process(container, new DefaultProcessor(), workspace);


        PNGExporter fileExporter = new PNGExporter();
        fileExporter.setHeight(1024);
        fileExporter.setWidth(1024);
        fileExporter.setMargin(0);
        fileExporter.setTransparentBackground(true);
        fileExporter.setWorkspace(workspace);
        File fileOutput = new File("/Volumes/datos/temp/test.png");
        OutputStream outStream = new BufferedOutputStream(new FileOutputStream(fileOutput));
        ((ByteExporter) fileExporter).setOutputStream(outStream);
        fileExporter.execute();
        System.out.println("Holaaa");

    }
}
