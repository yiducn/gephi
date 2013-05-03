/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.io.exporter.preview;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import javax.swing.*;

import org.gephi.io.exporter.spi.ByteExporter;
import org.gephi.io.exporter.spi.VectorExporter;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.ProcessingTarget;
import org.gephi.preview.api.RenderTarget;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;
import processing.core.PGraphicsJava2D;
import processing.core.PMatrix;

/**
 * @author Mathieu Bastian
 */
public class PNGExporter implements VectorExporter, ByteExporter, LongTask {

    private ProgressTicket progress;
    private boolean cancel = false;
    private Workspace workspace;
    private OutputStream stream;
    private int width = 1024;
    private int height = 1024;
    private boolean transparentBackground = false;
    private int margin = 4;
    private ProcessingTarget target;


    private int extraMargin() {

        Progress.start(progress);

        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        controller.getModel(workspace).getProperties().putValue(PreviewProperty.VISIBILITY_RATIO, 1.0);
        PreviewProperties props = controller.getModel(workspace).getProperties();

        int extraMargin = (int) Math.min(50, 0.05 * Math.max(width, height));
        System.out.println("@Var: extraMargin: "+extraMargin);
        props.putValue("width", width - 2 * extraMargin);
        props.putValue("height", height - 2 * extraMargin);
        props.putValue("margin", extraMargin);

        controller.refreshPreview(workspace);
        target = (ProcessingTarget) controller.getRenderTarget(RenderTarget.PROCESSING_TARGET, workspace);
        target.refresh();

        // transparent color
        Color transparentColor = new Color(255, 255, 255, 0);
        int transparentColorInt = transparentColor.getRGB();

        // searching for external pixels
        PGraphicsJava2D pg2 = (PGraphicsJava2D) target.getGraphics();
        System.out.println("@Var: pg2: "+pg2);

        boolean flag = false;
        int newMargin = extraMargin;
        System.out.println(pg2.pixels.length);
        for (int window = 0; window < extraMargin; window++) {
            // up
            if (flag) {
                newMargin = window;
                break;
            }
            for (int i = 0; i < width; i++) {
                if (pg2.pixels[window * width + i] != transparentColorInt) {
                    flag = true;
                    break;
                }
            }

            // bottom
            if (flag) {
                newMargin = window;
                break;
            }
            for (int i = 0; i < width; i++) {
                if (pg2.pixels[height - 1 - window * width + i] != transparentColorInt) {
                    flag = true;
                    break;
                }
            }

            // left
            if (flag) {
                newMargin = window;
                break;
            }
            for (int i = 0; i < height; i++) {
                if (pg2.pixels[i * width + window] != transparentColorInt) {
                    flag = true;
                    break;
                }
            }

            // right
            if (flag) {
                newMargin = window;
                break;
            }
            for (int i = 0; i < height; i++) {
                if (pg2.pixels[i * width + height - 1 + window] != transparentColorInt) {
                    flag = true;
                    break;
                }
            }


        }


        props.removeSimpleValue("width");
        props.removeSimpleValue("height");
        props.removeSimpleValue("margin");

        return extraMargin - newMargin;
    }

    @Override
    public boolean execute() {
        Progress.start(progress);

        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        controller.getModel(workspace).getProperties().putValue(PreviewProperty.VISIBILITY_RATIO, 1.0);

        int newMargin = extraMargin();
        System.out.println("@Var: newMargin: "+newMargin);


        PreviewProperties props = controller.getModel(workspace).getProperties();
        props.putValue("width", width);
        props.putValue("height", height);
        Color oldColor = props.getColorValue(PreviewProperty.BACKGROUND_COLOR);
        Color transparentColor = new Color(255, 255, 255, 0);
        int transparentColorInt = transparentColor.getRGB();
        if (transparentBackground) {
            props.putValue(PreviewProperty.BACKGROUND_COLOR, transparentColor);//White transparent
        }
        props.putValue(PreviewProperty.MARGIN, new Float((float) margin));
        controller.refreshPreview(workspace);
        target = (ProcessingTarget) controller.getRenderTarget(RenderTarget.PROCESSING_TARGET, workspace);
        target.refresh();


        if (target instanceof LongTask) {
            ((LongTask) target).setProgressTicket(progress);
        }
        //Fix bug caused by keeping width and height in the workspace preview properties.
        //When a .gephi file is loaded later with these properties PGraphics will be created instead of a PApplet
        props.removeSimpleValue("width");
        props.removeSimpleValue("height");
        props.removeSimpleValue(PreviewProperty.MARGIN);
        try {
            target.refresh();
            Progress.switchToIndeterminate(progress);
            PGraphicsJava2D pg2 = (PGraphicsJava2D) target.getGraphics();
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            img.setRGB(0, 0, width, height, pg2.pixels, 0, width);
            ImageIO.write(img, "png", stream);
            stream.close();
            props.putValue(PreviewProperty.BACKGROUND_COLOR, oldColor);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Progress.finish(progress);
        return !cancel;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getMargin() {
        return margin;
    }

    public void setMargin(int margin) {
        this.margin = margin;
    }

    public boolean isTransparentBackground() {
        return transparentBackground;
    }

    public void setTransparentBackground(boolean transparentBackground) {
        this.transparentBackground = transparentBackground;
    }

    @Override
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
    public void setOutputStream(OutputStream stream) {
        this.stream = stream;
    }

    public boolean cancel() {
        cancel = true;
        if (target instanceof LongTask) {
            ((LongTask) target).cancel();
        }
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }
}
