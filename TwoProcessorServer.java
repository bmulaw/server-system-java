import java.util.*;

public class TwoProcessorServer extends  EventGenerator {
    
    private Double T_s;
    private String serverName = null;
    private LinkedList<Request> queue = new LinkedList<Request>();
    private LinkedList<Integer> serv = new LinkedList<Integer>();

    private Double cumulQ = new Double(0);
    private Double cumulTq = new Double(0);
    private Double busyTime0 = new Double(0);
    private Double busyTime1 = new Double(0);
    private Double busyTime2 = new Double(0);
    private int snapCount = 0;
    private int servedReqs = 0;

    public TwoProcessorServer (Timeline timeline, Double T_s) {
        super(timeline);
        this.T_s = T_s;
        serv.push(1);
        serv.push(2);
    }
    public void setName(String serverName) {
        this.serverName = serverName;
    }

    private void startService(Event evt, Request curRequest, Integer procs) {
        // REPLICATED PROFESSOR CODE IN SimpleServer.java but added procs to keep track of processors
        curRequest.setProcessor(procs);
        double tsNextEvent = evt.getTimestamp() + Exp.getExp(1/this.T_s);
        Event nextEvent = new Event(EventType.DEATH, curRequest, tsNextEvent, this);

        curRequest.recordServiceStart(evt.getTimestamp());
        System.out.println(curRequest + " START" + (this.serverName != null ? " " + this.serverName : "") + "," + procs + ": " + evt.getTimestamp());
        super.timeline.addEvent(nextEvent);
    }

    @Override
    void receiveRequest(Event evt) {
        // inspired from SimpleServer.java too
        super.receiveRequest(evt);
        Request curRequest = evt.getRequest();
        curRequest.recordArrival(evt.getTimestamp());
        if(!serv.isEmpty() && queue.isEmpty()) {
            Random rand = new Random();
            Integer procs = serv.remove(rand.nextInt(serv.size()));
            startService(evt, curRequest, procs);
        }
        queue.add(curRequest);
    }

    @Override
    void releaseRequest(Event evt) {
        // Again inspired by SimpleServer.java from professor
        Request curRequest = evt.getRequest();
        Request queueHead = queue.removeFirst();
        assert curRequest == queueHead;
        curRequest.recordDeparture(evt.getTimestamp());

        Integer procs = curRequest.getProcessor();

        busyTime0 += curRequest.getDeparture() - curRequest.getServiceStart();
        cumulTq += (curRequest.getDeparture() - curRequest.getArrival());

        if (procs == 2) {
            busyTime2 += curRequest.getDeparture() - curRequest.getServiceStart();
        } else {
            busyTime1 += curRequest.getDeparture() - curRequest.getServiceStart();
        }
        servedReqs++;
        System.out.println(curRequest + " DONE" + (this.serverName != null ? " " + this.serverName : "")  + ": " + evt.getTimestamp());
        serv.push(procs);
        System.out.println(procs + "  HERE ");

        assert super.next != null;
        super.next.receiveRequest(evt);

        if(!serv.isEmpty() && !queue.isEmpty()) {
            Request nextRequest = queue.peekFirst();
            Random rand = new Random();
            startService(evt, nextRequest, serv.remove(rand.nextInt(serv.size())));
        }
    }

    // basically copied from SimpleServer.java since files are similar
    @Override
    Double getRate() {
        return 1/this.T_s;
    }

    @Override
    void executeSnapshot() {
        snapCount++;
        cumulQ += queue.size()/2.0;
//        System.out.println("TP " + cumulQ + "  " + snapCount);
//        System.out.println("TP " + serv.size());
    }

    @Override
    void printStats(Double time) {
        if (this.serverName == null) {
            System.out.println("UTIL: " + busyTime0/time);
            System.out.println("QLEN: " + cumulQ/snapCount);
            System.out.println("TRESP: " + cumulTq/servedReqs);
        } else {
            System.out.println();
            System.out.println(this.serverName + ",1" + " UTIL: " + busyTime1/time);
            System.out.println(this.serverName + ",2" + " UTIL: " + busyTime2/time);
            System.out.println(this.serverName + " QLEN: " + ((cumulQ / snapCount)));
            System.out.println(this.serverName + " TRESP: " + ((cumulTq/servedReqs)));
        }
    }

    // need avg
    public Double getAverage() {
//        System.out.println("this is TPS cumulq: " + cumulQ);
        return (cumulQ/snapCount);
    }

    @Override
    public String toString() {
        return (this.serverName != null ? this.serverName : "");
    }

}
