/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sim.app.geo.dadaab;

/**
 *
 * @author gmu
 */
// reed all files
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

import ec.util.MersenneTwisterFast;
import sim.app.geo.dadaab.dadaabData.DadaabData;
import sim.app.geo.dadaab.dadaabData.road.RoadData;
import sim.field.continuous.Continuous2D;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomGridField.GridDataType;
import sim.field.geo.GeomVectorField;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;
import sim.field.grid.ObjectGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.field.network.Edge;
import sim.io.geo.ArcInfoASCGridImporter;
import sim.io.geo.ShapeFileImporter;
//import Dadaab;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;

// all falimies in a bag and assign
public class CampBuilder {

    static int gridWidth = 0;
    static int gridHeight = 0;
    // Location locations;

    // static MersenneTwisterFast random;

    static public void create(final URL campfile, final URL facilityfile, final URL roadfile,
        final Dadaab dadaab, final MersenneTwisterFast random) throws Exception {
        // CampBuilder.random = random;
        // buffer reader - read ascii file

        final BufferedReader camp = new BufferedReader(new InputStreamReader(campfile.openStream()));

        // first read the dimensions
        String line = camp.readLine(); // read line for width
        String[] tokens = line.split("\\s+");
        final int width = Integer.parseInt(tokens[1]);
        CampBuilder.gridWidth = width;

        line = camp.readLine();
        tokens = line.split("\\s+");
        final int height = Integer.parseInt(tokens[1]);
        CampBuilder.gridHeight = height;

        createGrids(width, height, dadaab);

        // skip the next four lines as they contain irrelevant metadata

        for (int i = 0; i < 4; ++i) {
            line = camp.readLine();
            }

        dadaab.campSites.clear();// clear the bag

        for (int curr_row = 0; curr_row < height; ++curr_row) {
            line = camp.readLine();

            tokens = line.split("\\s+");

            for (int curr_col = 0; curr_col < width; ++curr_col) {
                final int camptype = Integer.parseInt(tokens[curr_col]);

                final FieldUnit fieldUnit = new FieldUnit();

                if (camptype > 0) {
                    fieldUnit.setFieldID(camptype);

                    if (camptype == 11 || camptype == 21 || camptype == 31) {
                        dadaab.campSites.add(fieldUnit);
                        }

                    if (camptype >= 10 && camptype <= 12) {
                        fieldUnit.setCampID(1);
                        } else if (camptype >= 20 && camptype <= 22) {
                        fieldUnit.setCampID(2);
                        }

                    else if (camptype >= 30 && camptype <= 32) {
                        fieldUnit.setCampID(3);
                        } else {
                        fieldUnit.setCampID(0);

                        }

                    } else {
                    fieldUnit.setFieldID(0);
                    }

                fieldUnit.setX(curr_col);
                fieldUnit.setY(curr_row);
                fieldUnit.setWater(0);
                // dadaab.allFields.add(fieldUnit);
                dadaab.allCamps.field[curr_col][curr_row] = fieldUnit;

                }

            }

        // read elev and change camp locations id to elev

        // TODO: Try with resources??
        camp.close();

        final InputStream inputStream = campfile.openStream();

        ArcInfoASCGridImporter.read(inputStream, GridDataType.INTEGER, dadaab.allCampGeoGrid);
        // overwrite the file and make 100

        // now read facility grid
        final BufferedReader fac = new BufferedReader(new InputStreamReader(facilityfile.openStream()));

        // skip the irrelevant metadata
        for (int i = 0; i < 6; i++) {
            fac.readLine();
            }

        for (int curr_row = 0; curr_row < height; ++curr_row) {
            line = fac.readLine();
            tokens = line.split("\\s+");

            for (int curr_col = 0; curr_col < width; ++curr_col) {
                final int facilitytype = Integer.parseInt(tokens[curr_col]);

                if (facilitytype > 0 && facilitytype < 11) {

                    final Facility facility = new Facility();
                    final FieldUnit facilityField = (FieldUnit) dadaab.allCamps.get(curr_col, curr_row);
                    facility.setLoc(facilityField);
                    facilityField.setFacility(facility);
                    dadaab.allFacilities.add(facilityField);
                    // facility.setCapacity(0);

                    if (facilitytype == 1) {

//                            if(curr_col ==54 && curr_row ==113){
//                                facility.setInfectionLevel(dadaab.params.global.getvibrioCholeraePerInfectedPerson() );
//                                //facilityField.setVibrioCholerae(dadaab.params.global.getMaximumWaterRequirement() * dadaab.params.global.getvibrioCholeraePerInfectedPerson());
//                            }
//                            else{
//                                facility.setInfectionLevel(0);
//                                facilityField.setVibrioCholerae(0);
//                            }

                        facility.setInfectionLevel(0);
                        facilityField.setVibrioCholerae(0);
                        facility.setFacilityID(2);

                        facilityField.setWater(dadaab.params.global.getBoreholeWaterSupplyPerDay());

                        dadaab.boreHoles.add(facilityField);

                        } else if (facilitytype == 2 || facilitytype == 3) {

                        facility.setFacilityID(6);
                        dadaab.healthCenters.add(facilityField);

                        } else if (facilitytype == 4) {

                        facility.setFacilityID(5);
                        dadaab.foodCenter.add(facilityField);

                        } else if (facilitytype > 5 && facilitytype <= 8) {

                        facility.setFacilityID(1);
                        dadaab.schooles.add(facilityField);
                        } else if (facilitytype == 9) {

                        facility.setFacilityID(4);
                        dadaab.market.add(facilityField);

                        } else if (facilitytype == 10) {

                        facility.setFacilityID(3);
                        dadaab.mosques.add(facilityField);
                        }

                    else {

                        facility.setFacilityID(8);
                        dadaab.other.add(facilityField);
                        }

                    dadaab.facilityGrid.setObjectLocation(facility, curr_col, curr_row);

                    }
                }
            }
        fac.close();
        inputStream.close();
        // now read road grid

        final BufferedReader road = new BufferedReader(new InputStreamReader(roadfile.openStream()));
        // skip the irrelevant metadata
        for (int i = 0; i < 6; i++) {
            road.readLine();
            }

        for (int curr_row = 0; curr_row < height; ++curr_row) {
            line = road.readLine();

            tokens = line.split("\\s+");

            for (int curr_col = 0; curr_col < width; ++curr_col) {
                final double r = Double.parseDouble(tokens[curr_col]); // no need
                final int roadID = (int) r * 1000;
                if (roadID >= 0) {
                    dadaab.roadGrid.set(curr_col, curr_row, roadID);

                    }
                }
            }

        road.close();

        final BufferedReader dailyRainfall = new BufferedReader(
            new InputStreamReader(DadaabData.class.getResourceAsStream("dadaabDailyRain.csv")));

        for (int curr_row = 0; curr_row < height; ++curr_row) {

            line = dailyRainfall.readLine();

            tokens = line.split("\\s+");
            final int rain = Integer.parseInt(tokens[0]);
            dadaab.dailyRain[curr_row] = rain;

            }

        dailyRainfall.close();

        // now read elev file and store in bag

        final BufferedReader elev = new BufferedReader(
            new InputStreamReader(DadaabData.class.getResourceAsStream("d_dem_n.txt")));

        // skip the irrelevant metadata
        for (int i = 0; i < 6; i++) {
            elev.readLine();
            }

        for (int curr_row = 0; curr_row < height; ++curr_row) {
            line = elev.readLine();

            tokens = line.split("\\s+");

            for (int curr_col = 0; curr_col < width; ++curr_col) {
                final double elevation = Double.parseDouble(tokens[curr_col]);

                if (elevation > 0) {

                    final FieldUnit elevationField = (FieldUnit) dadaab.allCamps.get(curr_col, curr_row);
                    elevationField.setElevation(elevation);

                    }
                }

            }

        elev.close();

        // read shape file

        final Bag maskedCamp = new Bag();
        maskedCamp.add("CAMPID");

        final URL campShapUL = RoadData.class.getResource("Camp_n.shp");
        final URL campShapDF = RoadData.class.getResource("Camp_n.dbf");

//              final URL campShapUL = getUrl("/dadaab/dadaabData/Road/Camp_n.shp");
//              final URL campShapDF = getUrl("/dadaab/dadaabData/Road/Camp_n.dbf");

        ShapeFileImporter.read(campShapUL, campShapDF, dadaab.campShape, maskedCamp);

        final Bag masked = new Bag();

        final URL roadLinkUL = RoadData.class.getResource("dadaab_road_f_node.shp");
        final URL roadLinkDF = RoadData.class.getResource("dadaab_road_f_node.dbf");

        ShapeFileImporter.read(roadLinkUL, roadLinkDF, dadaab.roadLinks, masked);

        extractFromRoadLinks(dadaab.roadLinks, dadaab); // construct a network of roads

        // set up the locations and nearest node capability

        dadaab.closestNodes = setupNearestNodes(dadaab);

        populate(random, dadaab);
        // random
        final int max = dadaab.params.global.getMaximumNumberRelative();
        final int[] numberOfFamilies = new int[dadaab.allFamilies.numObjs];

        for (int i = 0; i < dadaab.allFamilies.numObjs; i++) {

            final Family f = (Family) dadaab.allFamilies.objs[i];
            int tot = 0;
            if (dadaab.allFamilies.numObjs > max) {
                tot = max;
                }

            else
                tot = dadaab.allFamilies.numObjs;

            final int numOfRel = 1 + dadaab.random.nextInt(tot - 1);

            // swap the array index
            for (int kk = 0; kk < numberOfFamilies.length; kk++) {
                final int idx = dadaab.random.nextInt(numberOfFamilies.length);
                final int temp = numberOfFamilies[idx];
                numberOfFamilies[idx] = numberOfFamilies[i];
                numberOfFamilies[i] = temp;
                }

            for (int jj = 0; jj < numOfRel; jj++) {
                if (f.equals(dadaab.allFamilies.objs[numberOfFamilies[jj]]) != true) {
                    final FieldUnit l = ((Family) dadaab.allFamilies.objs[numberOfFamilies[jj]]).getCampLocation();
                    f.addRelative(l);
                    }
                }

            }

        }

