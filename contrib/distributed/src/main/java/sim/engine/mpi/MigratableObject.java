/*
  Copyright 2022 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
        
package sim.engine.mpi;

import java.io.Serializable;

/**
 * An object that can be sent on the transport layer.
 * 
 * @author Carmine Spagnuolo
 *
 */
public abstract class MigratableObject implements Serializable
    {
    private static final long serialVersionUID = 1L;

    private String exportName = null;

    public void setExportedName(String exportName)
        {
        this.exportName = exportName;

        }

    public String getExportedName()
        {
        return this.exportName;
        }

    }
