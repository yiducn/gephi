/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.graph;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.WorkspaceBytesPersistenceProvider;
import org.gephi.project.spi.WorkspacePersistenceProvider;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = WorkspacePersistenceProvider.class, position = 100)
public class GraphPersistenceProvider implements WorkspaceBytesPersistenceProvider {

    @Override
    public void writeBytes(DataOutputStream stream, Workspace workspace) {
        GraphModel model = workspace.getLookup().lookup(GraphModel.class);
        if (model != null) {
            try {
                GraphModel.Serialization.write(stream, model);
            } catch (IOException ex) {
                Logger.getLogger("").log(Level.SEVERE, "", ex.getCause());
            }
        }
    }

    private static final int GRAPHSTORE_SERIALIZATION_GRAPHMODEL_CONFIG_ID = 205;
    
    @Override
    public void readBytes(DataInputStream stream, Workspace workspace) {
        GraphModel model = workspace.getLookup().lookup(GraphModel.class);
        if (model != null) {
            throw new IllegalStateException("The graphModel wasn't null");
        }
        try {
            //Detect if the serialized graphstore declares its own version:
            stream.mark(1);
            int firstFieldType = stream.readUnsignedByte();
            stream.reset();
            
            if (firstFieldType == GRAPHSTORE_SERIALIZATION_GRAPHMODEL_CONFIG_ID) {
                //Old graphstore, from Gephi 0.9.0
                model = GraphModel.Serialization.readWithoutVersionHeader(stream, 0.0f /* no version, first was 0.4*/);//Previous to version header existing at all
            } else {
                model = GraphModel.Serialization.read(stream);
            }

            workspace.add(model);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String getIdentifier() {
        return "graphstore";
    }
}
