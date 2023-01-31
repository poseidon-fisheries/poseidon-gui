/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (c) 2023, CoHESyS Lab, cohesys.lab@gmail.com.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.poseidon.experiments;

import sim.display.Console;
import uk.ac.ox.oxfish.biology.growers.SimpleLogisticGrowerFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.HalfBycatchFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.WellMixedBiologyFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.regs.factory.MultiITQFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.poseidon.gui.FishGUI;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 2 Species ITQ in a well-mixed world
 * Created by carrknight on 10/9/15.
 */
public class TwoSpeciesITQ {

    public static void firstdemo(String[] args) {


        final FishState state = new FishState(System.currentTimeMillis());
        //world split in half

        MultiITQFactory multiFactory = new MultiITQFactory();
        //quota ratios: 90-10
        multiFactory.setQuotaFirstSpecie(new FixedDoubleParameter(4500));
        multiFactory.setQuotaOtherSpecies(new FixedDoubleParameter(500));
        //biomass ratio: 70-30
        WellMixedBiologyFactory biologyFactory = new WellMixedBiologyFactory();
        biologyFactory.setCapacityRatioSecondToFirst(new FixedDoubleParameter(.3));


        PrototypeScenario scenario = new PrototypeScenario();
        state.setScenario(scenario);
        //world split in half
        scenario.setBiologyInitializer(biologyFactory);
        scenario.setRegulation(multiFactory);


        scenario.setUsePredictors(true);

        //make species 2 worthless
        state.registerStartable(new Startable() {
            @Override
            public void start(FishState model) {
                List<Market> markets = state.getAllMarketsForThisSpecie(state.getSpecies().get(1));
                assert markets.size() == 1;
                ((FixedPriceMarket) markets.get(0)).setPrice(0d);
            }

            @Override
            public void turnOff() {

            }
        });


        state.start();
        while (state.getYear() < 10)
            state.schedule.step(state);

        FishStateUtilities.printCSVColumnToFile(
            Paths.get("docs", "20151009 lambda3", "red_landings.csv").toFile(),
            state.getYearlyDataSet().getColumn(state.getSpecies().get(0) + " " + AbstractMarket.LANDINGS_COLUMN_NAME)
        );

        FishStateUtilities.printCSVColumnToFile(
            Paths.get("docs", "20151009 lambda3", "blue_landings.csv").toFile(),
            state.getYearlyDataSet().getColumn(state.getSpecies().get(1) + " " + AbstractMarket.LANDINGS_COLUMN_NAME)
        );

        FishStateUtilities.printCSVColumnToFile(
            Paths.get("docs", "20151009 lambda3", "red_quotas.csv").toFile(),
            state.getDailyDataSet().getColumn("ITQ Last Closing Price Of Species " + 0)
        );

        FishStateUtilities.printCSVColumnToFile(
            Paths.get("docs", "20151009 lambda3", "blue_quotas.csv").toFile(),
            state.getDailyDataSet().getColumn("ITQ Last Closing Price Of Species " + 1)
        );
    }


    public static void firstGui(String[] args) {


        final FishState state = new FishState(System.currentTimeMillis());
        //world split in half

        MultiITQFactory multiFactory = new MultiITQFactory();
        //quota ratios: 90-10
        multiFactory.setQuotaFirstSpecie(new FixedDoubleParameter(4500));
        multiFactory.setQuotaOtherSpecies(new FixedDoubleParameter(500));
        //biomass ratio: 70-30
        WellMixedBiologyFactory biologyFactory = new WellMixedBiologyFactory();
        biologyFactory.setCapacityRatioSecondToFirst(new FixedDoubleParameter(.3));


        PrototypeScenario scenario = new PrototypeScenario();
        state.setScenario(scenario);
        //world split in half
        scenario.setBiologyInitializer(biologyFactory);
        scenario.setRegulation(multiFactory);


        scenario.setUsePredictors(true);

        //make species 2 worthless
        state.registerStartable(new Startable() {
            @Override
            public void start(FishState model) {
                List<Market> markets = state.getAllMarketsForThisSpecie(state.getSpecies().get(1));
                assert markets.size() == 1;
                ((FixedPriceMarket) markets.get(0)).setPrice(0d);
            }

            @Override
            public void turnOff() {

            }
        });

        FishGUI vid = new FishGUI(state);
        Console c = new Console(vid);
        c.setVisible(true);

    }