    private static void createGrids(final int width, final int height, final Dadaab dadaab) {
        System.out.println("Creating grids!");
        dadaab.allCamps = new ObjectGrid2D(width, height);
        dadaab.rainfallGrid = new DoubleGrid2D(width, height, 0);
        dadaab.allRefugees = new Continuous2D(0.1, width, height);
        dadaab.facilityGrid = new SparseGrid2D(width, height);

        dadaab.roadGrid = new IntGrid2D(width, height);
        dadaab.nodes = new SparseGrid2D(width, height);
        dadaab.closestNodes = new ObjectGrid2D(width, height);
        dadaab.roadLinks = new GeomVectorField(width, height);
        dadaab.campShape = new GeomVectorField(width, height);

        dadaab.allCampGeoGrid = new GeomGridField();
        }

//      private static URL getUrl(final String nodesFilename) throws IOException {
//              try {
//                      final InputStream nodeStream = Dadaab.class.getResourceAsStream(nodesFilename);
//
//
//
//                      // nodeStream.read(buffer);
//                      if (!new File("./shapeFiles/").exists()) {
//                              new File("./shapeFiles/").mkdir();
//                      }
//                      final File targetFile = new File(
//                                      "./shapeFiles/" + nodesFilename.split("/")[nodesFilename.split("/").length - 1]);
//                      final OutputStream outStream = new FileOutputStream(targetFile);
//                      // outStream.write(buffer);
//                      int read = 0;
//                      final byte[] bytes = new byte[1024];
//                      while ((read = nodeStream.read(bytes)) != -1) {
//                              outStream.write(bytes, 0, read);
//                      }
//                      outStream.close();
//                      nodeStream.close();
//                      if (nodesFilename.endsWith(".shp")) {
//                              getUrl(nodesFilename.replace("shp", "dbf"));
//                              getUrl(nodesFilename.replace("shp", "prj"));
//                              getUrl(nodesFilename.replace("shp", "sbx"));
//                              getUrl(nodesFilename.replace("shp", "sbn"));
//                              getUrl(nodesFilename.replace("shp", "shx"));
//                      }
//                      return targetFile.toURI().toURL();
//              } catch (final Exception e) {
//                      if (nodesFilename.endsWith("shp")) {
//                              e.printStackTrace();
//                              return null;
//                      } else {
//                              // e.printStackTrace();
//                              return null;
//                      }
//              }
//      }

//// add households
    private static void addAllRefugees(final int age, final int sex, final Family hh, final MersenneTwisterFast random,
        final Dadaab dadaab) {

        final Refugee newRefugee = new Refugee(age, sex, hh, hh.getCampLocation(), hh.getCampLocation(), random,
            dadaab.allRefugees);
        hh.addMembers(newRefugee);
        hh.getCampLocation().addRefugee(newRefugee);
        newRefugee.setBodyResistance(1);
        newRefugee.setHealthStatus(1);
        newRefugee.setCurrentActivity(0);
        newRefugee.setWaterLevel(2 * dadaab.params.global.getMinimumWaterRequirement()
            + dadaab.params.global.getMaximumWaterRequirement() * random.nextDouble());

        final double ratioInfected = (dadaab.params.global.getPercentageOfAsymptomatic() / (100));
        final double ageEffect = 0.5 + 0.5 * (Math.pow(newRefugee.getAge(), 2) / (Math.pow(60, 2.5)));

        if (dadaab.random.nextDouble() > ratioInfected * ageEffect) {

            newRefugee.setSymtomaticType(1);// symtotic
            } else {
            newRefugee.setSymtomaticType(2); // asymptotic
            }

        int study = 0;
        if (age >= 5 && age < 15) {
            if (dadaab.random.nextDouble() > 0.56) {
                study = 1;
                } else
                study = 0;
            }

        else {
            study = 0;
            }

        newRefugee.setStudyID(study);

        newRefugee.setStoppable(dadaab.schedule.scheduleRepeating(newRefugee, Refugee.ORDERING, 1.0));
        }

