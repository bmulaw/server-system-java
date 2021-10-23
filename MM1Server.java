import java.util.LinkedList;

public class MM1Server extends EventGenerator {
    // largely also replicating SimpleServer.java thanks to Professor
    private LinkedList<Request> queue = new LinkedList<Request>();

    private String name = null;
    private Double t1, t2, t3;
    private Double p1, p2, p3;
    
    private Double cumulQ = new Double(0);
    private Double cumulW = new Double(0);
    private Double cumulTq = new Double(0);
    private Double cumulTw = new Double(0);
    private Double busyTime = new Double(0);
    private int snapCount = 0;
    private int servedReqs = 0;

    public MM1Server(Timeline timeline, Double t1, Double t2, Double t3,
                     Double p1,  Double p2, Double p3) {
        super(timeline);
        this.t1 = t1; this.t2 = t2; this.t3 = t3;
        this.p1 = p1; this.p2 = p2; this.p3 = p3;
    }

    public void setName(String name) {
        this.name = name;
    }

    private double getTime() {
        double rand = Math.random();
        if (p1 >= rand) {
            return t1;
        } else if (p1+p2 >= rand) {
            return t2;
        } else {
            return t3;
        }
    }

    private void startService(Event evt, Request curRequest) {
        // EXACT SAME CODE AS __startService from SimpleServer.java
        Event nextEvent = new Event(EventType.DEATH, curRequest,
                evt.getTimestamp() + getTime(), this);
        curRequest.recordServiceStart(evt.getTimestamp());
        cumulTw += curRequest.getServiceStart() - curRequest.getArrival();
        System.out.println(curRequest + " START" + (this.name != null ? " " + this.name : "")  + ": " + evt.getTimestamp());
        super.timeline.addEvent(nextEvent);
    }

    @Override
    void receiveRequest(Event evt) {
        // copied and pasted same code from SimpleServer.java
        super.receiveRequest(evt);
        Request curRequest = evt.getRequest();
        curRequest.recordArrival(evt.getTimestamp());

        if(queue.isEmpty()) {
            startService(evt, curRequest);
        }
        queue.add(curRequest);
    }

    @Override
    void releaseRequest(Event evt) {
        Request curRequest = evt.getRequest();
        Request queueHead = queue.removeFirst();
        assert curRequest == queueHead;
        curRequest.recordDeparture(evt.getTimestamp());
        busyTime += curRequest.getDeparture() - curRequest.getServiceStart();
        cumulTq += curRequest.getDeparture() - curRequest.getArrival();

        servedReqs++;
//        System.out.println(curRequest + " DONE" + (this.name != null ? " " + this.name : "")  + ": " + evt.getTimestamp());
        assert super.next != null;
        super.next.receiveRequest(evt);

        if(!queue.isEmpty()) {
            Request nextRequest = queue.peekFirst();
            startService(evt, nextRequest);
        }
    }

    @Override
    void executeSnapshot() {
        snapCount++;
        cumulQ += queue.size();
//        System.out.println("M1 " + cumulQ + "  " + snapCount);
        cumulW += Math.max(queue.size()-1, 0);
    }

    @Override
    void printStats(Double time) {
        if (this.name == null) {
            System.out.println("UTIL: " + busyTime/time);
            System.out.println("QLEN: " + cumulQ/snapCount);
            System.out.println("TRESP: " + cumulTq/servedReqs);
        } else {
            System.out.println();
            System.out.println(this.name + " UTIL: " + busyTime/time);
            System.out.println(this.name + " QLEN: " + cumulQ/snapCount);
            System.out.println(this.name + " TRESP: "+ cumulTq/servedReqs);
        }
    }

    public Double getAverage() {
//        System.out.println("this is MM1 cumulq: " + cumulQ);
        return (cumulQ/snapCount);
    }

    @Override
    public String toString() {
        return (this.name != null ? this.name : "");
    }

}
