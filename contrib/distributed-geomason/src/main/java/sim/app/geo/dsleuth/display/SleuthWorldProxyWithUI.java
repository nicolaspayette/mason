package sim.app.geo.dsleuth.display;

import java.awt.Color;


import javax.swing.JFrame;

import org.jfree.data.xy.XYSeries;

//import sim.app.dheatbugs.display.HeatBugsProxy;
import sim.app.geo.dsleuth.DSleuthWorld;
import sim.app.geo.sleuth.SleuthWorld;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.portrayal.grid.DenseGridPortrayal2D;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.portrayal.grid.ObjectGridPortrayal2D;
import sim.util.gui.ColorMap;
import sim.util.media.chart.TimeSeriesChartGenerator;

public class SleuthWorldProxyWithUI extends GUIState{
	
    DSleuthWorld sleuthworld;
    private Display2D display;
    private JFrame displayFrame;
    
    // portrayal data
    ObjectGridPortrayal2D slope = new ObjectGridPortrayal2D();
    ObjectGridPortrayal2D landuse = new ObjectGridPortrayal2D();
    ObjectGridPortrayal2D excluded = new ObjectGridPortrayal2D();
    ObjectGridPortrayal2D urban = new ObjectGridPortrayal2D();
    ObjectGridPortrayal2D urbanOverTime = new ObjectGridPortrayal2D();
    ObjectGridPortrayal2D transport = new ObjectGridPortrayal2D();
    ObjectGridPortrayal2D hillshade = new ObjectGridPortrayal2D();
    
    // chart information
    TimeSeriesChartGenerator urbanChart;
    XYSeries numUrban;


    public static void main(String[] args)
    {
        new SleuthWorldProxyWithUI().createController();
    }

    public SleuthWorldProxyWithUI()
	{
    	super(new SleuthWorldProxy(System.currentTimeMillis()));
	}

    public SleuthWorldProxyWithUI(SimState state)
	{
    	super(state);
	}

    public static String getName()
    {
    	return "SleuthWorld Proxy";
    }

    public Object getSimulationInspectedObject()
	{
    	return state; // non-volatile
	}    
    
    /**
     * Called when starting a new run of the simulation. Sets up the portrayals
     * and chart data.
     */
    @Override
    public void start()
    {
        super.start();

        // set up the chart info
        numUrban = new XYSeries("Number of Urban Tiles");
        urbanChart.removeAllSeries();
        urbanChart.addSeries(numUrban, null);

        // schedule the chart to take data
        state.schedule.scheduleRepeating(new Steppable()
        {

            public void step(SimState state)
            {
                DSleuthWorld sw = (DSleuthWorld) state;
                numUrban.add(state.schedule.getTime(), sw.numUrban
                    / (double) (sw.numUrban + sw.numNonUrban));
            }

        });
        
        // set up the portrayals
        slope.setField(sleuthworld.landscape);
        slope.setPortrayalForAll(new SlopePortrayal());

        landuse.setField(sleuthworld.landscape);
        landuse.setPortrayalForAll(new LandusePortrayal());

        excluded.setField(sleuthworld.landscape);
        excluded.setPortrayalForAll(new ExcludedPortrayal());

        urban.setField(sleuthworld.landscape);
        urban.setPortrayalForAll(new OriginalUrbanPortrayal());

        urbanOverTime.setField(sleuthworld.landscape);
        urbanOverTime.setPortrayalForAll(new GrowingUrbanZonesPortrayal());

        transport.setField(sleuthworld.landscape);
        transport.setPortrayalForAll(new TransportPortrayal());

        hillshade.setField(sleuthworld.landscape);
        hillshade.setPortrayalForAll(new HillshadePortrayal());

        // reschedule the displayer
        display.reset();
        display.setBackdrop(Color.white);

        // redraw the display
        display.repaint();
    }



    /**
     * Called when first beginning a SleuthWorldWithUI. Sets up the display window,
     * the JFrames, and the chart structure.
     */
    @Override
    public void init(Controller c)
    {
        super.init(c);

        // make the displayer
        display = new Display2D(600, 600, this);
        // turn off clipping
        display.setClipping(false);

        displayFrame = display.createFrame();
        displayFrame.setTitle("SleuthWorld Display");
        c.registerFrame(displayFrame); // register the frame so it appears in
        // the "Display" list
        displayFrame.setVisible(true);

        display.attach(slope, "Slope");
        display.attach(landuse, "Landuse");
        display.attach(excluded, "Excluded");
        display.attach(urbanOverTime, "Current Urban");
        display.attach(urban, "Initial Urban");
        display.attach(transport, "Transport");
        display.attach(hillshade, "Hillshade");

        // chart!
        urbanChart = new TimeSeriesChartGenerator();
        urbanChart.setTitle("Percent of Urban Tiles in Simulation");
        urbanChart.setYAxisLabel("Percent of Urban Tiles");
        urbanChart.setXAxisLabel("Time");

        JFrame chartFrame = urbanChart.createFrame(this);
        chartFrame.setVisible(true);
        chartFrame.pack();
        c.registerFrame(chartFrame);

    }



    /** called when quitting a simulation. Does appropriate garbage collection. */
    public void quit()
    {
        super.quit();

        if (displayFrame != null)
        {
            displayFrame.dispose();
        }
        displayFrame = null; // let gc
        display = null; // let gc
    }

    // COLORMAPS FOR PORTRAYALS
    // colormap for slope, which is assumed to be between 0 and 255.
    private static ColorMap slopeColor = new sim.util.gui.SimpleColorMap(
        0, 100, new Color(250, 250, 250), new Color(0, 0, 0));
    private static ColorMap hillshadeColor = new sim.util.gui.SimpleColorMap(
        0, 255, new Color(250, 250, 250, 100), new Color(0, 0, 0, 100));



    public static ColorMap getHillshadeColor()
    {
        return hillshadeColor;
    }



    public static ColorMap getSlopeColor()
    {
        return slopeColor;
    }
    
}