    // random searching of next parcel to populate houses
    public static FieldUnit nextAvailCamp(final Dadaab dadaab) {

        // for now random

        int x = dadaab.random.nextInt(dadaab.campSites.numObjs);
        while (((FieldUnit) dadaab.campSites.objs[x]).isCampOccupied(dadaab) == true
            || dadaab.allFacilities.contains(dadaab.campSites.objs[x]) == true) {
            // try another spot
            x = dadaab.random.nextInt(dadaab.campSites.numObjs);

            }

//
        return (FieldUnit) dadaab.campSites.objs[x];

        }

    // create refugees - first hh
    private static void populateRefugee(final MersenneTwisterFast random, final Dadaab dadaab) {

        // UNHCR stat
        // age distibution
        // 1-4 = 0.20; 5-11 = 0.25; 12-17 = 0.12; 18-59 = 0.40;>= 60 = 0.;

        // family size
        // 1 = 30% , 2 =12% , 3 = 11%, 4=13%, 5 =12%, 6 = 10%, >6= 12%

        // proportion of teta = families/ total population = 8481/29772 ~ 0.3

        //
        final double teta = 0.3;
        final int totalRef = dadaab.params.global.getInitialRefugeeNumber();
        // prprtion of hh to total size
        // System.out.println("s: " + totalRef);

        // int hhsize = (totalRef * teta /10 ) +

        final double[] prop = { 0.30, 0.12, 0.11, 0.13, 0.12, 0.10, 0.06, 0.03, 0.01, 0.01, 0.01 }; // proportion of
        // household
        final int[] size = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }; // family size - all are zero
        // family size ranges from 1 to 11

