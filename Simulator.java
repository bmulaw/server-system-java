import java.lang.*;
import java.util.*; 

public class Simulator {

	/* These are the resources that we intend to monitor */
	private LinkedList<EventGenerator> resources = new LinkedList<EventGenerator>();

	/* Timeline of events */
	private Timeline timeline = new Timeline();

	/* Simulation time */
	private Double now;

	public void addMonitoredResource(EventGenerator resource) {
		this.resources.add(resource);
	}

	/* This method creates a new monitor in the simulator. To collect
	 * all the necessary statistics, we need at least one monitor. */
	private void addMonitor() {
		/* Scan the list of resources to understand the granularity of
		 * time scale to use */
		Double monRate = Double.POSITIVE_INFINITY;

		for (EventGenerator resource : resources) {
			Double rate = resource.getRate();
			if (monRate > rate) {
				monRate = rate;
			}
		}

		/* If this fails, something is wrong with the way the
		 * resources have been instantiated */
		assert !monRate.equals(Double.POSITIVE_INFINITY);

		/* Create a new monitor for this simulation */
		Monitor monitor = new Monitor(timeline, monRate, resources);

	}

	public void simulate(Double simTime) {

		/* Rewind time */
		now = new Double(0);

		/* Add monitor to the system */
		addMonitor();

		/* Main simulation loop */
		while (now < simTime) {
			/* Fetch event from timeline */
			Event evt = timeline.popEvent();

			/* Fast-forward time */
			now = evt.getTimestamp();

			/* Extract block responsible for this event */
			EventGenerator block = evt.getSource();

			/* Handle event */
			block.processEvent(evt);

		}

		/* Print all the statistics */
		for (int i = 0; i < resources.size(); ++i) {
			resources.get(i).printStats(now);
		}

	}

	/* Entry point for the entire simulation  */
	public static void main(String[] args) {

		/* Parse the input parameters */
		double simTime = Double.valueOf(args[0]);
		double lambda = Double.valueOf(args[1]);
		double ts0 = Double.valueOf(args[2]);
		double ts1 = Double.valueOf(args[3]);
		double ts2 = Double.valueOf(args[4]);
		double t1 = Double.valueOf(args[5]);
		double p1 = Double.valueOf(args[6]);
		double t2 = Double.valueOf(args[7]);
		double p2 = Double.valueOf(args[8]);
		double t3 = Double.valueOf(args[9]);
		double p3 = Double.valueOf(args[10]);
		int K = Integer.valueOf(args[11]);
		double p01 = Double.valueOf(args[12]);
		double p02 = Double.valueOf(args[13]);
		double p3exit = Double.valueOf(args[14]);
		double p31 = Double.valueOf(args[15]);
		double p32 = Double.valueOf(args[16]);

		/* Create a new simulator instance */
		Simulator sim = new Simulator();

		/* Create the traffic source */
		Source trafficSource = new Source(sim.timeline, lambda);

		/* Create new single-cpu processing server */
		SimpleServer S0 = new SimpleServer(sim.timeline, ts0);
		TwoProcessorServer S1 = new TwoProcessorServer(sim.timeline, ts1);
		MMKServer S2 = new MMKServer(sim.timeline, ts2, K);
		MM1Server S3 = new MM1Server(sim.timeline, t1, t2, t3, p1, p2, p3);


		LinkedList<EventGenerator> events = new LinkedList<EventGenerator>();
		events.push(S0);
		events.push(S1);
		events.push(S2);
		events.push(S3);
		Sink trafficSink = new Sink(sim.timeline, events);

		/* Give some names to identify these servers when printing
		 * trace and statistics */
		S0.setName("S0");
		S1.setName("S1");
		S2.setName("S2");
		S3.setName("S3");

		/* Create two routing nodes */
		RoutingNode route0 = new RoutingNode(sim.timeline);
		RoutingNode route1 = new RoutingNode(sim.timeline);
		RoutingNode route2 = new RoutingNode(sim.timeline);
		RoutingNode route3 = new RoutingNode(sim.timeline);

		/* Establish routing */
		trafficSource.routeTo(S0);
		S0.routeTo(route0);
		route0.routeTo(S1, p01);
		route0.routeTo(S2, p02);
		// --------------------- //
		S1.routeTo(route1);
		S2.routeTo(route2);
		route1.routeTo(S3);
		route2.routeTo(S3);
		// --------------------- //
		S3.routeTo(route3);
		route3.routeTo(trafficSink, p3exit);
		route3.routeTo(S1, p31);
		route3.routeTo(S2, p32);

		/* Add resources to be monitored */
		sim.addMonitoredResource(S0);
		sim.addMonitoredResource(S1);
		sim.addMonitoredResource(S2);
		sim.addMonitoredResource(S3);
		sim.addMonitoredResource(trafficSink);

		/* Kick off simulation */
		sim.simulate(simTime);
	}

}