    public static void main(String[] args) {


        final FishState state = new FishState(System.currentTimeMillis());
        //world split in half

        MultiITQFactory multiFactory = new MultiITQFactory();
        //quota ratios: 90-10
        multiFactory.setQuotaFirstSpecie(new FixedDoubleParameter(4500));
        multiFactory.setQuotaOtherSpecies(new FixedDoubleParameter(500));

        HalfBycatchFactory biologyFactory = new HalfBycatchFactory();
        biologyFactory.setCarryingCapacity(new FixedDoubleParameter(5000));
        biologyFactory.setGrower(new SimpleLogisticGrowerFactory(.9));
        biologyFactory.setDifferentialPercentageToMove(new FixedDoubleParameter(.2));
        biologyFactory.setPercentageLimitOnDailyMovement(new FixedDoubleParameter(.2));

        PrototypeScenario scenario = new PrototypeScenario();
        state.setScenario(scenario);
        //world split in half
        scenario.setBiologyInitializer(biologyFactory);
        scenario.setRegulation(multiFactory);

        SimpleMapInitializerFactory simpleMap = new SimpleMapInitializerFactory();
        simpleMap.setCoastalRoughness(new FixedDoubleParameter(0d));
        scenario.setMapInitializer(simpleMap);
        scenario.forcePortPosition(new int[]{40, 25});
        //try also 40,25


        scenario.setUsePredictors(true);


        FishGUI vid = new FishGUI(state);
        Console c = new Console(vid);
        c.setVisible(true);

    }


    public static void secondDemo(String[] args) {


        final FishState state = new FishState(System.currentTimeMillis());
        //world split in half

        MultiITQFactory multiFactory = new MultiITQFactory();
        //quota ratios: 90-10
        multiFactory.setQuotaFirstSpecie(new FixedDoubleParameter(4500));
        multiFactory.setQuotaOtherSpecies(new FixedDoubleParameter(500));

        HalfBycatchFactory biologyFactory = new HalfBycatchFactory();
        biologyFactory.setCarryingCapacity(new FixedDoubleParameter(5000));


        PrototypeScenario scenario = new PrototypeScenario();
        state.setScenario(scenario);
        //world split in half
        scenario.setBiologyInitializer(biologyFactory);
        scenario.setRegulation(multiFactory);

        SimpleMapInitializerFactory simpleMap = new SimpleMapInitializerFactory();
        simpleMap.setCoastalRoughness(new FixedDoubleParameter(0d));
        scenario.setMapInitializer(simpleMap);
        scenario.forcePortPosition(new int[]{40, 25});
        //try also 40,25


        scenario.setUsePredictors(true);


        state.start();
        while (state.getYear() < 10)
            state.schedule.step(state);

        Path directory = Paths.get("docs", "20151014 corollaries");

        //show the effect on catches
        FishStateUtilities.printCSVColumnToFile(
            directory.resolve("geo_red_landings.csv").toFile(),
            state.getYearlyDataSet().getColumn(state.getSpecies().get(0) + " " + AbstractMarket.LANDINGS_COLUMN_NAME)
        );

        FishStateUtilities.printCSVColumnToFile(
            directory.resolve("geo_blue_landings.csv").toFile(),
            state.getYearlyDataSet().getColumn(state.getSpecies().get(1) + " " + AbstractMarket.LANDINGS_COLUMN_NAME)
        );

        FishStateUtilities.printCSVColumnToFile(
            directory.resolve("north.csv").toFile(),
            state.getYearlyDataSet().getColumn("# of North Tows")
        );

        FishStateUtilities.printCSVColumnToFile(
            directory.resolve("south.csv").toFile(),
            state.getYearlyDataSet().getColumn("# of South Tows")
        );


    }

    public static void thirdGUI(String[] args) {


        final FishState state = new FishState(System.currentTimeMillis());
        //world split in half

        MultiITQFactory multiFactory = new MultiITQFactory();
        //quota ratios: 90-10
        multiFactory.setQuotaFirstSpecie(new FixedDoubleParameter(4500));
        multiFactory.setQuotaOtherSpecies(new FixedDoubleParameter(500));

        HalfBycatchFactory biologyFactory = new HalfBycatchFactory();
        biologyFactory.setCarryingCapacity(new FixedDoubleParameter(5000));
        biologyFactory.setGrower(new SimpleLogisticGrowerFactory(.9));
        biologyFactory.setDifferentialPercentageToMove(new FixedDoubleParameter(.2));
        biologyFactory.setPercentageLimitOnDailyMovement(new FixedDoubleParameter(.2));

        PrototypeScenario scenario = new PrototypeScenario();
        state.setScenario(scenario);
        //world split in half
        scenario.setBiologyInitializer(biologyFactory);
        scenario.setRegulation(multiFactory);

        SimpleMapInitializerFactory simpleMap = new SimpleMapInitializerFactory();
        simpleMap.setCoastalRoughness(new FixedDoubleParameter(0d));
        scenario.setMapInitializer(simpleMap);
        scenario.forcePortPosition(new int[]{40, 25});
        //try also 40,25


        scenario.setUsePredictors(true);


        FishGUI vid = new FishGUI(state);
        Console c = new Console(vid);
        c.setVisible(true);

    }

}