        final int count = 0;
        final int rem = 0;// remaining
        int curTot = 0;

//
//        while (curTot < totalRef) {
//
//            for (int i = 0; i < size.length; i++) {
//                rem = totalRef - curTot;
//                if (rem >= i + 1) {
//                    double x = prop[i] * totalRef * teta;
//                    int hh = (int) Math.round(x);
//                    size[i] = hh;
//                    curTot = curTot + ((i + 1) * hh);
//
//                } else {
//
//                    int t = 0;
//                    int r = 0;
//                    while (t < rem) {
//
//                        for (int j = 0; j < rem; j++) {
//                            r = rem - t;
//                            if (r > j) {
//                                int temp = 0;
//                                temp = size[j];;
//                                size[j] = temp + 1;
//                                t = t + (j + 1);
//                            }
//                        }
//                    }
//                    curTot = curTot + rem;
//                }
//
//            }
//
//        }
//
//

        for (int i = 0; i < size.length; i++) {
            final double x = prop[i] * totalRef * teta;
            final int hh = (int) Math.round(x);
            size[i] = hh;
            curTot = curTot + ((i + 1) * hh);

            }

        if (curTot > totalRef) {
            size[0] = size[0] - (curTot - totalRef);
            }

        if (curTot < totalRef) {
            size[0] = size[0] + (totalRef - curTot);
            }

/// creating aray of each family size ( disaggregate) and distibute randomly

        // calculate total hh size
        int ts = 0;
        for (int i = 0; i < size.length; i++) {

            ts = ts + size[i];

            }
        // initalize array based on hh size
        final int[] sizeDist = new int[ts];

        // add each hh size
        int c = 0;
        int k = 0;
        for (int t = 0; t < size.length; t++) {
            final int sum = size[t];
            c = c + sum;
            for (int j = k; j < c; j++) {
                sizeDist[j] = t + 1;
                }

            k = c;
            }

        // reshuffle position
        // Collections.shuffle(Arrays.asList(sizeDist)); // does not work

