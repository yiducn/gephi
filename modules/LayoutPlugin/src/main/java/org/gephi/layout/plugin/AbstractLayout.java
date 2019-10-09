/*
 Copyright 2008-2010 Gephi
 Authors : Helder Suzuki <heldersuzuki@gephi.org>
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
package org.gephi.layout.plugin;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;

/**
 * Base class for layout algorithms.
 *
 * @author Helder Suzuki
 */
public abstract class AbstractLayout implements Layout {

    private final LayoutBuilder layoutBuilder;
    protected GraphModel graphModel;
    private boolean converged;

    public AbstractLayout(LayoutBuilder layoutBuilder) {
        this.layoutBuilder = layoutBuilder;
    }

    @Override
    public LayoutBuilder getBuilder() {
        return layoutBuilder;
    }

    @Override
    public void setGraphModel(GraphModel graphModel) {
        this.graphModel = graphModel;
    }

    @Override
    public boolean canAlgo() {
        return !isConverged() && graphModel != null;
    }

    public void setConverged(boolean converged) {
        this.converged = converged;
    }

    public boolean isConverged() {
        return converged;
    }

    /**
     * See https://github.com/gephi/gephi/issues/603 Nodes position to NaN on applied layout
     *
     * @param graphModel
     */
    public static void ensureSafeLayoutNodePositions(GraphModel graphModel) {
        Graph graph = graphModel.getGraph();
        NodeIterable nodesIterable = graph.getNodes();
        for (Node node : nodesIterable) {
            if (node.x() != 0 || node.y() != 0) {
                nodesIterable.doBreak();
                return;
            }
        }

        //All at 0.0, init some random positions
        nodesIterable = graph.getNodes();
        for (Node node : nodesIterable) {
            node.setX((float) ((0.01 + Math.random()) * 1000) - 500);
            node.setY((float) ((0.01 + Math.random()) * 1000) - 500);
        }
    }
}