        // swaping with random posiion
        for (int i = 0; i < sizeDist.length; i++) {

            final int change = i + dadaab.random.nextInt(sizeDist.length - i);
            final int holder = sizeDist[i];
            sizeDist[i] = sizeDist[change];
            sizeDist[change] = holder;

            // System.out.println ("hh size: "+ sizeDist[i]);
            }

        // initialize household

        // UNHCR stat
        // age distibution
        // 1-4 = 0.20; 5-11 = 0.25; 12-17 = 0.12; 18-59 = 0.40;>= 60 = 0.03;

        for (int a = 0; a < sizeDist.length; a++) {
            int counter = 0;

            final int tot = sizeDist[a];
            counter = counter + tot;
            if (tot != 0 && counter <= totalRef) {
                final FieldUnit f = nextAvailCamp(dadaab);
                final Family hh = new Family(f);
                dadaab.allFamilies.add(hh);
                hh.setWaterAtHome(tot * dadaab.params.global.getMaximumWaterRequirement()
                    + (1.5 * dadaab.params.global.getMaximumWaterRequirement() * dadaab.random.nextDouble()));

                hh.setRationDate(1 + a % 9);
                if (dadaab.random.nextDouble() > dadaab.params.global.getLaterineCoverage()) {
                    hh.setHasLaterine(true);
                    }

//                if(dadaab.random.nextDouble() > 0.4){
//                    f.setCampHasLatrine(true);
//                }

                f.addRefugeeHH(hh);

                final double rn = dadaab.random.nextDouble();
                int age = 0;
                for (int i = 0; i < tot; i++) {

                    // a household head need to be between 18-59;

                    if (i == 0) {
                        age = 18 + dadaab.random.nextInt(42); // 18-59
                        } else {

                        if (rn <= 0.1) {
                            age = 1 + dadaab.random.nextInt(4); // 1-4 age
                            } else if (rn > 0.1 && rn <= 0.40) {
                            age = 5 + dadaab.random.nextInt(7); // 5=11

                            } else if (rn > 0.40 && rn <= 0.57) {
                            age = 12 + dadaab.random.nextInt(6); // 11-17
                            } else if (rn > 0.57 && rn <= 0.97) {
                            age = 18 + dadaab.random.nextInt(42); // 18-59
                            } else {
                            age = 60 + dadaab.random.nextInt(40); // 60 +
                            }
                        }

                    int sex = 0; // sex 50-50 chance
                    if (dadaab.random.nextDouble() > 0.5) {
                        sex = 1;
                        } else {
                        sex = 2;
                        }

                    // System.out.println("age: " + age + ": sex: " + sex);
                    addAllRefugees(age, sex, hh, random, dadaab);

                    }
                }

            }

        }

    private static void populate(final MersenneTwisterFast random, final Dadaab dadaab) {

        populateRefugee(random, dadaab);

        // if necessary to assign who is head of the household
//        for ( int i=0; i<dadaab.allFamilies.numObjs; i++){
//            for (int j = 0; j < ((Family)dadaab.allFamilies.objs[i]).getMembers().numObjs; j++){
//
//            }
//        }

        }

///  raod network methods from haiti project
    static void extractFromRoadLinks(final GeomVectorField roadLinks, final Dadaab dadaab) {
        final Bag geoms = roadLinks.getGeometries();
        final Envelope e = roadLinks.getMBR();
        final double xmin = e.getMinX(), ymin = e.getMinY(), xmax = e.getMaxX(), ymax = e.getMaxY();
        final int xcols = CampBuilder.gridWidth - 1, ycols = CampBuilder.gridHeight - 1;

        // extract each edge
        for (final Object o : geoms) {

            final MasonGeometry gm = (MasonGeometry) o;
            if (gm.getGeometry() instanceof LineString) {
                readLineString((LineString) gm.getGeometry(), xcols, ycols, xmin, ymin, xmax, ymax, dadaab);
                } else if (gm.getGeometry() instanceof MultiLineString) {
                final MultiLineString mls = (MultiLineString) gm.getGeometry();
                for (int i = 0; i < mls.getNumGeometries(); i++) {
                    readLineString((LineString) mls.getGeometryN(i), xcols, ycols, xmin, ymin, xmax, ymax, dadaab);
                    }
                }
            }
        }

    /**
     * Converts an individual linestring into a series of links and nodes in the
     * network int width, int height, Dadaab dadaab
     *
     * @param geometry
     * @param xcols    - number of columns in the field
     * @param ycols    - number of rows in the field
     * @param xmin     - minimum x value in shapefile
     * @param ymin     - minimum y value in shapefile
     * @param xmax     - maximum x value in shapefile
     * @param ymax     - maximum y value in shapefile
     */
    static void readLineString(final LineString geometry, final int xcols, final int ycols, final double xmin,
        final double ymin, final double xmax, final double ymax, final Dadaab dadaab) {

        final CoordinateSequence cs = geometry.getCoordinateSequence();

        // iterate over each pair of coordinates and establish a link between
        // them
        Node oldNode = null; // used to keep track of the last node referenced
        for (int i = 0; i < cs.size(); i++) {

            // calculate the location of the node in question
            final double x = cs.getX(i), y = cs.getY(i);
            final int xint = (int) Math.floor(xcols * (x - xmin) / (xmax - xmin)),
                yint = (int) (ycols - Math.floor(ycols * (y - ymin) / (ymax - ymin))); // REMEMBER TO FLIP THE Y
            // VALUE

            if (xint >= CampBuilder.gridWidth) {
                continue;
                } else if (yint >= CampBuilder.gridHeight) {
                continue;
                }

            // find that node or establish it if it doesn't yet exist
            final Bag ns = dadaab.nodes.getObjectsAtLocation(xint, yint);
            Node n;
            if (ns == null) {
                n = new Node(new FieldUnit(xint, yint));
                dadaab.nodes.setObjectLocation(n, xint, yint);
                } else {
                n = (Node) ns.get(0);
                }

            if (oldNode == n) // don't link a node to itself
                {
                continue;
                }

            // attach the node to the previous node in the chain (or continue if
            // this is the first node in the chain of links)

            if (i == 0) { // can't connect previous link to anything
                oldNode = n; // save this node for reference in the next link
                continue;
                }

            final int weight = (int) n.location.distanceTo(oldNode.location); // weight is just
            // distance

            // create the new link and save it
            final Edge e = new Edge(oldNode, n, weight);
            dadaab.roadNetwork.addEdge(e);
            oldNode.links.add(e);
            n.links.add(e);

            oldNode = n; // save this node for reference in the next link
            }
        }

    static class Node {

        FieldUnit location;
        ArrayList<Edge> links;

        public Node(final FieldUnit l) {
            location = l;
            links = new ArrayList<Edge>();
            }
        }

    /**
     * Used to find the nearest node for each space
     *
     */
    static class Crawler {

        Node node;
        FieldUnit location;

        public Crawler(final Node n, final FieldUnit l) {
            node = n;
            location = l;
            }
        }

    /**
     * Calculate the nodes nearest to each location and store the information
     *
     * @param closestNodes - the field to populate
     */
    static ObjectGrid2D setupNearestNodes(final Dadaab dadaab) {

        final ObjectGrid2D closestNodes = new ObjectGrid2D(CampBuilder.gridWidth, CampBuilder.gridHeight);
        ArrayList<Crawler> crawlers = new ArrayList<Crawler>();

        for (final Object o : dadaab.roadNetwork.allNodes) {
            final Node n = (Node) o;
            final Crawler c = new Crawler(n, n.location);
            crawlers.add(c);
            }

        // while there is unexplored space, continue!
        while (crawlers.size() > 0) {
            final ArrayList<Crawler> nextGeneration = new ArrayList<Crawler>();

            // randomize the order in which cralwers are considered
            final int size = crawlers.size();

            for (int i = 0; i < size; i++) {

                // randomly pick a remaining crawler
                final int index = dadaab.random.nextInt(crawlers.size());
                final Crawler c = crawlers.remove(index);

                // check if the location has already been claimed
                final Node n = (Node) closestNodes.get(c.location.getX(), c.location.getY());

                if (n == null) { // found something new! Mark it and reproduce

                    // set it
                    closestNodes.set(c.location.getX(), c.location.getY(), c.node);

                    // reproduce
                    final Bag neighbors = new Bag();

                    dadaab.allCamps.getNeighborsHamiltonianDistance(c.location.getX(), c.location.getY(),
                        1, false, neighbors, null, null);

                    for (final Object o : neighbors) {
                        final FieldUnit l = (FieldUnit) o;
                        // Location l = (Location) o;
                        if (l == c.location) {
                            continue;
                            }
                        final Crawler newc = new Crawler(c.node, l);
                        nextGeneration.add(newc);
                        }
                    }
                // otherwise just die
                }
            crawlers = nextGeneration;
            }
        return closestNodes;
        }

    }
